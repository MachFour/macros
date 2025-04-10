package com.machfour.macros.queries

import com.machfour.datestamp.DateStamp
import com.machfour.macros.core.EntityId
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.SearchRelevance
import com.machfour.macros.entities.*
import com.machfour.macros.entities.Unit
import com.machfour.macros.schema.FoodNutrientValueTable
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Each query returns a single value (or set of values) that is not updated when the database changes
open class StaticDataSource(private val database: SqlDatabase): MacrosDataSource {
    override fun reset() { }

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

    override fun getMealIdsForFoodPortionIds(foodPortionIds: Collection<Long>): List<Long> {
        return getMealIdsForFoodPortionIds(database, foodPortionIds)
    }


    override fun getDaysForMealIds(mealIds: Collection<Long>): List<DateStamp> {
        return getDaysForMealIds(database, mealIds)
    }

    override fun getCommonQuantities(foodId: Long, limit: Int): List<Triple<Double, Unit, String?>> {
        return getCommonQuantities(database, foodId, limit)
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

    override fun <M : MacrosEntity<M>> saveObjectsReturningIds(
        objects: Collection<M>,
        source: ObjectSource
    ): List<EntityId> {
        return saveObjectsReturningIds(database, objects, source)
    }

    override fun saveNutrientsToFood(foodId: EntityId, nutrients: List<FoodNutrientValue>) {
        val (insertObjects, updateObjects) = nutrients.partition { it.source == ObjectSource.USER_NEW }

        // add Food ID to data maps
        val completedInsertObjects = insertObjects.map {
            val data = it.dataFullCopy()
            data.put(FoodNutrientValueTable.FOOD_ID, foodId)
            FoodNutrientValue.factory.construct(data, ObjectSource.USER_NEW)
        }

        var openedNewConnection = false
        try {
            openedNewConnection = database.openConnection()
            database.beginTransaction()

            saveObjects(completedInsertObjects, ObjectSource.USER_NEW)
            saveObjects(updateObjects, ObjectSource.DB_EDIT)
            database.endTransaction()
        } catch (e: SqlException) {
            database.rollbackTransaction()
            throw e
        } finally {
            // TODO if exception is thrown here after an exception thrown
            // in the above try block, the one here will hide the previous.
            if (openedNewConnection) {
                database.closeConnection()
            }
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

    override fun foodSearch(keywords: List<String>, matchAll: Boolean, minRelevance: SearchRelevance): Set<Long> {
        return foodSearch(database, keywords, matchAll, minRelevance)
    }

    override fun foodSearch(keyword: String, minRelevance: SearchRelevance): Set<Long> {
        return foodSearch(database, keyword)
    }

    override fun recentFoodIds(howMany: Int, distinct: Boolean): List<Long> {
        return recentFoodIds(database, howMany, distinct)
    }

    override fun recentMealIds(howMany: Int, nameFilter: Collection<String>): List<Long> {
        return recentMealIds(database, howMany, nameFilter)
    }
}