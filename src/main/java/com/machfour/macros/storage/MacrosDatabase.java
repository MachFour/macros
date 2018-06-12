package com.machfour.macros.storage;

import com.machfour.macros.core.*;
import com.machfour.macros.objects.*;
import com.machfour.macros.storage.StorageUtils;
import com.machfour.macros.util.DateStamp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

import static com.machfour.macros.storage.StorageUtils.toList;

public abstract class MacrosDatabase implements MacrosDataSource {
    protected static String createStatements(List<String> sqlFileLines) {
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

    // fkColumns by definition contains only the foreign key columns
    protected static <M extends MacrosPersistable<M>> boolean fkIdsPresent(M object) {
        boolean idsPresent = true;
        for (Column.Fk<M, ?, ?> fkCol : object.getTable().fkColumns()) {
            if (fkCol.getParentColumn().equals(fkCol.getParentTable().getIdColumn())) {
                idsPresent &= object.hasData(fkCol) && !object.getData(fkCol).equals(MacrosPersistable.NO_ID);
            }
        }
        return idsPresent;
    }

    protected abstract <M extends MacrosPersistable> int deleteById(Long id, Table<M> t) throws SQLException;

    @NotNull
    protected abstract <M extends MacrosPersistable> List<Long> prefixSearch(
            Table<M> t, List<Column<M, String>> cols, String keyword) throws SQLException;

    protected abstract <M, I, J> Map<I, J> selectColumnMap(Table<M> t, Column<M, I> keyColumn, Column<M, J> valueColumn, Set<I> keys) throws SQLException;

    // does SELECT (selectColumn) FROM (t) WHERE (whereColumn) = (whereValue)
    // or SELECT (selectColumn) FROM (t) WHERE (whereColumn) IN (whereValue1, whereValue2, ...)
    protected abstract <M, I, J> List<I> selectColumn(
            Table<M> t, Column<M, I> selectColumn, Column<M, J> whereColumn, List<J> whereValues, boolean distinct) throws SQLException;

    // Retrives an object by a key column, and constructs it without any FK object instances.
    // Returns null if no row in the corresponding table had a key with the given value
    protected abstract <M, J> List<M> getRawObjectsByKeys(Table<M> t, Column<M, J> keyCol, List<J> keys) throws SQLException;

    protected abstract <M extends MacrosPersistable<M>> int insertObjectData(@NotNull List<ColumnData<M>> objectData, boolean withId) throws SQLException;

    // Note that if the id is not found in the database, nothing will be inserted
    public abstract <M extends MacrosPersistable<M>> int updateObjects(@NotNull List<M> objects) throws SQLException;

    protected abstract <M extends MacrosPersistable<M>> boolean idExistsInTable(Table<M> table, long id) throws SQLException;

    protected abstract <M extends MacrosPersistable<M>> Map<Long, Boolean> idsExistInTable(Table<M> table, List<Long> ids) throws SQLException;

    public boolean deleteIfExists(String dbFile) throws IOException {
        Path dbPath = Paths.get(dbFile);
        if (Files.exists(dbPath)) {
            Files.delete(dbPath);
            return true;
        } else {
            return false;
        }
    }

    public <M extends MacrosPersistable<M>> int deleteObject(@NotNull M o) throws SQLException {
        return deleteById(o.getId(), o.getTable());
    }

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

    @NotNull
    public List<Long> foodSearch(String keyword) throws SQLException {
        List<Column<Food, String>> columns = Arrays.asList(
              Schema.FoodTable.INDEX_NAME
            , Schema.FoodTable.NAME
            , Schema.FoodTable.VARIETY
            , Schema.FoodTable.BRAND
        );
        return prefixSearch(Schema.FoodTable.instance(), columns, keyword);
    }

    public List<Food> getAllFoods() throws SQLException {
        List<Serving> allServings = getRawObjectsByIds(Schema.ServingTable.instance(), new ArrayList<>());
        // TODO
        return getFoodsById(new ArrayList<>(0));
    }

    @Nullable
    public Food getFoodByIndexName(String indexName) throws SQLException {
        List<Food> resultFood = getFoodsByIndexName(toList(indexName));
        // a bit redundant since we only need the ID but whatever
        return resultFood.isEmpty() ? null : resultFood.get(0);
    }

    @Nullable
    public Food getFoodById(Long id) throws SQLException {
        List<Food> resultFood = getFoodsById(toList(id));
        return resultFood.isEmpty() ? null : resultFood.get(0);
    }

    public List<Food> getFoodsById(@NotNull List<Long> foodIds) throws SQLException {
        List<Food> foods = getRawObjectsByIds(Schema.FoodTable.instance(), foodIds);
        return processRawFoods(foods);
    }

    public List<Food> getFoodsByIndexName(@NotNull List<String> indexNames) throws SQLException {
        List<Food> foods = getRawObjectsByKeys(Schema.FoodTable.instance(), Schema.FoodTable.INDEX_NAME, indexNames);
        return processRawFoods(foods);
    }

    private List<Food> processRawFoods(List<Food> foods) throws SQLException {
        Map<Long, Food> idMap = StorageUtils.makeIdMap(foods);
        if (idMap.isEmpty()) {
            return Collections.emptyList();
        }
        applyServingsToRawFoods(idMap);
        applyNutritionDataToRawFoods(idMap);
        // TODO  FoodCategory, Ingredients
        //applyIngredientsToRawFood();
        //applyFoodCategoryToRawFood();
        return foods;

    }

    private void applyServingsToRawFoods(Map<Long, Food> foodMap) throws SQLException {
        assert !foodMap.isEmpty();
        List<Long> foodIds = new ArrayList<>(foodMap.keySet());
        List<Long> servingIds = selectColumn(Schema.ServingTable.instance(), Schema.ServingTable.ID, Schema.ServingTable.FOOD_ID, foodIds);
        if (servingIds.isEmpty()) {
            // no servings
            return;
        }
        List<Serving> servings = getRawObjectsByKeys(Schema.ServingTable.instance(), Schema.ServingTable.ID, servingIds);
        for (Serving s : servings) {
            // QtyUnit setup
            QtyUnit unit = QtyUnit.fromAbbreviation(s.getQuantityUnitAbbr());
            assert (unit != null) : "No quantity unit with the given abbreviation was found";
            s.setQtyUnit(unit);
            // this query should never fail, due to database constraints
            Food f = foodMap.get(s.getFoodId());
            assert (f != null);
            s.setFood(f);
            f.addServing(s);
        }
    }

    private void applyNutritionDataToRawFoods(Map<Long, Food> foodMap) throws SQLException {
        assert !foodMap.isEmpty();
        List<Long> foodIds = new ArrayList<>(foodMap.keySet());
        List<Long> nutritionDataIds = selectColumn(Schema.NutritionDataTable.instance(), Schema.NutritionDataTable.ID, Schema.NutritionDataTable.FOOD_ID, foodIds);
        if (nutritionDataIds.isEmpty()) {
            // no servings
            return;
        }
        List<NutritionData> ndObjects = getRawObjectsByKeys(Schema.NutritionDataTable.instance(), Schema.NutritionDataTable.ID, nutritionDataIds);
        for (NutritionData nd : ndObjects) {
            // this lookup should never fail, due to database constraints
            Food f = foodMap.get(nd.getFoodId());
            assert f != null;
            nd.setFood(f);
            f.setNutritionData(nd);
        }
    }

    private <M, I, J> List<I> selectColumn(
            Table<M> t, Column<M, I> selectColumn, Column<M, J> whereColumn, J whereValue) throws SQLException {
        return selectColumn(t, selectColumn, whereColumn, toList(whereValue), false);
    }

    private <M, I, J> List<I> selectColumn(
            Table<M> t, Column<M, I> selectColumn, Column<M, J> whereColumn, List<J> whereValues) throws SQLException {
        return selectColumn(t, selectColumn, whereColumn, whereValues, false);
    }

    public Meal getMealById(Long id) throws SQLException {
        List<Meal> resultMeals = getMealsById(Collections.singletonList(id));
        return (!resultMeals.isEmpty()) ? resultMeals.get(0) : null;
    }

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
        return selectColumn(Schema.FoodPortionTable.instance(), Schema.FoodPortionTable.FOOD_ID, Schema.FoodPortionTable.MEAL_ID, mealIds, true);
    }

    // Makes meal objects, filtering by the list of IDs. If mealIds is empty,
    // all meals will be returned.
    private List<Meal> getRawMealsById(@NotNull List<Long> mealIds) throws SQLException {
        return getRawObjectsByKeys(Schema.MealTable.instance(), Schema.MealTable.ID, mealIds);
    }

    /*
     * The map must map the meal ID to the (already created) FoodTable objects needed by FoodPortions
     * in that meal.
     */
    private void applyFoodPortionsToRawMeal(Meal meal, Map<Long, Food> foodMap) throws SQLException {
        List<Long> foodPortionIds = selectColumn(Schema.FoodPortionTable.instance(), Schema.FoodPortionTable.ID, Schema.FoodPortionTable.MEAL_ID, meal.getId());
        if (!foodPortionIds.isEmpty()) {
            List<FoodPortion> foodPortions = getRawObjectsByIds(Schema.FoodPortionTable.instance(), foodPortionIds);
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

    public List<Meal> getMealsForDay(DateStamp day) throws SQLException {
        List<Long> mealIds = getMealIdsForDay(day);
        return getMealsById(mealIds);
    }

    public List<Long> getMealIdsForDay(@NotNull DateStamp day) throws SQLException {
        return selectColumn(Schema.MealTable.instance(), Schema.MealTable.ID, Schema.MealTable.DAY, Collections.singletonList(day));
        // TODO: need "DATE(" + Meal.Column.DAY + ") = DATE ( ? )"; ???
    }

    private <M extends MacrosPersistable> M getRawObjectById(Table<M> t, Long id) throws SQLException {
        return getRawObjectByKey(t, t.getIdColumn(), id);
    }

    private <M extends MacrosPersistable> List<M> getRawObjectsByIds(Table<M> t, List<Long> ids) throws SQLException {
        return getRawObjectsByKeys(t, t.getIdColumn(), ids);
    }

    private <M, J> M getRawObjectByKey(Table<M> t, Column<M, J> keyCol, J key) throws SQLException {
        List<M> returned = getRawObjectsByKeys(t, keyCol, Collections.singletonList(key));
        assert returned.size() <= 1;
        return returned.isEmpty() ? null : returned.get(0);
    }

    public <M extends MacrosPersistable<M>> int insertObjects(@NotNull List<M> objects, boolean withId) throws SQLException {
        List<ColumnData<M>> objectData = new ArrayList<>(objects.size());
        for (M object: objects)  {
            objectData.add(object.getAllData());
        }
        return insertObjectData(objectData, withId);
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

    // only Storage classes should know about these two methods
    <M extends MacrosPersistable<M>> List<M> completeForeignKeys(List<M> objects, Column.Fk<M, ?, ?> fk) throws SQLException {
        return completeForeignKeys(objects, toList(fk));
    }

    <M extends MacrosPersistable<M>> List<M> completeForeignKeys(List<M> objects, List<Column.Fk<M, ?, ?>> which) throws SQLException {
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

    public <M extends MacrosPersistable<M>> int saveObject(@NotNull M o) throws SQLException {
        return saveObjects(toList(o), o.getObjectSource());
    }
}
