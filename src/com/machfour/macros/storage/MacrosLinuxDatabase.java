package com.machfour.macros.storage;

import com.machfour.macros.core.*;
import com.machfour.macros.data.*;
import com.machfour.macros.util.DateStamp;
import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class MacrosLinuxDatabase implements MacrosDataSource {
    private static final String TAG = "MacrosDatabase";
    private static final Path INIT_SQL = Paths.get("/home/max/devel/macros/macros-db-create.sql");
    private static final Path TRIG_SQL = Paths.get("/home/max/devel/macros/macros-db-triggers.sql");
    private static final Path DATA_SQL = Paths.get("/home/max/devel/macros/macros-initial-data.sql");
    private static final PrintStream out = System.out;
    private static final PrintStream err = System.err;
    private static final String DB_LOCATION = "/home/max/devel/macros-java/sample.db";
    private static final Path DB_PATH = Paths.get(DB_LOCATION);
    // singleton
    private static MacrosLinuxDatabase INSTANCE;

    private MacrosLinuxDatabase() {
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

    private static boolean runStatements(Connection c, List<String> sqlStatements) {
        try (Statement s = c.createStatement()) {
            for (String sql : sqlStatements) {
                out.println("Executing statement: '" + sql + "'");
                s.executeUpdate(sql);
            }
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }


    private static boolean removeDb() {
        try {
            if (Files.exists(DB_PATH)) {
                Files.delete(DB_PATH);
            }
            return true;
        } catch (IOException e) {
            err.println("Could not delete database: " + e.getMessage());
            return false;
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + DB_PATH.toAbsolutePath());
    }

    private static boolean initDb() {
        try (Connection c = getConnection()) {
            List<String> initStatements = new ArrayList<>(3);
            initStatements.add(createStatements(Files.readAllLines(INIT_SQL)));
            initStatements.add(createStatements(Files.readAllLines(TRIG_SQL)));
            initStatements.add(createStatements(Files.readAllLines(DATA_SQL)));
            return runStatements(c, initStatements);
        } catch (IOException | SQLException e) {
            err.println(e);
            return false;
        }
    }

    private static <M extends MacrosPersistable> int insert(Connection c, Table<M> t, ColumnData<M> values, boolean withId) throws SQLException {
        List<Column<M, ?>> columnsToInsert = t.columns();
        if (!withId) {
            columnsToInsert.remove(t.getIdColumn());
        }
        try (PreparedStatement p = c.prepareStatement(SqlUtils.insertTemplate(t, columnsToInsert))) {
            SqlUtils.bindData(p, values, columnsToInsert);
            return p.executeUpdate();
        }
    }

    private static <M extends MacrosPersistable, T> int update(Connection c, Table<M> t, ColumnData<M> values, Column<M, T> keyCol) throws SQLException {
        T key = values.unboxColumn(keyCol);
        try (PreparedStatement p = c.prepareStatement(SqlUtils.updateTemplate(t, t.columns(), keyCol))) {
            SqlUtils.bindData(p, values, t.columns(), key);
            return p.executeUpdate();
        }
    }

    @Override
    public <M extends MacrosPersistable<M>> boolean deleteObject(@NotNull M o) {
        return deleteById(o.getId(), o.getTable());
    }

    private boolean deleteById(Long id, Table t) {
        try (Connection c = getConnection()) {
            try (Statement s = c.createStatement()) {
                s.executeUpdate("DELETE FROM " + t.name() + " WHERE " + t.getIdColumn().sqlName() + " = " + id.toString());
                return true;
            }
        } catch (SQLException e) {
            err.println(e);
            return false;
        }
    }

    @Override
    public <M extends MacrosPersistable<M>> void deleteObjects(@NotNull List<M> objects) {
        if (!objects.isEmpty()) {
            Table t = objects.get(0).getTable();
            for (M object : objects) {
                if (object != null) {
                    deleteById(object.getId(), t);
                }
            }
        }
    }

    @Override
    public List<Long> foodSearch(String keyword) {
        List<Column<Food, ?>> columns = Arrays.asList(
            Columns.FoodCol.INDEX_NAME
            , Columns.FoodCol.NAME
            , Columns.FoodCol.COMMERCIAL_NAME
            , Columns.FoodCol.BRAND
        );
        return prefixSearch(Tables.FoodTable.getInstance(), columns, keyword);
    }

    public List<Long> prefixSearch(Table conv, List<Column<Food, ?>> cols, String keyword) {
        int numCols = cols.size();
        if (numCols == 0) {
            return Collections.emptyList();
        }

        SQLiteDatabase db = helper.getReadableDatabase();
        String[] projection = new String[]{conv.getIdColumnName()};
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(conv.getTableName());

        StringBuilder selection = new StringBuilder();
        for (int i = 0; i < numCols - 1; ++i) {
            selection.append(cols.get(i));
            selection.append(" LIKE ?");
            selection.append(" OR ");
        }
        selection.append(cols.get(numCols - 1));
        selection.append(" LIKE ?");

        // have to append the percent sign for LIKE globbing to the actual argument string
        String keywordGlob = keyword + "%";
        String[] selectionArgs = Collections.nCopies(numCols, keywordGlob).toArray(new String[numCols]);

        Cursor c = queryBuilder.query(db, projection, selection.toString(), selectionArgs,
            null, null, null);

        List<Long> ids;
        if (c != null) {
            ids = new ArrayList<>(c.getCount());
            if (c.getCount() > 0) {
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    // column at index zero is the ID, since no other results are returned
                    ids.add(c.getLong(0));
                }
            }
            c.close();
        } else {
            ids = Collections.emptyList();
        }

        db.close();
        return ids;


    }

    @Override
    public List<Food> getAllFoods() {
        List<Serving> allServings = getRawServingsById(new ArrayList<Long>());
        return getFoodsById(new ArrayList<Long>(0));
    }

    public List<Food> getFoodsById(@NotNull List<Long> foodIds) {
        List<Food> foods = getRawFoodsById(foodIds);
        Map<Long, Food> foodMap = new HashMap<>(foods.size(), 1);

        for (Food f : foods) {
            foodMap.put(f.getId(), f);
        }

        applyServingsToRawFoods(foodMap);

        return foods;
    }

    private void applyServingsToRawFoods(Map<Long, Food> foodsByIds) {
        List<Serving> servings = getRawServingsForFoods(foodsByIds.keySet());

        for (Serving s : servings) {
            // this query should never fail, due to database constraints
            Food f = foodsByIds.get(s.getFoodId());
            s.setFood(f);
            f.addServing(s);
        }
    }

    private List<Serving> getRawServingsForFoods(Collection<Long> foodIds) {
        List<Long> servingIds = getServingIdsForFoods(foodIds);
        if (!servingIds.isEmpty()) {
            return getRawServingsById(servingIds);
        } else {
            return Collections.emptyList();
        }
    }

    private List<Long> getServingIdsForFoods(Collection<Long> foodIds) {
        Log.d(TAG, "getServingIdsForFoods((" + foodIds + ")");
        SQLiteDatabase db = helper.getReadableDatabase();
        String[] projection = new String[]{Serving.Column.ID.str};
        String selection = buildWhereString(Serving.Column.FOOD_ID.str, foodIds);
        String table = Serving.CONVERTER.getTableName();

        List<Long> servingIds = new ArrayList<>(0);

        if (foodIds.size() > 0) {
            Cursor c = db.query(table, projection, selection, null, null, null, null);
            if (c != null) {
                servingIds = new ArrayList<>(c.getCount());
                if (c.getCount() > 0) {
                    for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                        // column at index zero is the ID, since no other results are returned
                        servingIds.add(c.getLong(0));
                    }
                }
                c.close();
            }
        }

        db.close();
        return servingIds;
    }

    // Makes food objects, filtering by the list of IDs. If foodIds is empty,
    // all foods will be returned.
    private List<Food> getRawFoodsById(@NotNull List<Long> foodIds) {
        SQLiteDatabase db = helper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        String[] projection = Food.CONVERTER.getColumns();
        queryBuilder.setTables(Food.CONVERTER.getTableName());
        if (foodIds.size() > 0) {
            queryBuilder.appendWhere(buildWhereString(Food.CONVERTER.getIdColumnName(), foodIds));
        }
        String orderBy = "lower(" + Food.Column.NAME + ") ASC";
        Cursor c = queryBuilder.query(db, projection, null, null, null, null, orderBy);

        List<Food> foods;
        if (c != null) {
            foods = cursorToRawFoods(c, projection.length);
            c.close();
        } else {
            foods = new ArrayList<>();
        }

        return foods;
    }

    // if servingIds is empty, returns all servings
    // Don't need to return servings explicitly; they are discoverable via food.getServings()
    // They can also be cached by adding them this way
    private List<Serving> getRawServingsById(List<Long> servingIds) {
        SQLiteDatabase db = helper.getReadableDatabase();
        MacrosPersistable.Converter conv = Serving.CONVERTER;
        String[] projection = conv.getColumns();
        String table = conv.getTableName();
        String selection = servingIds.isEmpty() ? null : buildWhereString(conv.getIdColumnName(), servingIds);
        Cursor c = db.query(table, projection, selection, null, null, null, null);
        List<Serving> resultServings;
        if (c != null) {
            resultServings = cursorToRawServings(c, projection.length);
            c.close();
        } else {
            resultServings = Collections.emptyList();
        }
        db.close();
        return resultServings;
    }

    // ids must not be null
    private String buildWhereString(String idColumnName, Collection<Long> ids) {
        StringBuilder whereString = new StringBuilder();
        List<String> idStrings = new ArrayList<>(ids.size());
        for (Long id : ids) {
            idStrings.add(id.toString());
        }
        if (ids.size() > 1) {
            whereString.append(idColumnName)
                .append(" IN (")
                .append(String.join(",", idStrings))
                .append(")");
        } else if (ids.size() == 1) {
            whereString.append(idColumnName).append(" = ").append(ids.iterator().next());
        }
        return whereString.toString();
    }

    @Override
    public Food getFoodById(Long id) {
        Food f = getRawFoodById(id);
        if (f != null) {
            Map<Long, Food> foodMap = new HashMap<>(1, 1);
            foodMap.put(f.getId(), f);
            applyServingsToRawFoods(foodMap);
        }

        return f;
    }

    @Override
    public Food getFoodByIndexName(String indexName) {
        long id = getFoodIdForIndexName(indexName);
        return (id == MacrosPersistable.NO_ID) ? null : getFoodById(id);
    }

    private long getFoodIdForIndexName(String indexName) {
        String selection = Columns.FoodCol.INDEX_NAME.sqlName();
        String[] selectionArgs = new String[]{indexName};
        Food f = getRawFoodByKey(selection, selectionArgs);
        // a bit redundant since we only need the ID but whatever
        return f == null ? MacrosPersistable.NO_ID : f.getId();
    }

    @Override
    public Meal getMealById(Long id) {
        List<Meal> resultMeals = getMealsById(Collections.singletonList(id));
        return (!resultMeals.isEmpty()) ? resultMeals.get(0) : null;
    }

    @Override
    public List<Meal> getMealsById(@NotNull List<Long> mealIds) {
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

    private List<Long> getFoodIdsForMeals(List<Long> mealIds) {
        Log.d(TAG, "getFoodIdsForMeals(" + mealIds + ")");
        SQLiteDatabase db = helper.getReadableDatabase();
        String[] projection = new String[]{FoodPortion.Column.FOOD_ID.str};
        String selection = buildWhereString(FoodPortion.Column.MEAL_ID.str, mealIds);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(FoodPortion.CONVERTER.getTableName());
        qb.setDistinct(true);

        List<Long> foodIds = new ArrayList<>(0);

        if (mealIds.size() > 0) {
            Cursor c = qb.query(db, projection, selection, null, null, null, null);
            if (c != null) {
                foodIds = new ArrayList<>(c.getCount());
                if (c.getCount() > 0) {
                    for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                        // column at index zero is the ID, since no other results are returned
                        foodIds.add(c.getLong(0));
                    }
                }
                c.close();
            }
        }

        db.close();
        return foodIds;
    }

    // Makes meal objects, filtering by the list of IDs. If mealIds is empty,
    // all meals will be returned.
    private List<Meal> getRawMealsById(@NotNull List<Long> mealIds) {
        SQLiteDatabase db = helper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        String[] projection = Meal.CONVERTER.getColumns();
        queryBuilder.setTables(Meal.CONVERTER.getTableName());
        if (mealIds.size() > 0) {
            queryBuilder.appendWhere(buildWhereString(Meal.CONVERTER.getIdColumnName(), mealIds));
        }
        Cursor c = queryBuilder.query(db, projection, null, null, null, null, null);

        List<Meal> meals;
        if (c != null) {
            meals = cursorToRawMeals(c, projection.length);
            c.close();
        } else {
            meals = new ArrayList<>();
        }

        return meals;
    }

    /*
     * The map must map the meal ID to the (already created) FoodTable objects needed by FoodPortions
     * in that meal.
     */
    private void applyFoodPortionsToRawMeal(Meal meal, Map<Long, Food> foodMap) {
        if (meal == null) {
            return;
        }

        SQLiteDatabase db = helper.getReadableDatabase();

        String table = FoodPortion.CONVERTER.getTableName();
        String[] projection = FoodPortion.CONVERTER.getColumns();
        String selection = FoodPortion.Column.MEAL_ID + " = " + meal.getId();
        Cursor c = db.query(table, projection, selection, null, null, null, null);

        if (c != null) {
            List<FoodPortion> resultPortions = cursorToRawFoodPortions(c, projection.length);
            for (FoodPortion portion : resultPortions) {
                Food foodForPortion = foodMap.get(portion.getFoodId());
                portion.setFood(foodForPortion);
                long servingId = portion.getServingId();
                if (servingId != MacrosPersistable.NO_ID) {
                    Serving serving = foodForPortion.getServingById(servingId);
                    if (serving != null) {
                        portion.setServing(serving);
                    } else {
                        // oh no!
                        throw new IllegalStateException("FoodPortionTable's serving not found in food!");
                    }
                }
                portion.setMeal(meal);
                meal.addFoodPortion(portion);
            }
            c.close();
        }

        db.close();
    }

    @Override
    public List<Meal> getMealsForDay(DateStamp day) {
        List<Long> mealIds = getMealIdsForDay(day);
        return getMealsById(mealIds);
    }

    @Override
    public List<Long> getMealIdsForDay(@NotNull DateStamp day) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String table = Meal.CONVERTER.getTableName();
        String[] projection = new String[]{Meal.CONVERTER.getIdColumnName()};

        String selection =
            "DATE(" + Meal.Column.DAY + ") = DATE ( ? )";
        String[] selectionArgs = {day.toString()};
        Cursor c = db.query(table, projection, selection, selectionArgs, null, null, null);

        List<Long> mealIds;
        if (c != null) {
            mealIds = new ArrayList<>(c.getCount());
            if (c.getCount() > 0) {
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    // column at index zero is the ID, since no other results are returned
                    mealIds.add(c.getLong(0));
                }
            }
            c.close();
        } else {
            mealIds = new ArrayList<>();
        }

        db.close();
        return mealIds;
    }

    private Food getRawFoodById(@NotNull Long id) throws SQLException {
        return getRawObjectByKey(Tables.FoodTable.getInstance(), Columns.FoodCol.ID, id);
    }

    private <M extends MacrosPersistable, T> M getRawObjectByKey(Table<M> t, Column<M, T> keyCol, T key) throws SQLException {
        ColumnData<M> container = new ColumnData<>(t);
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(SqlUtils.selectTemplate(t, t.columns(), keyCol))) {
            SqlUtils.bindObjects(p, key);
            try (ResultSet rs = p.executeQuery()) {
                for (Column<M, ?> col : t.columns()) {
                    SqlUtils.addRawToColumnData(container, col, rs.getObject(col.sqlName()));
                }
            }
        }
        return t.construct(container, true);
    }

    private List<Long> getServingIdsForFoods(@NotNull Long foodId) {
        return getServingIdsForFoods(Collections.singletonList(foodId));
    }

    @Override
    public <M extends MacrosPersistable<M>> void saveObjects(@NotNull List<M> objects) {
        for (M object : objects) {
            if (object != null) {
                saveObject(object);
            }
        }
    }

    @Override
    public <M extends MacrosPersistable<M>> boolean saveObject(@NotNull M o) {
        Table<M> t = o.getTable();
        Long id = o.getId();
        try (Connection c = getConnection()) {
            if (id == MacrosPersistable.NO_ID) {
                return insert(c, t, o.getAllData(), false) == 1;
            } else {
                return update(c, t, o.getAllData(), t.getIdColumn()) == 1;
            }
        } catch (SQLException e) {
            err.println(e);
            return false;
        }
    }
}
