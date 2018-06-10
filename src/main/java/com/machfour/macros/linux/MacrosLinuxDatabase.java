package com.machfour.macros.linux;

import com.machfour.macros.core.*;
import com.machfour.macros.core.Schema.*;
import com.machfour.macros.objects.*;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.storage.StorageUtils;
import com.machfour.macros.util.DateStamp;
import org.jetbrains.annotations.NotNull;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

import static com.machfour.macros.storage.StorageUtils.toList;

// data source provided by Xerial library

public class MacrosLinuxDatabase implements MacrosDataSource {
    // singleton
    private static MacrosLinuxDatabase INSTANCE;
    private final SQLiteDataSource dataSource;

    private MacrosLinuxDatabase(String dbFile) {
        Path dbPath = Paths.get(dbFile);
        dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + dbPath.toAbsolutePath());
        SQLiteConfig config = new SQLiteConfig();
        config.enableRecursiveTriggers(true);
        config.enforceForeignKeys(true);
        dataSource.setConfig(config);
    }

    public static MacrosLinuxDatabase getInstance() {
        return getInstance(Config.DB_LOCATION);
    }

    public static MacrosLinuxDatabase getInstance(String dbFile) {
        if (INSTANCE == null) {
            INSTANCE = new MacrosLinuxDatabase(dbFile);
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

    // fkColumns by definition contains only the foreign key columns
    private static <M extends MacrosPersistable<M>> boolean fkIdsPresent(M object) {
        boolean idsPresent = true;
        for (Column.Fk<M, ?, ?> fkCol : object.getTable().fkColumns()) {
            if (fkCol.getParentColumn().equals(fkCol.getParentTable().getIdColumn())) {
                idsPresent &= object.hasData(fkCol) && !object.getData(fkCol).equals(MacrosPersistable.NO_ID);
            }
        }
        return idsPresent;
    }

    public boolean deleteIfExists(String dbFile) throws IOException {
        Path dbPath = Paths.get(dbFile);
        if (Files.exists(dbPath)) {
            Files.delete(dbPath);
            return true;
        } else {
            return false;
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void initDb() throws SQLException, IOException {
        try (Connection c = getConnection()) {
            List<String> initStatements = new ArrayList<>(3);
            initStatements.add(createStatements(Files.readAllLines(Config.INIT_SQL)));
            initStatements.add(createStatements(Files.readAllLines(Config.TRIG_SQL)));
            initStatements.add(createStatements(Files.readAllLines(Config.DATA_SQL)));
            runStatements(c, initStatements);
        }
    }

    @Override
    public <M extends MacrosPersistable<M>> int deleteObject(@NotNull M o) throws SQLException {
        return deleteById(o.getId(), o.getTable());
    }
    // TODO select by secondary key

    private <M extends MacrosPersistable> int deleteById(Long id, Table<M> t) throws SQLException {
        try (Connection c = getConnection();
                PreparedStatement s = c.prepareStatement(StorageUtils.deleteTemplate(t, t.getIdColumn()))) {
            StorageUtils.bindObjects(s, toList(id));
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
        List<Column<Food, String>> columns = Arrays.asList(
              FoodTable.INDEX_NAME
            , FoodTable.NAME
            , FoodTable.VARIETY
            , FoodTable.BRAND
        );
        return prefixSearch(FoodTable.instance(), columns, keyword);
    }

    public <M extends MacrosPersistable> List<Long> prefixSearch(
            Table<M> t, List<Column<M, String>> cols, String keyword) throws SQLException {
        List<Long> resultList = new ArrayList<>(0);
        if (!cols.isEmpty()) {
            // TODO copy-pasted from SelectColumn... probably needs refactoring
            try (Connection c = getConnection();
                 PreparedStatement p = c.prepareStatement(StorageUtils.selectLikeTemplate(t, t.getIdColumn(), cols))) {
                // have to append the percent sign for LIKE globbing to the actual argument string
                String keywordGlob = keyword + "%";
                List<String> bindString = Collections.nCopies(cols.size(), keywordGlob);
                StorageUtils.bindObjects(p, bindString);
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
        Map<Long, Food> idMap = StorageUtils.makeIdMap(foods);
        applyServingsToRawFoods(idMap);
        applyNutritionDataToRawFoods(idMap);
        // TODO  FoodCategory, Ingredients
        //applyIngredientsToRawFood();
        //applyFoodCategoryToRawFood();
        return foods;
    }

    private void applyServingsToRawFoods(Map<Long, Food> foodMap) throws SQLException {
        List<Long> foodIds = new ArrayList<>(foodMap.keySet());
        List<Long> servingIds = selectColumn(ServingTable.instance(), ServingTable.ID, ServingTable.FOOD_ID, foodIds);
        if (servingIds.isEmpty()) {
            // no servings
            return;
        }
        List<Serving> servings = getRawObjectsByKeys(ServingTable.instance(), ServingTable.ID, servingIds);
        for (Serving s : servings) {
            // QuantityUnit setup
            QuantityUnit unit = QuantityUnit.fromAbbreviation(s.getQuantityUnitAbbr());
            assert (unit != null) : "No quantity unit with the given abbreviation was found";
            s.setQuantityUnit(unit);
            // this query should never fail, due to database constraints
            Food f = foodMap.get(s.getFoodId());
            assert (f != null);
            s.setFood(f);
            f.addServing(s);
        }
    }

    private void applyNutritionDataToRawFoods(Map<Long, Food> foodMap) throws SQLException {
        List<Long> foodIds = new ArrayList<>(foodMap.keySet());
        List<Long> nutritionDataIds = selectColumn(NutritionDataTable.instance(), NutritionDataTable.ID, NutritionDataTable.FOOD_ID, foodIds);
        if (nutritionDataIds.isEmpty()) {
            // no servings
            return;
        }
        List<NutritionData> ndObjects = getRawObjectsByKeys(NutritionDataTable.instance(), NutritionDataTable.ID, nutritionDataIds);
        for (NutritionData nd : ndObjects) {
            // this lookup should never fail, due to database constraints
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

    private <M, I, J> List<I> selectColumn(
            Table<M> t, Column<M, I> selectColumn, Column<M, J> whereColumn, J whereValue) throws SQLException {
        return selectColumn(t, selectColumn, whereColumn, toList(whereValue), false);
    }

    private <M, I, J> List<I> selectColumn(
            Table<M> t, Column<M, I> selectColumn, Column<M, J> whereColumn, List<J> whereValues) throws SQLException {
        return selectColumn(t, selectColumn, whereColumn, whereValues, false);
    }
    private <M, I, J> Map<I, J> selectColumnMap(Table<M> t, Column<M, I> keyColumn, Column<M, J> valueColumn, Set<I> keys) throws SQLException {
        Map<I, J> resultMap = new HashMap<>();
        // for batch queries
        //List<Column<M, ?>> selectColumns = Arrays.asList(keyColumn, valueColumn);
        List<Column<M, ?>> selectColumns = Collections.singletonList(valueColumn);
        try (Connection c = getConnection();
             // should be distinct by default: assert keyColumn.isUnique();
             PreparedStatement p = c.prepareStatement(StorageUtils.selectTemplate(t, selectColumns, keyColumn, 1, false))) {
            // do queries one by one so we don't send a huge number of parameters at once
            for (I key : keys) {
                StorageUtils.bindObjects(p, Collections.singletonList(key));
                try (ResultSet rs = p.executeQuery()) {
                    for (rs.next(); !rs.isAfterLast(); rs.next()) {
                        //I key = keyColumn.getType().fromRaw(rs.getObject(keyColumn.sqlName()));
                        J value = valueColumn.getType().fromRaw(rs.getObject(valueColumn.sqlName()));
                        assert !resultMap.containsKey(key) : "Two rows in the DB contained the same data in the key column!";
                        resultMap.put(key, value);
                    }
                }
                p.clearParameters();
            }
        }
        return resultMap;
    }

    // does SELECT (selectColumn) FROM (t) WHERE (whereColumn) = (whereValue)
    // or SELECT (selectColumn) FROM (t) WHERE (whereColumn) IN (whereValue1, whereValue2, ...)
    private <M, I, J> List<I> selectColumn(
            Table<M> t, Column<M, I> selectColumn, Column<M, J> whereColumn, List<J> whereValues, boolean distinct) throws SQLException {
        List<I> resultList = new ArrayList<>(0);
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(StorageUtils.selectTemplate(t, selectColumn, whereColumn, whereValues.size(), distinct))) {
            StorageUtils.bindObjects(p, whereValues);
            try (ResultSet rs = p.executeQuery()) {
                for (rs.next(); !rs.isAfterLast(); rs.next()) {
                    Object resultValue = rs.getObject(selectColumn.sqlName());
                    resultList.add(selectColumn.getType().fromRaw(resultValue));
                }
            }
        }
        return resultList;
    }

    @Override
    public Food getFoodByIndexName(String indexName) throws SQLException {
        List<Long> idList = selectColumn(FoodTable.instance(), FoodTable.ID, FoodTable.INDEX_NAME, indexName);
        // a bit redundant since we only need the ID but whatever
        return idList.isEmpty() ? null : getFoodById(idList.get(0));
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
        return selectColumn(FoodPortionTable.instance(), FoodPortionTable.FOOD_ID, FoodPortionTable.MEAL_ID, mealIds, true);
    }

    // Makes meal objects, filtering by the list of IDs. If mealIds is empty,
    // all meals will be returned.
    private List<Meal> getRawMealsById(@NotNull List<Long> mealIds) throws SQLException {
        return getRawObjectsByKeys(MealTable.instance(), MealTable.ID, mealIds);
    }

    /*
     * The map must map the meal ID to the (already created) FoodTable objects needed by FoodPortions
     * in that meal.
     */
    private void applyFoodPortionsToRawMeal(Meal meal, Map<Long, Food> foodMap) throws SQLException {
        List<Long> foodPortionIds = selectColumn(FoodPortionTable.instance(), FoodPortionTable.ID, FoodPortionTable.MEAL_ID, meal.getId());
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
        return selectColumn(MealTable.instance(), MealTable.ID, MealTable.DAY, Collections.singletonList(day));
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
    private <M, J> List<M> getRawObjectsByKeys(Table<M> t, Column<M, J> keyCol, List<J> keys) throws SQLException {
        // if the list of keys is empty, every row will be returned
        assert !keys.isEmpty() : "List of keys is empty";
        List<M> objects = new ArrayList<>(keys.size());
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(StorageUtils.selectTemplate(t, t.columns(), keyCol, keys.size(), false))) {
            StorageUtils.bindObjects(p, keys);
            try (ResultSet rs = p.executeQuery()) {
                for (rs.next(); !rs.isAfterLast(); rs.next()) {
                    ColumnData<M> data = new ColumnData<>(t);
                    for (Column<M, ?> col : t.columns()) {
                        data.putFromRaw(col, rs.getObject(col.sqlName()));
                    }
                    objects.add(t.getFactory().construct(data, ObjectSource.DATABASE));
                }
            }
        }
        return objects;
    }

    private <M, J> M getRawObjectByKey(Table<M> t, Column<M, J> keyCol, J key) throws SQLException {
        List<M> returned = getRawObjectsByKeys(t, keyCol, Collections.singletonList(key));
        assert returned.size() <= 1;
        return returned.isEmpty() ? null : returned.get(0);
    }

    @Override
    public <M extends MacrosPersistable<M>> int insertObjects(@NotNull List<M> objects, boolean withId) throws SQLException {
        List<ColumnData<M>> objectData = new ArrayList<>(objects.size());
        for (M object: objects)  {
            objectData.add(object.getAllData());
        }
        return insertObjectData(objectData, withId);
    }

    private <M extends MacrosPersistable<M>> int insertObjectData(@NotNull List<ColumnData<M>> objectData, boolean withId) throws SQLException {
        if (objectData.isEmpty()) {
            return 0;
        }
        int saved = 0;
        Table<M> table = objectData.get(0).getTable();
        List<Column<M, ?>> columnsToInsert = table.columns();
        if (!withId) {
            columnsToInsert = new ArrayList<>(table.columns());
            columnsToInsert.remove(table.getIdColumn());
        } // else inserting for the first time, but it has an ID that we want to keep intact
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            String statement = StorageUtils.insertTemplate(table, columnsToInsert);
            try (PreparedStatement p = c.prepareStatement(statement)) {
                for (ColumnData<M> row : objectData) {
                    StorageUtils.bindData(p, row, columnsToInsert);
                    saved += p.executeUpdate();
                    p.clearParameters();
                }
            }
            c.commit();
            c.setAutoCommit(true);
        }
        return saved;
    }

    // wildcard capture helper for natural key column type
    private <M extends MacrosPersistable<M>, J, N, I> Map<I, J> completeFkIdColHelper(
            Column.Fk<M, J, N> fkColumn, Column<N, I> parentNaturalKeyCol, List<ColumnData<N>> data) throws SQLException {
        assert (parentNaturalKeyCol.isUnique());
        Set<I> uniqueColumnValues = new HashSet<>(data.size());
        for (ColumnData<N> cd : data) {
            uniqueColumnValues.add(cd.get(parentNaturalKeyCol));
        }
        return selectColumnMap(fkColumn.getParentTable(), parentNaturalKeyCol, fkColumn.getParentColumn(), uniqueColumnValues);
    }

    // wildcard capture helper for parent unique column type
    private <M extends MacrosPersistable<M>, J, N> List<M> completeFkCol(List<M> objects, Column.Fk<M, J, N> fkCol) throws SQLException {
        List<M> completedObjects = new ArrayList<>(objects.size());
        List<ColumnData<N>> naturalKeyData = new ArrayList<>(objects.size());
        for (M object : objects) {
            assert object.getObjectSource() == ObjectSource.IMPORT : "Object is not from import";
            assert !object.getFkNaturalKeyMap().isEmpty() : "Object has no FK data maps";
            ColumnData<N> objectNkData = object.getFkParentNaturalKey(fkCol);
            assert objectNkData != null : "Natural key data was null";
            naturalKeyData.add(objectNkData);
        }
        Column<N, ?> parentNaturalKeyCol = fkCol.getParentTable().getNaturalKeyColumn();
        assert (parentNaturalKeyCol != null) : "Table " + fkCol.getParentTable().name() + " has no natural key defined";
        Map<?, J> uniqueKeyToFkParent = completeFkIdColHelper(fkCol, parentNaturalKeyCol, naturalKeyData);
        for (M object : objects) {
            ColumnData<M> newData = object.getAllData().copy();
            // TODO might be able to remove one level of indirection here because the ParentUniqueColData only contains parentNaturalKeyCol
            newData.put(fkCol, uniqueKeyToFkParent.get(object.getFkParentNaturalKey(fkCol).get(parentNaturalKeyCol)));
            M newObject = object.getTable().construct(newData, object.getObjectSource());
            // copy over old FK data to new object
            newObject.copyFkNaturalKeyMap(object);
            completedObjects.add(newObject);
        }
        return completedObjects;
    }

    public <M extends MacrosPersistable<M>> List<M> completeForeignKeys(List<M> objects, Column.Fk<M, ?, ?> fk) throws SQLException {
        return completeForeignKeys(objects, toList(fk));
    }

    public <M extends MacrosPersistable<M>> List<M> completeForeignKeys(List<M> objects, List<Column.Fk<M, ?, ?>> which) throws SQLException {
        List<M> partiallyCompletedObjects = objects;
        List<M> completedObjects = new ArrayList<>(objects.size());
        if (objects.isEmpty()) {
            return completedObjects;
        }
        Factory<M> factory = objects.get(0).getFactory();
        // cycle through the FK columns.
        for (Column.Fk<M, ?, ?> fkCol: which) {
            partiallyCompletedObjects = completeFkCol(partiallyCompletedObjects, fkCol);
        }
        // Check everything's fine and change source to ObjectSource.IMPORT_FK_PRESENT
        for (M object : partiallyCompletedObjects) {
            assert fkIdsPresent(object);
            completedObjects.add(factory.construct(object.getAllData(), object.getObjectSource()));
        }
        return completedObjects;
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
            try (PreparedStatement p = c.prepareStatement(StorageUtils.updateTemplate(table, table.columns(), table.getIdColumn()))) {
                for (M object : objects) {
                    StorageUtils.bindData(p, object.getAllData(), table.columns(), object.getId());
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
                PreparedStatement p = c.prepareStatement(StorageUtils.deleteTemplate(t))) {
            return p.executeUpdate();
        }
    }

    private <M extends MacrosPersistable<M>> boolean idExistsInTable(Table<M> table, long id) throws SQLException {
        String idCol = table.getIdColumn().sqlName();
        String query = "SELECT COUNT(" + idCol + ") AS count FROM " + table.name() + " WHERE " + idCol + " = " + id;
        try (Connection c = getConnection()) {
            try (Statement s = c.createStatement()) {
                ResultSet rs = s.executeQuery(query);
                rs.next();
                return rs.getInt("count") == 1;
            }
        }
    }
    private <M extends MacrosPersistable<M>> Map<Long, Boolean> idsExistInTable(Table<M> table, List<Long> ids) throws SQLException {
        Column<M, Long> idCol = table.getIdColumn();
        Map<Long, Boolean> idMap = new HashMap<>(ids.size(), 1);
        try (Connection c = getConnection()) {
            try (PreparedStatement p = c.prepareStatement(StorageUtils.selectTemplate(table, idCol, idCol, ids.size()))) {
                StorageUtils.bindObjects(p, ids);
                ResultSet rs = p.executeQuery();
                for (rs.next(); !rs.isAfterLast(); rs.next()) {
                    Long id = rs.getLong(idCol.sqlName());
                    idMap.put(id, true);
                }
                rs.next();
            }
        }
        // check for missing IDs
        for (Long id : ids) {
            if (!idMap.keySet().contains(id)) {
                idMap.put(id, false);
            }
        }
        return idMap;
    }

    private <M extends MacrosPersistable<M>> boolean isInDatabase(@NotNull M o) throws SQLException {
        if (o.getId() != MacrosPersistable.NO_ID) {
            return idExistsInTable(o.getTable(), o.getId());
        } else {
            List<Column<M, ?>> secondaryKey = o.getTable().getSecondaryKeyCols();
            if (secondaryKey.isEmpty()) {
                // no way to know except by ID...
            }
            // TODO
            return false;
        }
    }

    public <M extends MacrosPersistable<M>> int saveObjects(List<M> objects, ObjectSource objectSource) throws SQLException {
        switch (objectSource) {
            case IMPORT:
                // TODO have overwrite mode; split import into new insert and updates
                /* fall through */
            case USER_NEW:
                return insertObjects(objects, false);
            case DB_EDIT:
                return updateObjects(objects);
            case DATABASE:
                // it's unchanged we don't need to do anything at all!
                return 1;
            case RESTORE:
                // will have ID. Assume database has been cleared?
                return insertObjects(objects, true);
            case COMPUTED:
                // don't want to save these ones either
                assert false : "Why save a computed object?";
                return 0;
            default:
                assert (false) : "Unrecognised object source: " + objectSource;
                return 0;
        }
    }

    @Override
    public <M extends MacrosPersistable<M>> int saveObject(@NotNull M o) throws SQLException {
        return saveObjects(toList(o), o.getObjectSource());
    }
}
