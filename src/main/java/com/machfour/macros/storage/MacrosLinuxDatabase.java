package com.machfour.macros.storage;

import com.machfour.macros.core.*;
import com.machfour.macros.data.*;
import com.machfour.macros.data.Columns.*;
import com.machfour.macros.data.Tables.*;
import com.machfour.macros.data.Types;
import com.machfour.macros.util.DateStamp;
import com.sun.istack.internal.NotNull;
import org.sqlite.SQLiteDataSource;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

import static com.machfour.macros.storage.SqlUtils.toList;

// data source provided by Xerial library

public class MacrosLinuxDatabase implements MacrosDataSource {
    private static final Path INIT_SQL = Paths.get("/home/max/devel/macros/macros-db-create.sql");
    private static final Path TRIG_SQL = Paths.get("/home/max/devel/macros/macros-db-triggers.sql");
    private static final Path DATA_SQL = Paths.get("/home/max/devel/macros/macros-initial-data.sql");
    private static final PrintStream out = System.out;
    private static final PrintStream err = System.err;
    private static final String DB_LOCATION = "/home/max/devel/macros-java/sample.db";
    private static final Path DB_PATH = Paths.get(DB_LOCATION);

    private final SQLiteDataSource dataSource;

    // singleton
    private static MacrosLinuxDatabase INSTANCE;

    private MacrosLinuxDatabase() {
        dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + DB_PATH.toAbsolutePath());
    }

    public static MacrosLinuxDatabase getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MacrosLinuxDatabase();
        }
        return INSTANCE;
    }

    private static String createStatements(List<String> sqlFileLines) {
        // steps: remove all comment lines, trim, join, split on semicolon
        List<String> trimmedAndDecommented = new ArrayList<>(sqlFileLines.size());
        for (String line : sqlFileLines) {
            int commentIndex = line.indexOf("--");
            if (commentIndex != -1) {
                line = line.substring(0, commentIndex);
            }
            line = line.trim();
            line = line.replaceAll("\\s+", " ");
            if (line.length() != 0) {
                trimmedAndDecommented.add(line);
            }
        }
        return String.join(" ", trimmedAndDecommented);
    }

    private static void runStatements(Connection c, List<String> sqlStatements) throws SQLException {
        try (Statement s = c.createStatement()) {
            for (String sql : sqlStatements) {
                s.executeUpdate(sql);
            }
        }
    }


    public boolean dbExists() {
        return Files.exists(DB_PATH);
    }

    public boolean removeDb() throws IOException {
        if (Files.exists(DB_PATH)) {
            Files.delete(DB_PATH);
        }
        return true;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void initDb() throws SQLException, IOException {
        try (Connection c = getConnection()) {
            List<String> initStatements = new ArrayList<>(3);
            initStatements.add(createStatements(Files.readAllLines(INIT_SQL)));
            initStatements.add(createStatements(Files.readAllLines(TRIG_SQL)));
            initStatements.add(createStatements(Files.readAllLines(DATA_SQL)));
            runStatements(c, initStatements);
        }
    }

    @Override
    public <M extends MacrosPersistable<M>> int deleteObject(@NotNull M o) throws SQLException {
        return deleteById(o.getId(), o.getTable());
    }

    private <M extends MacrosPersistable> int deleteById(Long id, Table<M> t) throws SQLException {
        try (Connection c = getConnection();
                PreparedStatement s = c.prepareStatement(SqlUtils.deleteTemplate(t, t.getIdColumn()))) {
            SqlUtils.bindObjects(s, toList(id));
            s.executeUpdate();
            return 1;
        }
    }

    @Override
    // TODO make this the general one
    public <M extends MacrosPersistable<M>> int deleteObjects(@NotNull List<M> objects) throws SQLException {
        int deleted = 0;
        if (!objects.isEmpty()) {
            Table<M> t = objects.get(0).getTable();
            for (M object : objects) {
                if (object != null) {
                    deleted += deleteById(object.getId(), t);
                }
            }
        }
        return deleted;
    }

    @Override
    public List<Long> foodSearch(String keyword) throws SQLException {
        List<Column<Food, Types.Text, String>> columns = Arrays.asList(
              Columns.FoodCol.INDEX_NAME
            , Columns.FoodCol.NAME
            , Columns.FoodCol.COMMERCIAL_NAME
            , Columns.FoodCol.BRAND
        );
        return prefixSearch(Tables.FoodTable.instance(), columns, keyword);
    }

    public <M extends MacrosPersistable> List<Long> prefixSearch(
            Table<M> t, List<Column<M, Types.Text, String>> cols, String keyword) throws SQLException {
        List<Long> resultList = new ArrayList<>(0);
        if (!cols.isEmpty()) {
            // TODO copy-pasted from SelectColumn... probably needs refactoring
            try (Connection c = getConnection();
                 PreparedStatement p = c.prepareStatement(SqlUtils.selectLikeTemplate(t, t.getIdColumn(), cols))) {
                // have to append the percent sign for LIKE globbing to the actual argument string
                String keywordGlob = keyword + "%";
                List<String> bindString = Collections.nCopies(cols.size(), keywordGlob);
                SqlUtils.bindObjects(p, bindString);
                try (ResultSet rs = p.executeQuery()) {
                    for (; rs.next(); rs.afterLast()) {
                        resultList.add(rs.getLong(0));
                    }
                }
            }
        }
        return resultList;
    }

    @Override
    public List<Food> getAllFoods() throws SQLException {
        List<Serving> allServings = getRawObjectsByIds(ServingTable.instance(), new ArrayList<>());
        // TODO
        return getFoodsById(new ArrayList<>(0));
    }

    public List<Food> getFoodsById(@NotNull List<Long> foodIds) throws SQLException {
        List<Food> foods = getRawObjectsByIds(FoodTable.instance(), foodIds);
        Map<Long, Food> idMap = SqlUtils.makeIdMap(foods);
        applyServingsToRawFoods(idMap);
        applyNutritionDataToRawFoods(idMap);
        // TODO FoodType, FoodCategory
        return foods;
    }

    private void applyServingsToRawFoods(Map<Long, Food> foodMap) throws SQLException {
        List<Long> foodIds = new ArrayList<>(foodMap.keySet());
        List<Long> servingIds = selectColumn(ServingTable.instance(), ServingCol.ID, ServingCol.FOOD_ID, foodIds);
        List<Serving> servings = getRawObjectsByKeys(ServingTable.instance(), ServingCol.ID, servingIds);
        for (Serving s : servings) {
            // this query should never fail, due to database constraints
            Food f = foodMap.get(s.getFoodId());
            s.setFood(f);
            f.addServing(s);
        }
    }

    private void applyNutritionDataToRawFoods(Map<Long, Food> foodMap) throws SQLException {
        List<Long> foodIds = new ArrayList<>(foodMap.keySet());
        List<Long> nutritionDataIds = selectColumn(NutritionDataTable.instance(), NutritionDataCol.ID, NutritionDataCol.FOOD_ID, foodIds);
        List<NutritionData> servings = getRawObjectsByKeys(NutritionDataTable.instance(), NutritionDataCol.ID, nutritionDataIds);
        for (NutritionData nd : servings) {
            // this query should never fail, due to database constraints
            Food f = foodMap.get(nd.getFoodId());
            nd.setFood(f);
            f.setNutritionData(nd);
        }
    }

    @Override
    public Food getFoodById(Long id) throws SQLException {
        List<Food> resultFood = getFoodsById(toList(id));
        return resultFood.isEmpty() ? null : resultFood.get(0);
    }

    private <M, S extends MacrosType<I>, T extends MacrosType<J>, I, J> List<I> selectColumn(
            Table<M> t, Column<M, S, I> selectColumn, Column<M, T, J> whereColumn, J whereValue) throws SQLException {
        return selectColumn(t, selectColumn, whereColumn, toList(whereValue), false);
    }

    private <M, S extends MacrosType<I>, T extends MacrosType<J>, I, J> List<I> selectColumn(
            Table<M> t, Column<M, S, I> selectColumn, Column<M, T, J> whereColumn, List<J> whereValues) throws SQLException {
        return selectColumn(t, selectColumn, whereColumn, whereValues, false);
    }

    // does SELECT (selectColumn) FROM (t) WHERE (whereColumn) = (whereValue)
    // or SELECT (selectColumn) FROM (t) WHERE (whereColumn) IN (whereValue1, whereValue2, ...)
    private <M, S extends MacrosType<I>, T extends MacrosType<J>, I, J> List<I> selectColumn(
            Table<M> t, Column<M, S, I> selectColumn, Column<M, T, J> whereColumn, List<J> whereValues, boolean distinct) throws SQLException {
        List<I> resultList = new ArrayList<>(0);
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(SqlUtils.selectTemplate(t, selectColumn, whereColumn, whereValues.size(), distinct))) {
            SqlUtils.bindObjects(p, whereValues);
            try (ResultSet rs = p.executeQuery()) {
                for (; rs.next(); rs.afterLast()) {
                    resultList.add(selectColumn.type().fromRaw(rs.getObject(0)));
                }
            }

        }
        return resultList;
    }

    @Override
    public Food getFoodByIndexName(String indexName) throws SQLException {
        Long id = getFoodIdForIndexName(indexName);
        return id == null ? null : getFoodById(id);
    }

    private Long getFoodIdForIndexName(String indexName) throws SQLException {
        List<Long> idList = selectColumn(FoodTable.instance(), FoodCol.ID, FoodCol.INDEX_NAME, indexName);
        // a bit redundant since we only need the ID but whatever
        return idList.isEmpty() ? null : idList.get(0);
    }

    @Override
    public Meal getMealById(Long id) throws SQLException {
        List<Meal> resultMeals = getMealsById(Collections.singletonList(id));
        return (!resultMeals.isEmpty()) ? resultMeals.get(0) : null;
    }

    @Override
    public List<Meal> getMealsById(@NotNull List<Long> mealIds) throws SQLException {
        if (mealIds.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<Long> foodIds = getFoodIdsForMeals(mealIds);
        List<Meal> meals = getRawMealsById(mealIds);
        // this check stops an unnecessary lookup of all foods, which happens if no IDS are passed
        // into getFoodsByID;
        if (!foodIds.isEmpty()) {
            List<Food> foods = getFoodsById(foodIds);
            Map<Long, Food> foodMap = new HashMap<>(foods.size(), 1);
            for (Food food : foods) {
                foodMap.put(food.getId(), food);
            }

            for (Meal meal : meals) {
                applyFoodPortionsToRawMeal(meal, foodMap);
            }
        }


        return meals;
    }

    private List<Long> getFoodIdsForMeals(List<Long> mealIds) throws SQLException {
        return selectColumn(FoodPortionTable.instance(), FoodPortionCol.FOOD_ID, FoodPortionCol.MEAL_ID, mealIds, true);
    }

    // Makes meal objects, filtering by the list of IDs. If mealIds is empty,
    // all meals will be returned.
    private List<Meal> getRawMealsById(@NotNull List<Long> mealIds) throws SQLException {
        return getRawObjectsByKeys(MealTable.instance(), MealCol.ID, mealIds);
    }

    /*
     * The map must map the meal ID to the (already created) FoodTable objects needed by FoodPortions
     * in that meal.
     */
    private void applyFoodPortionsToRawMeal(Meal meal, Map<Long, Food> foodMap) throws SQLException {
        List<Long> foodPortionIds = selectColumn(FoodPortionTable.instance(), FoodPortionCol.ID, FoodPortionCol.MEAL_ID, meal.getId());
        if (!foodPortionIds.isEmpty()) {
            List<FoodPortion> foodPortions = getRawObjectsByIds(FoodPortionTable.instance(), foodPortionIds);
            for (FoodPortion fp : foodPortions) {
                Food portionFood = foodMap.get(fp.getFoodId());
                fp.setFood(portionFood);
                Long servingId = fp.getServingId();
                if (servingId != null) {
                    Serving serving = portionFood.getServingById(servingId);
                    assert serving != null : "Serving specified by FoodPortion not found in its food!";
                    fp.setServing(serving);
                }
                fp.setMeal(meal);
                meal.addFoodPortion(fp);
            }
        }
    }

    @Override
    public List<Meal> getMealsForDay(DateStamp day) throws SQLException {
        List<Long> mealIds = getMealIdsForDay(day);
        return getMealsById(mealIds);
    }

    @Override
    public List<Long> getMealIdsForDay(@NotNull DateStamp day) throws SQLException {
        return selectColumn(MealTable.instance(), MealCol.ID, MealCol.DAY, Collections.singletonList(day));
        // TODO: need "DATE(" + Meal.Column.DAY + ") = DATE ( ? )"; ???
    }

    private <M extends MacrosPersistable> M getRawObjectById(Table<M> t, Long id) throws SQLException {
        return getRawObjectByKey(t, t.getIdColumn(), id);
    }

    private <M extends MacrosPersistable> List<M> getRawObjectsByIds(Table<M> t, List<Long> ids) throws SQLException {
        return getRawObjectsByKeys(t, t.getIdColumn(), ids);
    }

    // Retrives an object by a key column, and constructs it without any FK object instances.
    // Returns null if no row in the corresponding table had a key with the given value
    private <M, T extends MacrosType<J>, J> List<M> getRawObjectsByKeys(Table<M> t, Column<M, T, J> keyCol, List<J> keys) throws SQLException {
        List<M> objects = new ArrayList<>(keys.size());
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(SqlUtils.selectTemplate(t, t.columns(), keyCol, keys.size(), false))) {
            SqlUtils.bindObjects(p, keys);
            try (ResultSet rs = p.executeQuery()) {
                for (; !rs.isAfterLast(); rs.next()) {
                    ColumnData<M> container = new ColumnData<>(t);
                    for (Column<M, ?, ?> col : t.columns()) {
                        SqlUtils.rawToColumnData(container, col, rs.getObject(col.sqlName()));
                    }
                    objects.add(t.construct(container, true));
                }
            }
        }
        return objects;
    }

    private <M, T extends MacrosType<J>, J> M getRawObjectByKey(Table<M> t, Column<M, T, J> keyCol, J key) throws SQLException {
        List<M> returned = getRawObjectsByKeys(t, keyCol, Collections.singletonList(key));
        assert returned.size() <= 1;
        return returned.isEmpty() ? null : returned.get(0);
    }

    @Override
    public <M extends MacrosPersistable<M>> int insertObjects(@NotNull List<M> objects, boolean withId) throws SQLException {
        if (objects.isEmpty()) {
            return 0;
        }
        int saved = 0;
        Table<M> table = objects.get(0).getTable();
        List<Column<M, ?, ?>> columnsToInsert = table.columns();
        if (!withId) {
            columnsToInsert.remove(table.getIdColumn());
        } // else inserting for the first time, but it has an ID that we want to keep intact
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement(SqlUtils.insertTemplate(table, columnsToInsert))) {
                for (M object : objects) {
                    SqlUtils.bindData(p, object.getAllData(), columnsToInsert);
                    saved += p.executeUpdate();
                    p.clearParameters();
                }
            }
            c.commit();
            c.setAutoCommit(true);
        }
        return saved;
    }

    // Note that if the id is not found in the database, nothing will be inserted
    @Override
    public <M extends MacrosPersistable<M>> int updateObjects(@NotNull List<M> objects) throws SQLException {
        if (objects.isEmpty()) {
            return 0;
        }
        int saved = 0;
        Table<M> table = objects.get(0).getTable();
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement(SqlUtils.updateTemplate(table, table.columns(), table.getIdColumn()))) {
                for (M object : objects) {
                    SqlUtils.bindData(p, object.getAllData(), table.columns(), object.getId());
                    saved += p.executeUpdate();
                    p.clearParameters();
                }
            }
            c.commit();
            c.setAutoCommit(true);
        }
        return saved;
    }

    public <M extends MacrosPersistable> int removeAll(Table<M> t) throws SQLException {
        try (Connection c = getConnection();
                PreparedStatement p = c.prepareStatement(SqlUtils.deleteTemplate(t))) {
            return p.executeUpdate();
        }
    }

    @Override
    public <M extends MacrosPersistable<M>> int saveObject(@NotNull M o) throws SQLException {
        if (o.isFromDb()) {
            return updateObjects(toList(o));
        } else {
            return insertObjects(toList(o), !o.getId().equals(MacrosPersistable.NO_ID));
        }
    }
}
