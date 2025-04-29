package com.machfour.macros.queries

import com.machfour.datestamp.DateStamp
import com.machfour.macros.core.EntityId
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.SearchRelevance
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodCategory
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.Meal
import com.machfour.macros.nutrients.IQuantity
import com.machfour.macros.schema.FoodNutrientValueTable
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.Table
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Each query returns a single value (or set of values) that is not updated when the database changes
open class StaticDataSource(private val database: SqlDatabase): MacrosDataSource {
    override fun reset() { }

    override fun getAllFoodCategories(): Flow<Map<String, FoodCategory>> {
        return flowOf(getAllFoodCategories(database))
    }

    override fun getFood(id: EntityId): Flow<Food?> {
        return flowOf(getFoodById(database, id))
    }

    override fun getFoods(ids: Collection<EntityId>, preserveOrder: Boolean): Flow<Map<EntityId, Food>> {
        return flowOf(getFoodsById(database, ids, preserveOrder))
    }

    override fun getAllFoods(): Flow<Map<EntityId, Food>> {
        return flowOf(getAllFoodsMap(database))
    }

    override fun getMeal(id: EntityId): Flow<Meal?> {
        return flowOf(getMealById(database, id))
    }

    override fun getMeals(ids: Collection<EntityId>): Flow<Map<EntityId, Meal>> {
        return flowOf(getMealsById(database, ids))
    }

    override fun getMealsForDay(day: DateStamp): Flow<Map<EntityId, Meal>> {
        return flowOf(getMealsForDay(database, day))
    }

    override fun getFoodIdByIndexName(indexName: String): EntityId? {
        return getFoodIdByIndexName(database, indexName)
    }

    override fun getParentFoodIdsContainingFoodIds(foodIds: List<EntityId>): List<EntityId> {
        return getParentFoodIdsContainingFoodIds(database, foodIds)
    }

    override fun getMealIdsForDay(day: DateStamp): List<EntityId> {
        return getMealIdsForDay(database, day)
    }

    override fun getMealIdsForFoodIds(foodIds: Collection<EntityId>): List<EntityId> {
        return getMealIdsForFoodIds(database, foodIds)
    }

    override fun getMealIdsForFoodPortionIds(foodPortionIds: Collection<EntityId>): List<EntityId> {
        return getMealIdsForFoodPortionIds(database, foodPortionIds)
    }


    override fun getDaysForMealIds(mealIds: Collection<EntityId>): List<DateStamp> {
        return getDaysForMealIds(database, mealIds)
    }

    override fun getCommonQuantities(foodId: EntityId, limit: Int): List<Pair<IQuantity, EntityId?>> {
        return getCommonQuantities(database, foodId, limit)
    }

    override fun <I: MacrosEntity, M: I> deleteObjects(table: Table<I, M>, objects: Collection<I>): Int {
        return deleteObjects(database, table, objects)
    }

    override fun <I : MacrosEntity, M: I> saveObjects(
        table: Table<I, M>,
        objects: Collection<I>,
        source: ObjectSource
    ): Int {
        return saveObjects(database, table, objects, source)
    }

    override fun <I: MacrosEntity, M: I> saveObjectsReturningIds(
        table: Table<I, M>,
        objects: Collection<I>,
        source: ObjectSource
    ): List<EntityId> {
        return saveObjectsReturningIds(database, table, objects, source)
    }

    override fun saveNutrientsToFood(foodId: EntityId, nutrients: List<FoodNutrientValue>) {
        val (insertObjects, updateObjects) = nutrients.partition { it.source == ObjectSource.USER_NEW }

        // add Food ID to data maps
        val completedInsertObjects = insertObjects.map {
            val data = it.toRowData()
            data.put(FoodNutrientValueTable.FOOD_ID, foodId)
            FoodNutrientValue.factory.construct(data, ObjectSource.USER_NEW)
        }

        var openedNewConnection = false
        try {
            openedNewConnection = database.openConnection()
            database.beginTransaction()

            saveObjects(FoodNutrientValueTable, completedInsertObjects, ObjectSource.USER_NEW)
            saveObjects(FoodNutrientValueTable, updateObjects, ObjectSource.DB_EDIT)
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

    override fun foodSearch(keywords: List<String>, matchAll: Boolean, maxResults: Int, minRelevance: SearchRelevance): Set<EntityId> {
        return foodSearch(database, keywords, matchAll, maxResults, minRelevance)
    }

    override fun foodSearch(keyword: String, maxResults: Int, minRelevance: SearchRelevance): Set<EntityId> {
        return foodSearch(database, keyword)
    }

    override fun recentFoodIds(howMany: Int, distinct: Boolean): List<EntityId> {
        return recentFoodIds(database, howMany, distinct)
    }

    override fun recentMealIds(howMany: Int, nameFilter: Collection<String>): List<EntityId> {
        return recentMealIds(database, howMany, nameFilter)
    }
}