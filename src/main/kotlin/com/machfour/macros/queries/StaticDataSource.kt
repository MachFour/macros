package com.machfour.macros.queries

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodCategory
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.Meal
import com.machfour.macros.schema.FoodNutrientValueTable
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.util.DateStamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Each query returns a single value (or set of values) that is not updated when the database changes
open class StaticDataSource(private val database: SqlDatabase): MacrosDataSource {

    override fun getAllFoodCategories(): Flow<Map<String, FoodCategory>> {
        return flowOf(getAllFoodCategories(database))
    }

    override fun getFood(id: Long): Flow<Food?> {
        return flowOf(getFoodById(database, id))
    }

    override fun getFoods(ids: Collection<Long>, preserveOrder: Boolean): Flow<Map<Long, Food>> {
        return flowOf(getFoodsById(database, ids, preserveOrder))
    }

    override fun getAllFoods(): Flow<Map<Long, Food>> {
        return flowOf(getAllFoodsMap(database))
    }

    override fun getMeal(id: Long): Flow<Meal?> {
        return flowOf(getMealById(database, id))
    }

    override fun getMeals(ids: Collection<Long>): Flow<Map<Long, Meal>> {
        return flowOf(getMealsById(database, ids))
    }

    override fun getMealsForDay(day: DateStamp): Flow<Map<Long, Meal>> {
        return flowOf(getMealsForDay(database, day))
    }

    override fun getFoodIdByIndexName(indexName: String): Long? {
        return getFoodIdByIndexName(database, indexName)
    }

    override fun getParentFoodIdsContainingFoodIds(foodIds: List<Long>): List<Long> {
        return getParentFoodIdsContainingFoodIds(database, foodIds)
    }

    override fun getMealIdsForDay(day: DateStamp): List<Long> {
        return getMealIdsForDay(database, day)
    }

    override fun getMealIdsForFoodIds(foodIds: Collection<Long>): List<Long> {
        return getMealIdsForFoodIds(database, foodIds)
    }

    override fun getDaysForMealIds(mealIds: Collection<Long>): List<DateStamp> {
        return getDaysForMealIds(database, mealIds)
    }

    override fun <M : MacrosEntity<M>> deleteObjects(objects: Collection<M>): Int {
        return deleteObjects(database, objects)
    }

    override fun <M : MacrosEntity<M>> saveObjects(
        objects: Collection<M>,
        source: ObjectSource
    ): Int {
        return saveObjects(database, objects, source)
    }

    override fun saveNutrientsToFood(food: Food, nutrients: List<FoodNutrientValue>) {
        val foodIdCol = FoodNutrientValueTable.FOOD_ID
        try {
            val (insertNutrients, updateNutrients) = nutrients.partition { it.source == ObjectSource.USER_NEW }

            // link the new FoodNutrientValues to the food
            for (nv in insertNutrients) {
                nv.setFkParentKey(foodIdCol, FoodTable.INDEX_NAME, food)
            }

            database.openConnection()
            database.beginTransaction()

            // get the food ID into the FOOD_ID field of the NutrientValues
            val completedNValues = completeForeignKeys(database, insertNutrients, foodIdCol)

            saveObjects(completedNValues, ObjectSource.USER_NEW)
            saveObjects(updateNutrients, ObjectSource.DB_EDIT)


            database.endTransaction()
        } finally {
            // TODO if exception is thrown here after an exception thrown
            // in the above try block, the one here will hide the previous.
            database.closeConnection()
        }
    }

    fun deleteAllIngredients() {
        return deleteAllIngredients(database)
    }

    fun deleteAllFoodPortions() {
        return deleteAllFoodPortions(database)
    }

    fun deleteAllCompositeFoods(): Int {
        return deleteAllCompositeFoods(database)
    }

    override fun forgetFood(f: Food) {
        return forgetFood(database, f)
    }

    /*
     * Passthrough fuunctions
     */

    override fun foodSearch(keywords: List<String>, matchAll: Boolean, minRelevance: Int): Set<Long> {
        return foodSearch(database, keywords, matchAll, minRelevance)
    }

    override fun foodSearch(keyword: String, minRelevance: Int): Set<Long> {
        return foodSearch(database, keyword)
    }

    override fun recentFoodIds(howMany: Int): List<Long> {
        return recentFoodIds(database, howMany)
    }

}