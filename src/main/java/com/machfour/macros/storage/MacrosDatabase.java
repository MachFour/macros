package com.machfour.macros.storage;

import com.machfour.macros.core.*;
import com.machfour.macros.objects.*;
import com.machfour.macros.util.DateStamp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

import static com.machfour.macros.core.MacrosUtils.getOrDefault;
import static com.machfour.macros.storage.DatabaseUtils.toList;

public abstract class MacrosDatabase implements MacrosDataSource {

    // fkColumns by definition contains only the foreign key columns
    protected static <M extends MacrosEntity<M>> boolean fkIdsPresent(M object) {
        boolean idsPresent = true;
        for (Column.Fk<M, ?, ?> fkCol : object.getTable().fkColumns()) {
            // if the FK refers to an ID column and it's not nullable, make sure there's a value
            if (fkCol.getParentColumn().equals(fkCol.getParentTable().getIdColumn()) && !fkCol.isNullable()) {
                idsPresent &= object.hasData(fkCol) && !object.getData(fkCol).equals(MacrosEntity.NO_ID);
            }
        }
        return idsPresent;
    }

    // caller-managed connection, useful to reduce number of calls to DB
    // caller needs to call closeConnection() after. Use with begin and end transaction
    public abstract void openConnection() throws SQLException;
    public abstract void closeConnection() throws SQLException;

    // By default, database functions will autocommit.
    // These functions can be used to temporarily disable autocommit and are useful to group multiple operations together
    public abstract void beginTransaction() throws SQLException;
    public abstract void endTransaction() throws SQLException;


    protected abstract <M> int deleteById(Long id, Table<M> t) throws SQLException;


    @NotNull
    protected <M> List<Long> prefixSearch(
            Table<M> t, List<Column<M, String>> cols, String keyword) throws SQLException {
        return stringSearch(t, cols, keyword, false, true);
    }

    @NotNull
    protected <M> List<Long> substringSearch(
            Table<M> t, List<Column<M, String>> cols, String keyword) throws SQLException {
        return stringSearch(t, cols, keyword, true, true);
    }
    @NotNull
    protected <M> List<Long> exactStringSearch(
            Table<M> t, List<Column<M, String>> cols, String keyword) throws SQLException {
        return stringSearch(t, cols, keyword, false, false);
    }


    @NotNull
    protected abstract <M> List<Long> stringSearch(
            Table<M> t, List<Column<M, String>> cols, String keyword, boolean globBefore, boolean globAfter) throws SQLException;

    protected abstract <M, I, J> Map<I, J> selectColumnMap(Table<M> t, Column<M, I> keyColumn, Column<M, J> valueColumn, Set<I> keys) throws SQLException;

    protected static <M, J> void rethrowAsSqlException(Object rawValue, Column<M, J> col) throws SQLException {
        throw new SQLException(String.format("Could not convert value '%s' for column %s.%s (type %s)",
                rawValue, col.getTable(), col.sqlName(), col.getType()));
    }

    // does SELECT (selectColumn) FROM (t) WHERE (whereColumn) = (whereValue)
    // or SELECT (selectColumn) FROM (t) WHERE (whereColumn) IN (whereValue1, whereValue2, ...)
    protected abstract <M, I, J> List<I> selectColumn(
            Table<M> t, Column<M, I> selectColumn, Column<M, J> whereColumn, Collection<J> whereValues, boolean distinct) throws SQLException;

    // does DELETE FROM (t) WHERE (whereColumn) = (whereValue)
    // or DELETE FROM (t) WHERE (whereColumn) IN (whereValue1, whereValue2, ...)
    public abstract <M, J> int deleteByColumn(Table<M> t, Column<M, J> whereColumn, Collection<J> whereValues) throws SQLException;

    // Retrives an object by a key column, and constructs it without any FK object instances.
    // Returns null if no row in the corresponding table had a key with the given value
    // The collection of keys must not be empty; an assertion error is thrown if so
    protected abstract <M, J> Map<J, M> getRawObjectsByKeysNoEmpty(Table<M> t,
            Column<M, J> keyCol, Collection<J> keys) throws SQLException;

    protected abstract <M, J> Map<J, Long> getIdsByKeysNoEmpty(Table<M> t, Column<M, J> keyCol, Collection<J> keys) throws SQLException;

    private <M, J> Map<J, M> getRawObjectsByKeys(Table<M> t, Column<M, J> keyCol, Collection<J> keys) throws SQLException {
        return keys.isEmpty() ? Collections.emptyMap() : getRawObjectsByKeysNoEmpty(t, keyCol, keys);
    }

    private <M, J> Map<J, Long> getIdsFromKeys(Table<M> t, Column<M, J> keyCol, Collection<J> keys) throws SQLException {
        return keys.isEmpty() ? Collections.emptyMap() : getIdsByKeysNoEmpty(t, keyCol, keys);
    }

    // returns map of all objects in table, by ID
    // TODO make protected -- but it's useful for CSV export
    public abstract <M> Map<Long, M> getAllRawObjects(Table<M> t) throws SQLException;

    protected abstract <M extends MacrosEntity<M>> int insertObjectData(@NotNull List<ColumnData<M>> objectData, boolean withId) throws SQLException;

    // Note that if the id is not found in the database, nothing will be inserted
    public abstract <M extends MacrosEntity<M>> int updateObjects(Collection<? extends M> objects) throws SQLException;

    protected abstract <M extends MacrosEntity<M>> boolean idExistsInTable(Table<M> table, long id) throws SQLException;

    protected abstract <M extends MacrosEntity<M>> Map<Long, Boolean> idsExistInTable(Table<M> table, List<Long> ids) throws SQLException;

    public abstract <M> int clearTable(Table<M> t) throws SQLException;

    public boolean deleteIfExists(String dbFile) throws IOException {
        Path dbPath = Paths.get(dbFile);
        if (Files.exists(dbPath)) {
            Files.delete(dbPath);
            return true;
        } else {
            return false;
        }
    }

    public <M extends MacrosEntity<M>> int deleteObject(@NotNull M o) throws SQLException {
        return deleteById(o.getId(), o.getTable());
    }

    // TODO make this the general one
    public <M extends MacrosEntity<M>> int deleteObjects(@NotNull List<M> objects) throws SQLException {
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

    public void saveFoodPortions(@NotNull Meal m) throws SQLException {
        for (FoodPortion fp : m.getFoodPortions()) {
            if (!fp.getObjectSource().equals(ObjectSource.DATABASE)) {
                saveObject(fp);
            }
        }
    }

    public Meal getOrCreateMeal(@NotNull DateStamp day, @NotNull String name) throws SQLException {
        Map<String, Meal> mealsForDay = getMealsForDay(day);
        if (mealsForDay.containsKey(name)) {
            return mealsForDay.get(name);
        } else {
            ColumnData<Meal> newMealData = new ColumnData<>(Meal.table());
            newMealData.put(Schema.MealTable.DAY, day);
            newMealData.put(Schema.MealTable.NAME, name);
            Meal newMeal = Meal.factory().construct(newMealData, ObjectSource.USER_NEW);
            saveObject(newMeal);
            // get it back again, so that it has an ID and stuff
            mealsForDay = getMealsForDay(day);
            assert (mealsForDay.containsKey(name)) : "didn't find saved meal in meals for its day";
            return mealsForDay.get(name);
        }

    }

    @Override
    public @Nullable Meal getCurrentMeal() throws SQLException {
        Map<String, Meal> mealsForDay = getMealsForDay(DateStamp.forCurrentDate());
        if (mealsForDay.isEmpty()) {
            return null;
        } else {
            // most recently modified -> largest modification time -> swap compare order
            return Collections.max(mealsForDay.values(),
                (Meal a, Meal b) -> Long.compare(b.modifyTime(), a.modifyTime()));

        }
    }


    // Searches Index name, name, variety and brand for prefix, then index name for anywhere
    // empty list will be returned if keyword is empty string
    @NotNull
    public Set<Long> foodSearch(@NotNull String keyword) throws SQLException {
        List<Column<Food, String>> columns = Arrays.asList(
              Schema.FoodTable.INDEX_NAME
            , Schema.FoodTable.NAME
            , Schema.FoodTable.VARIETY
            , Schema.FoodTable.BRAND
        );
        // match any column prefix
        List<Long> prefixResults = prefixSearch(Food.table(), columns, keyword);
        // or anywhere in index name
        List<Long> indexResults = substringSearch(Food.table(), toList(Schema.FoodTable.INDEX_NAME), keyword);
        Set<Long> results = new LinkedHashSet<>(prefixResults.size() + indexResults.size());
        results.addAll(prefixResults);
        results.addAll(indexResults);

        return results;

    }

    public Map<String, FoodCategory> getAllFoodCategories() throws SQLException {
        Map<Long, FoodCategory> categoriesById = getAllRawObjects(FoodCategory.table());
        Map<String, FoodCategory> categoriesByString = new LinkedHashMap<>(categoriesById.size());
        for (FoodCategory c : categoriesById.values()) {
            categoriesByString.put(c.getName(), c);
        }
        return categoriesByString;
    }

    // The proper way to get all fooods
    public List<Food> getAllFoods() throws SQLException {
        Map<Long, Food> allFoods = getAllRawObjects(Food.table());
        Map<Long, Serving> allServings = getAllRawObjects(Serving.table());
        Map<Long, NutritionData> allNutritionData = getAllRawObjects(NutritionData.table());
        Map<String, FoodCategory> allFoodCategories = getAllFoodCategories();
        Map<Long, Ingredient> allIngredients = getAllRawObjects(Ingredient.table());
        processRawIngredients(allIngredients);
        processRawFoodMap(allFoods, allServings, allNutritionData, allIngredients, allFoodCategories);
        return new ArrayList<>(allFoods.values());
    }

    @Nullable
    public Food getFoodByIndexName(@NotNull String indexName) throws SQLException {
        Map<String, Food> resultFood = getFoodsByIndexName(toList(indexName));
        return MacrosUtils.getOrDefault(resultFood, indexName, null);
    }

    @Nullable
    public Food getFoodById(@NotNull Long id) throws SQLException {
        Map<Long, Food> resultFood = getFoodsById(toList(id));
        return MacrosUtils.getOrDefault(resultFood, id, null);
    }

    @Override
    public Map<Long, Food> getFoodsById(@NotNull Collection<Long> foodIds) throws SQLException {
        Map<Long, Food> foods = getRawObjectsByIds(Food.table(), foodIds);
        processRawFoodMap(foods);
        return foods;
    }

    public Map<Long, Serving> getServingsById(@NotNull Collection<Long> servingIds) throws SQLException {
        return getRawObjectsByIds(Serving.table(), servingIds);
    }

    @Override
    public Map<String, Long> getFoodIdsByIndexName(@NotNull Collection<String> indexNames) throws SQLException {
        return getIdsFromKeys(Food.table(), Schema.FoodTable.INDEX_NAME, indexNames);
    }

    /*
     * Constructs full food objects by their index name
     * Returns a map of index name to food object
     */
    @Override
    public Map<String, Food> getFoodsByIndexName(@NotNull Collection<String> indexNames) throws SQLException {
        Map<String, Food> foods = getRawObjectsByKeys(Schema.FoodTable.instance(), Schema.FoodTable.INDEX_NAME, indexNames);
        // TODO hmm this is kind of inefficient
        Map<Long, Food> idMap = DatabaseUtils.makeIdMap(foods.values());
        processRawFoodMap(idMap);
        return foods;
    }

    private void processRawIngredients(Map<Long, Ingredient> ingredientMap) throws SQLException {
        List<Long> foodIds = new ArrayList<>(ingredientMap.size());
        List<Long> servingIds = new ArrayList<>(ingredientMap.size());
        for (Ingredient i : ingredientMap.values()) {
            foodIds.add(i.getIngredientFoodId());
            if (i.getServingId() != null) {
                servingIds.add(i.getServingId());
            }
        }
        // XXX make sure this doesn't loop infinitely if two composite foods contain each other as ingredients
        // (or potentially via a longer chain -- A contains B, B contains C, C contains A)
        Map<Long, Food> ingredientFoods = getFoodsById(foodIds);
        Map<Long, Serving> ingredientServings = getServingsById(servingIds);

        for (Ingredient i : ingredientMap.values()) {
            // applyFoodsToRawIngredients(ingredients, servings
            Food f = ingredientFoods.get(i.getIngredientFoodId());
            i.setIngredientFood(f);
            // applyServingsToRawIngredients(ingredients, servings)
            if (i.getServingId() != null) {
                Serving s = ingredientServings.get(i.getServingId());
                i.setServing(s);
            }
        }
    }

    private void processRawFoodMap(Map<Long, Food> foods, Map<Long, Serving> servings,
            Map<Long, NutritionData> nutritionData, Map<Long, Ingredient> ingredients,
            Map<String, FoodCategory> categories) {
        applyServingsToRawFoods(foods, servings);
        applyNutritionDataToRawFoods(foods, nutritionData);
        applyIngredientsToRawFoods(foods, ingredients);
        applyFoodCategoriesToRawFoods(foods, categories);
    }
    // foodMap is a map of food IDs to the raw (i.e. unlinked) object created from the database
    private void processRawFoodMap(Map<Long, Food> foodMap) throws SQLException {
        if (!foodMap.isEmpty()) {
            //Map<Long, Serving> servings = getRawServingsForFoods(idMap);
            //Map<Long, NutritionData> nData = getRawNutritionDataForFoods(idMap);
            Map<Long, Serving> servings = getRawObjectsForParentFk(foodMap, Serving.table(), Schema.ServingTable.FOOD_ID);
            Map<Long, NutritionData> nutritionData = getRawObjectsForParentFk(foodMap, NutritionData.table(), Schema.NutritionDataTable.FOOD_ID);
            Map<Long, Ingredient> ingredients = getRawObjectsForParentFk(foodMap, Ingredient.table(), Schema.IngredientTable.COMPOSITE_FOOD_ID);
            Map<String, FoodCategory> categories = getAllFoodCategories();
            processRawIngredients(ingredients);
            processRawFoodMap(foodMap, servings, nutritionData, ingredients, categories);
        }
    }

    private <M, N> Map<Long, M> getRawObjectsForParentFk(
            @NotNull Map<Long, N> parentObjectMap, Table<M> childTable, Column.Fk<M, Long, N> fkCol) throws SQLException {
        if (parentObjectMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Column<M, Long> childIdCol = childTable.getIdColumn();
        List<Long> ids = selectColumn(childTable, childIdCol, fkCol, parentObjectMap.keySet());
        if (!ids.isEmpty()) {
            return getRawObjectsByKeys(childTable, childIdCol, ids);
        } else {
            // no objects in the child table refer to any of the parent objects/rows
            return Collections.emptyMap();
        }
    }

    /*
    private Map<Long, Serving> getRawServingsForFoods(@NotNull Map<Long, Food> foodMap) throws SQLException {
        if (foodMap.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> ids = selectColumn(Schema.ServingTable.instance(), Schema.ServingTable.ID, Schema.ServingTable.FOOD_ID, foodMap.keySet());
        if (ids.isEmpty()) {
            // no servings
            return Collections.emptyMap();
        }
        return getRawObjectsByKeys(Schema.ServingTable.instance(), Schema.ServingTable.ID, ids);
    }
    private Map<Long, NutritionData> getRawNutritionDataForFoods(@NotNull Map<Long, Food> foodMap) throws SQLException {
        if (foodMap.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> ids = selectColumn(Schema.NutritionDataTable.instance(),
                Schema.NutritionDataTable.ID, Schema.NutritionDataTable.FOOD_ID, foodMap.keySet());
        if (ids.isEmpty()) {
            // no nutrition data... shouldn't happen!
            return Collections.emptyMap();
        }
        return getRawObjectsByKeys(Schema.NutritionDataTable.instance(), Schema.NutritionDataTable.ID, ids);
    }
    */

    private void applyServingsToRawFoods(Map<Long, Food> foodMap, Map<Long, Serving> servingMap) {
        for (Serving s : servingMap.values()) {
            // QtyUnit setup
            QtyUnit unit = QtyUnits.fromAbbreviationNoThrow(s.qtyUnitAbbr());
            assert (unit != null) : "No quantity unit exists with abbreviation '" + s.qtyUnitAbbr() + "'";
            s.setQtyUnit(unit);
            // this query should never fail, due to database constraints
            Food f = foodMap.get(s.getFoodId());
            assert (f != null);
            s.setFood(f);
            f.addServing(s);
        }
    }

    private void applyNutritionDataToRawFoods(Map<Long, Food> foodMap, Map<Long, NutritionData> nutritionDataMap) {
        for (NutritionData nd : nutritionDataMap.values()) {
            // this lookup should never fail, due to database constraints
            Food f = foodMap.get(nd.getFoodId());
            assert f != null;
            nd.setFood(f);
            f.setNutritionData(nd);
        }
    }

    // note not all foods in the map will be composite
    private void applyIngredientsToRawFoods(Map<Long, Food> foodMap, Map<Long, Ingredient> ingredientMap) {
        for (Ingredient i : ingredientMap.values()) {
            Food f = foodMap.get(i.getCompositeFoodId());
            assert f instanceof CompositeFood && f.getFoodType() == FoodType.COMPOSITE;
            CompositeFood cf = (CompositeFood) f;
            i.setCompositeFood(cf);
            cf.addIngredient(i);
        }
    }

    private void applyFoodCategoriesToRawFoods(Map<Long, Food> foodMap, Map<String, FoodCategory> categories) {
        for (Food f : foodMap.values()) {
            String categoryName = f.getData(Schema.FoodTable.CATEGORY);
            FoodCategory c = categories.get(categoryName);
            f.setFoodCategory(c);
        }
    }

    private <M, I, J> List<I> selectColumn(
            Table<M> t, Column<M, I> selectColumn, Column<M, J> whereColumn, J whereValue) throws SQLException {
        return selectColumn(t, selectColumn, whereColumn, toList(whereValue), false);
    }

    private <M, I, J> List<I> selectColumn(
            Table<M> t, Column<M, I> selectColumn, Column<M, J> whereColumn, Collection<J> whereValues) throws SQLException {
        return selectColumn(t, selectColumn, whereColumn, whereValues, false);
    }

    @Nullable
    public Meal getMealById(@NotNull Long id) throws SQLException {
        Map<Long, Meal> resultMeals = getMealsById(Collections.singletonList(id));
        return getOrDefault(resultMeals, id, null);
    }

    public Map<Long, Meal> getMealsById(@NotNull List<Long> mealIds) throws SQLException {
        if (mealIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> foodIds = getFoodIdsForMeals(mealIds);
        Map<Long, Meal> meals = getRawMealsById(mealIds);
        // this check stops an unnecessary lookup of all foods, which happens if no IDs are passed
        // into getFoodsById;
        if (!foodIds.isEmpty()) {
            Map<Long, Food> foodMap = getFoodsById(foodIds);
            for (Meal meal : meals.values()) {
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
    private Map<Long, Meal> getRawMealsById(@NotNull List<Long> mealIds) throws SQLException {
        return getRawObjectsByKeys(Schema.MealTable.instance(), Schema.MealTable.ID, mealIds);
    }

    /*
     * The map must map the meal ID to the (already created) FoodTable objects needed by FoodPortions
     * in that meal.
     */
    private void applyFoodPortionsToRawMeal(Meal meal, Map<Long, Food> foodMap) throws SQLException {
        List<Long> foodPortionIds = selectColumn(Schema.FoodPortionTable.instance(), Schema.FoodPortionTable.ID, Schema.FoodPortionTable.MEAL_ID, meal.getId());
        if (!foodPortionIds.isEmpty()) {
            Map<Long, FoodPortion> foodPortions = getRawObjectsByIds(Schema.FoodPortionTable.instance(), foodPortionIds);
            for (FoodPortion fp : foodPortions.values()) {
                Food portionFood = foodMap.get(fp.getFoodId());
                assert (portionFood != null);
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
    public Map<String, Meal> getMealsForDay(DateStamp day) throws SQLException {
        List<Long> mealIds = getMealIdsForDay(day);
        Map<Long, Meal> mealsById = getMealsById(mealIds);
        // sort by create time and put in tree map to preserve order
        List<Meal> mealsByCreateTime = new ArrayList<>(mealsById.values());
        // TODO API level: mealsByCreateTime.sort(Comparator.comparingLong(Meal::createTime));
        // TODO API level: Comparator.comparingLong(Meal::createTime);
        Collections.sort(mealsByCreateTime, (m1, m2) -> Long.compare(m1.createTime(), m2.createTime()));

        Map<String, Meal> mealsByName = new TreeMap<>();
        for (Meal m : mealsById.values()) {
            assert !mealsByName.containsKey(m.getName());
            mealsByName.put(m.getName(), m);
        }
        return mealsByName;
    }

    public List<Long> getMealIdsForDay(@NotNull DateStamp day) throws SQLException {
        return selectColumn(Schema.MealTable.instance(), Schema.MealTable.ID, Schema.MealTable.DAY, Collections.singletonList(day));
        // TODO: need "DATE(" + Meal.Column.DAY + ") = DATE ( ? )"; ???
    }

    private <M> M getRawObjectById(Table<M> t, Long id) throws SQLException {
        return getRawObjectByKey(t, t.getIdColumn(), id);
    }

    private <M> Map<Long, M> getRawObjectsByIds(Table<M> t, Collection<Long> ids) throws SQLException {
        return getRawObjectsByKeys(t, t.getIdColumn(), ids);
    }

    private <M, J> M getRawObjectByKey(Table<M> t, Column<M, J> keyCol, J key) throws SQLException {
        Map<J, M> returned = getRawObjectsByKeys(t, keyCol, Collections.singletonList(key));
        return getOrDefault(returned, key, null);
    }

    public <M extends MacrosEntity<M>> int insertObjects(Collection<? extends M> objects, boolean withId) throws SQLException {
        List<ColumnData<M>> objectData = new ArrayList<>(objects.size());
        for (M object: objects)  {
            objectData.add(object.getAllData());
        }
        return insertObjectData(objectData, withId);
    }

    // wildcard capture helper for natural key column type
    private <M extends MacrosEntity<M>, J, N, I> Map<I, J> completeFkIdColHelper(
            Column.Fk<M, J, N> fkColumn, Column<N, I> parentNaturalKeyCol, List<ColumnData<N>> data) throws SQLException {
        assert (parentNaturalKeyCol.isUnique());
        Set<I> uniqueColumnValues = new HashSet<>(data.size());
        for (ColumnData<N> cd : data) {
            uniqueColumnValues.add(cd.get(parentNaturalKeyCol));
        }
        return selectColumnMap(fkColumn.getParentTable(), parentNaturalKeyCol, fkColumn.getParentColumn(), uniqueColumnValues);
    }

    // wildcard capture helper for parent unique column type
    private <M extends MacrosEntity<M>, J, N> List<M> completeFkCol(
            List<M> objects, Column.Fk<M, J, N> fkCol) throws SQLException {
        List<M> completedObjects = new ArrayList<>(objects.size());
        List<ColumnData<N>> naturalKeyData = new ArrayList<>(objects.size());
        for (M object : objects) {
            // needs to be either imported data, or computed, for Recipe nutrition data
            assert (object.getObjectSource() == ObjectSource.IMPORT) ||
                    (object.getObjectSource() == ObjectSource.COMPUTED) : "Object is not from import or computed";
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
    <M extends MacrosEntity<M>> List<M> completeForeignKeys(Collection<M> objects, Column.Fk<M, ?, ?> fk) throws SQLException {
        return completeForeignKeys(objects, toList(fk));
    }

    <M extends MacrosEntity<M>> List<M> completeForeignKeys(
            Collection<M> objects, List<Column.Fk<M, ?, ?>> which) throws SQLException {
        List<M> completedObjects = new ArrayList<>(objects.size());
        if (!objects.isEmpty()) {
            // objects without foreign key data yet (mutable copy of first argument)
            List<M> partiallyCompletedObjects = new ArrayList<>(objects.size());
            partiallyCompletedObjects.addAll(objects);

            // hack to get correct factory type without passing it explicitly as argument
            Factory<M> factory = partiallyCompletedObjects.get(0).getFactory();

            // cycle through the FK columns.
            for (Column.Fk<M, ?, ?> fkCol: which) {
                partiallyCompletedObjects = completeFkCol(partiallyCompletedObjects, fkCol);
            }
            // Check everything's fine and change source to ObjectSource.IMPORT_FK_PRESENT
            for (M object : partiallyCompletedObjects) {
                assert fkIdsPresent(object);
                completedObjects.add(factory.construct(object.getAllData(), object.getObjectSource()));
            }
        }
        return completedObjects;
    }

    private <M extends MacrosEntity<M>> boolean isInDatabase(@NotNull M o) throws SQLException {
        if (o.getId() != MacrosEntity.NO_ID) {
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

    public <M extends MacrosEntity<M>> int saveObjects(Collection<? extends M> objects, ObjectSource objectSource) throws SQLException {
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

    public <M extends MacrosEntity<M>> int saveObject(@NotNull M o) throws SQLException {
        return saveObjects(toList(o), o.getObjectSource());
    }
}
