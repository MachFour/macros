package com.machfour.macros.queries;

import com.machfour.macros.core.Column;
import com.machfour.macros.core.Schema;
import com.machfour.macros.core.Table;
import com.machfour.macros.objects.CompositeFood;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.FoodCategory;
import com.machfour.macros.objects.FoodPortion;
import com.machfour.macros.objects.FoodType;
import com.machfour.macros.objects.Ingredient;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.objects.QtyUnit;
import com.machfour.macros.objects.QtyUnits;
import com.machfour.macros.objects.Serving;
import com.machfour.macros.storage.MacrosDataSource;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.machfour.macros.core.MacrosUtils.getOrDefault;

class QueryHelpers {
    static <M, J> Map<J, Long> getIdsFromKeys(MacrosDataSource ds, Table<M> t, Column<M, J> keyCol, Collection<J> keys) throws SQLException {
        return keys.isEmpty() ? Collections.emptyMap() : ds.getIdsByKeysNoEmpty(t, keyCol, keys);
    }

    // Makes meal objects, filtering by the list of IDs. If mealIds is empty,
    // all meals will be returned.
    @NotNull
    static Map<Long, Meal> getRawMealsById(MacrosDataSource ds, @NotNull List<Long> mealIds) throws SQLException {
        return getRawObjectsByKeys(ds, Meal.table(), Schema.MealTable.ID, mealIds);
    }

    @Nullable
    static <M> M getRawObjectById(MacrosDataSource ds, Table<M> t, Long id) throws SQLException {
        return getRawObjectByKey(ds, t, t.getIdColumn(), id);
    }

    @NotNull
    static <M> Map<Long, M> getRawObjectsByIds(MacrosDataSource ds, Table<M> t, Collection<Long> ids) throws SQLException {
        return getRawObjectsByKeys(ds, t, t.getIdColumn(), ids);
    }

    @Nullable
    static <M, J> M getRawObjectByKey(MacrosDataSource ds, Table<M> t, Column<M, J> keyCol, J key) throws SQLException {
        Map<J, M> returned = getRawObjectsByKeys(ds, t, keyCol, Collections.singletonList(key));
        return getOrDefault(returned, key, null);
    }

    @NotNull
    static <M, J> Map<J, M> getRawObjectsByKeys(MacrosDataSource ds, Table<M> t, Column<M, J> keyCol, Collection<J> keys) throws SQLException {
        return keys.isEmpty() ? Collections.emptyMap() : ds.getRawObjectsByKeysNoEmpty(t, keyCol, keys);
    }

    static void processRawIngredients(MacrosDataSource ds, Map<Long, Ingredient> ingredientMap) throws SQLException {
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
        Map<Long, Food> ingredientFoods = FoodQueries.getFoodsById(ds, foodIds);
        Map<Long, Serving> ingredientServings = FoodQueries.getServingsById(ds, servingIds);

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

    static void processRawFoodMap(Map<Long, Food> foods, Map<Long, Serving> servings,
            Map<Long, NutritionData> nutritionData, Map<Long, Ingredient> ingredients,
            Map<String, FoodCategory> categories) {
        applyServingsToRawFoods(foods, servings);
        applyNutritionDataToRawFoods(foods, nutritionData);
        applyIngredientsToRawFoods(foods, ingredients);
        applyFoodCategoriesToRawFoods(foods, categories);
    }

    // foodMap is a map of food IDs to the raw (i.e. unlinked) object created from the database
    static void processRawFoodMap(MacrosDataSource ds, Map<Long, Food> foodMap) throws SQLException {
        if (!foodMap.isEmpty()) {
            //Map<Long, Serving> servings = getRawServingsForFoods(idMap);
            //Map<Long, NutritionData> nData = getRawNutritionDataForFoods(idMap);
            Map<Long, Serving> servings = getRawObjectsForParentFk(ds, foodMap, Serving.table(), Schema.ServingTable.FOOD_ID);
            Map<Long, NutritionData> nutritionData = getRawObjectsForParentFk(ds, foodMap, NutritionData.table(), Schema.NutritionDataTable.FOOD_ID);
            Map<Long, Ingredient> ingredients = getRawObjectsForParentFk(ds, foodMap, Ingredient.table(), Schema.IngredientTable.COMPOSITE_FOOD_ID);
            Map<String, FoodCategory> categories = FoodQueries.getAllFoodCategories(ds);
            processRawIngredients(ds, ingredients);
            processRawFoodMap(foodMap, servings, nutritionData, ingredients, categories);
        }
    }

    @NotNull
    static <M, N> Map<Long, M> getRawObjectsForParentFk(MacrosDataSource ds,
            @NotNull Map<Long, N> parentObjectMap, Table<M> childTable, Column.Fk<M, Long, N> fkCol) throws SQLException {
        Map<Long, M> objectMap = Collections.emptyMap();
        if (!parentObjectMap.isEmpty()) {
            Column<M, Long> childIdCol = childTable.getIdColumn();
            List<Long> ids = Queries.selectColumn(ds, childTable, childIdCol, fkCol, parentObjectMap.keySet());
            if (!ids.isEmpty()) {
                objectMap = getRawObjectsByKeys(ds, childTable, childIdCol, ids);
            } // else no objects in the child table refer to any of the parent objects/rows
        }
        return objectMap;
    }

    static void applyServingsToRawFoods(Map<Long, Food> foodMap, Map<Long, Serving> servingMap) {
        for (Serving s : servingMap.values()) {
            // QtyUnit setup
            QtyUnit unit = QtyUnits.fromAbbreviationNoThrow(s.getQtyUnitAbbr());
            assert (unit != null) : "No quantity unit exists with abbreviation '" + s.getQtyUnitAbbr() + "'";
            s.setQtyUnit(unit);
            // this query should never fail, due to database constraints
            Food f = foodMap.get(s.getFoodId());
            assert (f != null);
            s.setFood(f);
            f.addServing(s);
        }
    }

    static void applyNutritionDataToRawFoods(Map<Long, Food> foodMap, Map<Long, NutritionData> nutritionDataMap) {
        for (NutritionData nd : nutritionDataMap.values()) {
            // this lookup should never fail, due to database constraints
            Food f = foodMap.get(nd.getFoodId());
            assert f != null;
            nd.setFood(f);
            f.setNutritionData(nd);
        }
    }

    // note not all foods in the map will be composite
    static void applyIngredientsToRawFoods(Map<Long, Food> foodMap, Map<Long, Ingredient> ingredientMap) {
        for (Ingredient i : ingredientMap.values()) {
            Food f = foodMap.get(i.getCompositeFoodId());
            assert f instanceof CompositeFood && f.getFoodType() == FoodType.COMPOSITE;
            CompositeFood cf = (CompositeFood) f;
            i.setCompositeFood(cf);
            cf.addIngredient(i);
        }
    }

    static void applyFoodCategoriesToRawFoods(Map<Long, Food> foodMap, Map<String, FoodCategory> categories) {
        for (Food f : foodMap.values()) {
            String categoryName = f.getData(Schema.FoodTable.CATEGORY);
            FoodCategory c = categories.get(categoryName);
            f.setFoodCategory(c);
        }
    }

    /*
     * The map must map the meal ID to the (already created) FoodTable objects needed by FoodPortions
     * in that meal.
     */
    static void applyFoodPortionsToRawMeal(MacrosDataSource ds, Meal meal, Map<Long, Food> foodMap) throws SQLException {
        List<Long> foodPortionIds = Queries.selectColumn(ds, FoodPortion.table(),
                Schema.FoodPortionTable.ID, Schema.FoodPortionTable.MEAL_ID, meal.getId());
        if (!foodPortionIds.isEmpty()) {
            Map<Long, FoodPortion> foodPortions = getRawObjectsByIds(ds, FoodPortion.table(), foodPortionIds);
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
}
