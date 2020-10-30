package com.machfour.macros.queries

import com.machfour.macros.core.Column
import com.machfour.macros.core.Schema
import com.machfour.macros.core.Table
import com.machfour.macros.objects.*
import com.machfour.macros.queries.FoodQueries.getAllFoodCategories
import com.machfour.macros.queries.FoodQueries.getFoodsById
import com.machfour.macros.queries.FoodQueries.getServingsById
import com.machfour.macros.storage.MacrosDataSource
import java.sql.SQLException

internal object QueryHelpers {
    @Throws(SQLException::class)
    internal fun <M, J> getIdsFromKeys(ds: MacrosDataSource, t: Table<M>, keyCol: Column<M, J>, keys: Collection<J>): Map<J, Long> {
        return if (keys.isEmpty()) emptyMap() else ds.getIdsByKeys(t, keyCol, keys)
    }

    // Makes meal objects, filtering by the list of IDs. If mealIds is empty,
    // all meals will be returned.
    @Throws(SQLException::class)
    internal fun getRawMealsById(ds: MacrosDataSource, mealIds: List<Long>): Map<Long, Meal> {
        return ds.getRawObjectsByKeys(Meal.table, Schema.MealTable.ID, mealIds)
    }

    @Throws(SQLException::class)
    internal fun <M> getRawObjectById(ds: MacrosDataSource, t: Table<M>, id: Long): M? {
        return getRawObjectByKey(ds, t, t.idColumn, id)
    }

    @Throws(SQLException::class)
    internal fun <M> getRawObjectsByIds(ds: MacrosDataSource, t: Table<M>, ids: Collection<Long>): Map<Long, M> {
        return ds.getRawObjectsByKeys(t, t.idColumn, ids)
    }

    @Throws(SQLException::class)
    internal fun <M, J> getRawObjectByKey(ds: MacrosDataSource, t: Table<M>, keyCol: Column<M, J>, key: J): M? {
        val returned = ds.getRawObjectsByKeys(t, keyCol, listOf(key))
        return returned.getOrDefault(key, null)
    }

    @Throws(SQLException::class)
    internal fun processRawIngredients(ds: MacrosDataSource, ingredientMap: Map<Long, Ingredient>) {
        val foodIds: MutableList<Long> = ArrayList(ingredientMap.size)
        val servingIds: MutableList<Long> = ArrayList(ingredientMap.size)
        for (i in ingredientMap.values) {
            foodIds.add(i.ingredientFoodId)
            i.servingId?.let { servingIds += it }
        }
        // XXX make sure this doesn't loop infinitely if two composite foods contain each other as ingredients
        // (or potentially via a longer chain -- A contains B, B contains C, C contains A)
        val ingredientFoods = getFoodsById(ds, foodIds)
        val ingredientServings = getServingsById(ds, servingIds)
        for (i in ingredientMap.values) {
            // applyFoodsToRawIngredients(ingredients, servings
            val f = ingredientFoods.getValue(i.ingredientFoodId)
            i.initIngredientFood(f)
            // applyServingsToRawIngredients(ingredients, servings)
            i.servingId?.let { id ->
                val s = ingredientServings.getValue(id)
                i.initServing(s)
            }
        }
    }

    internal fun processRawFoodMap(foods: Map<Long, Food>, servings: Map<Long, Serving>,
                          nutritionData: Map<Long, NutritionData>, ingredients: Map<Long, Ingredient>,
                          categories: Map<String, FoodCategory>) {
        applyServingsToRawFoods(foods, servings)
        applyNutritionDataToRawFoods(foods, nutritionData)
        applyIngredientsToRawFoods(foods, ingredients)
        applyFoodCategoriesToRawFoods(foods, categories)
    }

    // foodMap is a map of food IDs to the raw (i.e. unlinked) object created from the database
    @Throws(SQLException::class)
    internal fun processRawFoodMap(ds: MacrosDataSource, foodMap: Map<Long, Food>) {
        if (foodMap.isNotEmpty()) {
            //Map<Long, Serving> servings = getRawServingsForFoods(idMap);
            //Map<Long, NutritionData> nData = getRawNutritionDataForFoods(idMap);
            val servings = getRawObjectsForParentFk(ds, foodMap, Serving.table, Schema.ServingTable.FOOD_ID)
            val nutritionData = getRawObjectsForParentFk(ds, foodMap, NutritionData.table, Schema.NutritionDataTable.FOOD_ID)
            val ingredients = getRawObjectsForParentFk(ds, foodMap, Ingredient.table, Schema.IngredientTable.COMPOSITE_FOOD_ID)
            val categories: Map<String, FoodCategory> = getAllFoodCategories(ds)
            processRawIngredients(ds, ingredients)
            processRawFoodMap(foodMap, servings, nutritionData, ingredients, categories)
        }
    }

    @Throws(SQLException::class)
    internal fun <M, N> getRawObjectsForParentFk(ds: MacrosDataSource,
                                        parentObjectMap: Map<Long, N>, childTable: Table<M>, fkCol: Column.Fk<M, Long, N>): Map<Long, M> {
        var objectMap: Map<Long, M> = emptyMap()
        if (parentObjectMap.isNotEmpty()) {
            val childIdCol = childTable.idColumn
            val ids = Queries.selectColumn(ds, childTable, childIdCol, fkCol, parentObjectMap.keys)
                .map { requireNotNull(it) { "Error: null ID encountered: $it" } }
            if (ids.isNotEmpty()) {
                objectMap = ds.getRawObjectsByKeys(childTable, childIdCol, ids)
            } // else no objects in the child table refer to any of the parent objects/rows
        }
        return objectMap
    }

    private fun applyServingsToRawFoods(foodMap: Map<Long, Food>, servingMap: Map<Long, Serving>) {
        for (s in servingMap.values) {
            // this query should never fail, due to database constraints
            val f = foodMap.getValue(s.foodId)
            s.initFood(f)
            f.addServing(s)
        }
    }

    private fun applyNutritionDataToRawFoods(foodMap: Map<Long, Food>, nutritionDataMap: Map<Long, NutritionData>) {
        for (nd in nutritionDataMap.values) {
            // this lookup should never fail, due to database constraints
            val f = foodMap[nd.foodId]!!
            nd.food = f
            f.setNutritionData(nd)
        }
    }

    // note not all foods in the map will be composite
    private fun applyIngredientsToRawFoods(foodMap: Map<Long, Food>, ingredientMap: Map<Long, Ingredient>) {
        for (i in ingredientMap.values) {
            val f = foodMap[i.compositeFoodId]
            require(f is CompositeFood && f.foodType == FoodType.COMPOSITE)
            i.initCompositeFood(f)
            f.addIngredient(i)
        }
    }

    private fun applyFoodCategoriesToRawFoods(foodMap: Map<Long, Food>, categories: Map<String, FoodCategory>) {
        for (f in foodMap.values) {
            val categoryName = f.getData(Schema.FoodTable.CATEGORY)
            val c = categories[categoryName]!!
            f.setFoodCategory(c)
        }
    }

    @Throws(SQLException::class)
    internal fun processRawFoodPortions(ds: MacrosDataSource, meal: Meal, fpMap: Map<Long, FoodPortion>, foodMap: Map<Long, Food>) {
        // sort by create time - but if adding to existing meals, they are added to the end
        val fpList = fpMap.values.toList().sortedBy { it.createTime }
        for (fp in fpList) {
            val portionFood = foodMap[fp.foodId]
            require(portionFood != null) { "foodMap did not contain food with ID ${fp.foodId}" }
            fp.initFood(portionFood)
            fp.servingId?.let {
                val serving = portionFood.getServingById(it)
                checkNotNull(serving) { "Serving specified by FoodPortion not found in its food!" }
                fp.initServing(serving)
            }
            fp.initMeal(meal)
            meal.addFoodPortion(fp)
        }
    }

    @Throws(SQLException::class)
    private fun getRawFoodPortionsForMeal(ds: MacrosDataSource, meal: Meal): Map<Long, FoodPortion> {
        return Queries.selectColumn(ds, FoodPortion.table, Schema.FoodPortionTable.ID, Schema.FoodPortionTable.MEAL_ID, meal.id)
            .map { checkNotNull(it) { "Error: null FoodPortion ID encountered: $it" } }
            .takeIf { it.isNotEmpty() }
            ?.let { getRawObjectsByIds(ds, FoodPortion.table, it) }
            ?: emptyMap()
    }

    /*
     * The map must map the meal ID to the (already created) FoodTable objects needed by FoodPortions
     * in that meal.
     */
    @Throws(SQLException::class)
    internal fun applyFoodPortionsToRawMeal(ds: MacrosDataSource, meal: Meal, foodMap: Map<Long, Food>) {
        getRawFoodPortionsForMeal(ds, meal)
            .takeIf { it.isNotEmpty() }
            ?.let { processRawFoodPortions(ds, meal, it, foodMap) }
    }
}