package com.machfour.macros.storage;

import com.machfour.macros.core.*;
import com.machfour.macros.data.Column;
import com.machfour.macros.data.Columns;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Tables;
import com.machfour.macros.util.DateStamp;
import com.sun.istack.internal.NotNull;

import java.util.*;

public class MacrosDatabase implements MacrosDataSource {
    private static final String TAG = "MacrosDatabase";

    // singleton
    private static MacrosDatabase INSTANCE;

    private MacrosDatabase() {
    }

    public static MacrosDatabase getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MacrosDatabase();
        }
        return INSTANCE;
    }

    @Override
    public <T extends MacrosPersistable> boolean deleteObject(@NotNull T o) {
        return deleteById(o.getId(), o.getTable());
    }

    private <T extends MacrosPersistable> boolean deleteById(Long id, Table t) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String tableName = c.getTableName();
        String whereClause = c.getIdColumnName() + " = " + id;
        int rowsDeleted = db.delete(tableName, whereClause, null);
        return (rowsDeleted == 1);
    }

    @Override
    public <T extends MacrosPersistable> void deleteObjects(@NotNull List<T> objects) {
        if (!objects.isEmpty()) {
            Table t = objects.get(0).getTable();
            for (T object : objects) {
                if (object != null) {
                    deleteById(object.getId(), t);
                }
            }
        }
    }

    @Override
    public List<Long> foodSearch(String keyword) {
        List<Column> columns = Arrays.asList(
            Columns.Food.INDEX_NAME
            , Columns.Food.NAME
            , Columns.Food.COMMERCIAL_NAME
            , Columns.Food.BRAND
        );
        return prefixSearch(Tables.FoodTable, columns, keyword);
    }

    public List<Long> prefixSearch(Table conv, List<Column<?>> cols, String keyword) {
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
        Log.d(TAG, "getFoodsById(" + foodIds + ")");
        List<Food> foods = getRawFoodsById(foodIds);
        Map<Long, Food> foodMap = new HashMap(foods.size(), 1);

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

    // makes the result set into a list of foods.
    // returns an empty list if there were not foods in the result set.
    private List<Food> cursorToRawFoods(@NotNull Cursor c, int numFields) {
        List<Food> foods = new ArrayList<>(c.getCount());
        if (c.getCount() > 0) {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                ContentValues v = new ContentValues(numFields);

                for (String field : NutritionData.DATA_FIELDS) {
                    DatabaseUtils.cursorDoubleToCursorValues(c, field, v);
                }
                for (String field : FoodDescription.DATA_FIELDS) {
                    DatabaseUtils.cursorStringToContentValues(c, field, v);
                }
                DatabaseUtils.cursorLongToContentValues(c, Food.Column.ID.str, v);
                DatabaseUtils.cursorIntToContentValues(c, Food.Column.MEASURED_BY_VOLUME.str, v);
                DatabaseUtils.cursorIntToContentValues(c, Food.Column.USDA_INDEX.str, v);
                DatabaseUtils.cursorStringToContentValues(c, Food.Column.NUTTAB_INDEX.str, v);
                DatabaseUtils.cursorLongToContentValues(c, Food.Column.CREATE_TIME.str, v);
                DatabaseUtils.cursorLongToContentValues(c, Food.Column.MODIFY_TIME.str, v);

                foods.add(Food.Converter.getInstance().fromDbContentValues(v));
            }
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

    /*
     * Creates serving objects from a cursor, after querying serving data about ONE food
     * The food object is unchanged.
     */
    private List<Serving> cursorToRawServings(@NotNull Cursor c, int numFields) {
        List<Serving> servings = new ArrayList<>(c.getCount());
        if (c.getCount() > 0) {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                ContentValues v = new ContentValues(numFields);
                DatabaseUtils.cursorLongToContentValues(c, Serving.Column.ID.str, v);
                DatabaseUtils.cursorStringToContentValues(c, Serving.Column.NAME.str, v);
                DatabaseUtils.cursorLongToContentValues(c, Serving.Column.FOOD_ID.str, v);
                DatabaseUtils.cursorDoubleToCursorValues(c, Serving.Column.QUANTITY.str, v);
                DatabaseUtils.cursorIntToContentValues(c, Serving.Column.IS_DEFAULT.str, v);
                DatabaseUtils.cursorLongToContentValues(c, Serving.Column.CREATE_TIME.str, v);
                DatabaseUtils.cursorLongToContentValues(c, Serving.Column.MODIFY_TIME.str, v);
                servings.add(Serving.Converter.getInstance().fromDbContentValues(v));
            }
        }
        return servings;
    }

    private String buildWhereString(String idColumnName, Collection<Long> ids) {
        StringBuilder whereString = new StringBuilder();
        if (ids.size() > 1) {
            whereString.append(idColumnName)
                .append(" IN (")
                .append(TextUtils.join(",", ids))
                .append(")");
        } else if (ids.size() == 1) {
            whereString.append(idColumnName).append(" = ").append(ids.iterator().next());
        }
        return whereString.toString();
    }

    @Override
    public Food getFoodById(long id) {
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
        String selection = Food.Column.INDEX_NAME.str;
        String[] selectionArgs = new String[]{indexName};
        Food f = getRawFoodByKey(selection, selectionArgs);
        // a bit redundant since we only need the ID but whatever
        return f == null ? MacrosPersistable.NO_ID : f.getId();
    }

    @Override
    public Meal getMealById(long id) {
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

    private List<Meal> cursorToRawMeals(@NotNull Cursor c, int numFields) {
        List<Meal> meals = new ArrayList<>(c.getCount());
        if (c.getCount() > 0) {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                ContentValues v = new ContentValues(numFields);

                DatabaseUtils.cursorIntToContentValues(c, Meal.Column.ID.str, v);
                DatabaseUtils.cursorStringToContentValues(c, Meal.Column.DESCRIPTION.str, v);
                DatabaseUtils.cursorStringToContentValues(c, Meal.Column.DAY.str, v);
                DatabaseUtils.cursorLongToContentValues(c, Meal.Column.CREATE_TIME.str, v);
                DatabaseUtils.cursorLongToContentValues(c, Meal.Column.MODIFY_TIME.str, v);

                meals.add(Meal.Converter.getInstance().fromDbContentValues(v));
            }
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

    /*
     * Creates food portion objects from the cursor returned by a query for
     * FoodPortionTable data for ONE meal.
     */
    private List<FoodPortion> cursorToRawFoodPortions(Cursor c, int numFields) {
        List<FoodPortion> foodPortions = new ArrayList<>(c.getCount());
        if (c.getCount() > 0) {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                ContentValues v = new ContentValues(numFields);
                DatabaseUtils.cursorLongToContentValues(c, FoodPortion.Column.ID.str, v);
                DatabaseUtils.cursorDoubleToCursorValues(c, FoodPortion.Column.QUANTITY.str, v);
                DatabaseUtils.cursorLongToContentValues(c, FoodPortion.Column.FOOD_ID.str, v);
                DatabaseUtils.cursorLongToContentValues(c, FoodPortion.Column.MEAL_ID.str, v);
                DatabaseUtils.cursorLongToContentValues(c, FoodPortion.Column.SERVING_ID.str, v);
                DatabaseUtils.cursorLongToContentValues(c, FoodPortion.Column.CREATE_TIME.str, v);
                DatabaseUtils.cursorLongToContentValues(c, FoodPortion.Column.MODIFY_TIME.str, v);
                foodPortions.add(FoodPortion.Converter.getInstance().fromDbContentValues(v));
            }
        }
        return foodPortions;
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

    private Food getRawFoodById(long id) {
        // have to bind the ID as an integer manually, as selectionArgs only supports strings
        String selection = Food.CONVERTER.getIdColumnName() + " = " + id;
        return getRawFoodByKey(selection, null);
    }

    private Food getRawFoodByKey(String selection, String[] selectionArgs) {
        SQLiteDatabase db = helper.getReadableDatabase();

        String table = Food.Converter.getInstance().getTableName();
        String[] projection = Food.Converter.getInstance().getColumns();
        Cursor c = db.query(table, projection, selection, selectionArgs, null, null, null);

        // there should be at most 1 FoodTable object in the result list
        Food food = null;
        if (c != null) {
            List<Food> resultFoods = cursorToRawFoods(c, projection.length);
            if (!resultFoods.isEmpty()) {
                food = resultFoods.get(0);
            }
            c.close();
        }

        db.close();
        return food;
    }

    private List<Long> getServingIdsForFoods(long foodId) {
        return getServingIdsForFoods(Collections.singletonList(foodId));
    }

    public void resetDatabase() {
        helper.resetDatabase();
    }

    @Override
    public <T extends MacrosPersistable<T>> void saveObjects(@NotNull List<T> objects) {
        for (T object : objects) {
            if (object != null) {
                saveObject(object);
            }
        }
    }

    @Override
    public <T extends MacrosPersistable<T>> boolean saveObject(@NotNull T o) {
        Table t = o.getTable();

        ContentValues values = converter.toDbContentValues(o);
        SQLiteDatabase db = helper.getWritableDatabase();
        String tableName = converter.getTableName();

        long id = o.getId();
        // neither case below actually requires explicit storing of the ID column
        if (values.containsKey(converter.getIdColumnName())) {
            values.remove(converter.getIdColumnName());
        }

        boolean writeSuccess;

        if (id == MacrosPersistable.NO_ID) {
            // insert operation
            long result = db.insert(tableName, null, values);
            writeSuccess = (result != -1);
        } else {
            // update existing row
            String whereClause = converter.getIdColumnName() + " = " + id;
            int rowsUpdated = db.update(tableName, values, whereClause, null);
            writeSuccess = (rowsUpdated == 1);
        }

        return writeSuccess;
    }
}
