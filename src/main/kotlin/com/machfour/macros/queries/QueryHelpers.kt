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
import java.util.*

internal object QueryHelpers {
    @Throws(SQLException::class)
    fun <M, J> getIdsFromKeys(ds: MacrosDataSource, t: Table<M>, keyCol: Column<M, J>, keys: Collection<J>): Map<J, Long> {
        return if (keys.isEmpty()) emptyMap() else ds.getIdsByKeysNoEmpty(t, keyCol, keys)
    }

    // Makes meal objects, filtering by the list of IDs. If mealIds is empty,
    // all meals will be returned.
    @Throws(SQLException::class)
    fun getRawMealsById(ds: MacrosDataSource, mealIds: List<Long>): Map<Long, Meal> {
        return getRawObjectsByKeys(ds, Meal.table(), Schema.MealTable.ID, mealIds)
    }

    @Throws(SQLException::class)
    fun <M> getRawObjectById(ds: MacrosDataSource, t: Table<M>, id: Long): M? {
        return getRawObjectByKey(ds, t, t.idColumn, id)
    }

    @Throws(SQLException::class)
    fun <M> getRawObjectsByIds(ds: MacrosDataSource, t: Table<M>, ids: Collection<Long>): Map<Long, M> {
        return getRawObjectsByKeys(ds, t, t.idColumn, ids)
    }

    @Throws(SQLException::class)
    fun <M, J> getRawObjectByKey(ds: MacrosDataSource, t: Table<M>, keyCol: Column<M, J>, key: J): M? {
        val returned = getRawObjectsByKeys(ds, t, keyCol, listOf(key))
        return returned.getOrDefault(key, null)
    }

    @Throws(SQLException::class)
    fun <M, J> getRawObjectsByKeys(ds: MacrosDataSource, t: Table<M>, keyCol: Column<M, J>, keys: Collection<J>): Map<J, M> {
        return if (keys.isEmpty()) emptyMap() else ds.getRawObjectsByKeysNoEmpty(t, keyCol, keys)
    }

    @Throws(SQLException::class)
    fun processRawIngredients(ds: MacrosDataSource, ingredientMap: Map<Long, Ingredient>) {
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
            i.ingredientFood = f
            // applyServingsToRawIngredients(ingredients, servings)
            i.servingId?.let { id ->
                val s = ingredientServings.getValue(id)
                i.setServing(s)
            }
        }
    }

    fun processRawFoodMap(foods: Map<Long, Food>, servings: Map<Long, Serving>,
                          nutritionData: Map<Long, NutritionData>, ingredients: Map<Long, Ingredient>,
                          categories: Map<String, FoodCategory>) {
        applyServingsToRawFoods(foods, servings)
        applyNutritionDataToRawFoods(foods, nutritionData)
        applyIngredientsToRawFoods(foods, ingredients)
        applyFoodCategoriesToRawFoods(foods, categories)
    }

    // foodMap is a map of food IDs to the raw (i.e. unlinked) object created from the database
    @Throws(SQLException::class)
    fun processRawFoodMap(ds: MacrosDataSource, foodMap: Map<Long, Food>) {
        if (foodMap.isNotEmpty()) {
            //Map<Long, Serving> servings = getRawServingsForFoods(idMap);
            //Map<Long, NutritionData> nData = getRawNutritionDataForFoods(idMap);
            val servings = getRawObjectsForParentFk(ds, foodMap, Serving.table(), Schema.ServingTable.FOOD_ID)
            val nutritionData = getRawObjectsForParentFk(ds, foodMap, NutritionData.table(), Schema.NutritionDataTable.FOOD_ID)
            val ingredients = getRawObjectsForParentFk(ds, foodMap, Ingredient.table(), Schema.IngredientTable.COMPOSITE_FOOD_ID)
            val categories: Map<String, FoodCategory> = getAllFoodCategories(ds)
            processRawIngredients(ds, ingredients)
            processRawFoodMap(foodMap, servings, nutritionData, ingredients, categories)
        }
    }

    @Throws(SQLException::class)
    fun <M, N> getRawObjectsForParentFk(ds: MacrosDataSource,
                                        parentObjectMap: Map<Long, N>, childTable: Table<M>, fkCol: Column.Fk<M, Long, N>): Map<Long, M> {
        var objectMap: Map<Long, M> = emptyMap()
        if (parentObjectMap.isNotEmpty()) {
            val childIdCol = childTable.idColumn
            val ids = Queries.selectColumn(ds, childTable, childIdCol, fkCol, parentObjectMap.keys)
                .map { requireNotNull(it) { "Error: null ID encountered: $it" } }
            if (ids.isNotEmpty()) {
                objectMap = getRawObjectsByKeys(ds, childTable, childIdCol, ids)
            } // else no objects in the child table refer to any of the parent objects/rows
        }
        return objectMap
    }

    private fun applyServingsToRawFoods(foodMap: Map<Long, Food>, servingMap: Map<Long, Serving>) {
        for (s in servingMap.values) {
            // QtyUnit setup
            val unit = QtyUnits.fromAbbreviationNoThrow(s.qtyUnitAbbr)
                    ?: error("No quantity unit exists with abbreviation '" + s.qtyUnitAbbr + "'")
            s.qtyUnit = unit
            // this query should never fail, due to database constraints
            val f = foodMap.getValue(s.foodId)
            s.food = f
            f.addServing(s)
        }
    }

    private fun applyNutritionDataToRawFoods(foodMap: Map<Long, Food>, nutritionDataMap: Map<Long, NutritionData>) {
        for (nd in nutritionDataMap.values) {
            // this lookup should never fail, due to database constraints
            val f = foodMap[nd.foodId]!!
            nd.food = f
            f.nutritionData = nd
        }
    }

    // note not all foods in the map will be composite
    private fun applyIngredientsToRawFoods(foodMap: Map<Long, Food>, ingredientMap: Map<Long, Ingredient>) {
        for (i in ingredientMap.values) {
            val f = foodMap[i.compositeFoodId]
            require(f is CompositeFood && f.getFoodType() == FoodType.COMPOSITE)
            i.compositeFood = f
            f.addIngredient(i)
        }
    }

    private fun applyFoodCategoriesToRawFoods(foodMap: Map<Long, Food>, categories: Map<String, FoodCategory>) {
        for (f in foodMap.values) {
            val categoryName = f.getData(Schema.FoodTable.CATEGORY)
            val c = categories[categoryName]
            f.foodCategory = c!!
        }
    }

    /*
     * The map must map the meal ID to the (already created) FoodTable objects needed by FoodPortions
     * in that meal.
     */
    @Throws(SQLException::class)
    fun applyFoodPortionsToRawMeal(ds: MacrosDataSource, meal: Meal, foodMap: Map<Long, Food>) {
        val foodPortionIds = Queries.selectColumn(ds, FoodPortion.table(),
                Schema.FoodPortionTable.ID, Schema.FoodPortionTable.MEAL_ID, meal.id)
                .map { requireNotNull(it) { "Error: null FoodPortion ID encountered: $it" } }
        if (foodPortionIds.isNotEmpty()) {
            val foodPortions = getRawObjectsByIds(ds, FoodPortion.table(), foodPortionIds)
            for (fp in foodPortions.values) {
                val portionFood = foodMap.getValue(fp.foodId)
                fp.food = portionFood
                fp.servingId?.let {
                    val serving = portionFood.getServingById(it)
                            ?: error("Serving specified by FoodPortion not found in its food!")
                    fp.setServing(serving)
                }
                fp.meal = meal
                meal.addFoodPortion(fp)
            }
        }
    }
}