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
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.Table
import kotlinx.coroutines.flow.Flow

// Implements a higher level query interface than the database
// which allows for caching of objects returned from query results.
interface MacrosDataSource {
    // Marks upstream database as invalid, clearing any caches
    fun reset()

    @Throws(SqlException::class)
    fun foodSearch(
        keywords: List<String>,
        matchAll: Boolean = true,
        maxResults: Int = -1,
        minRelevance: SearchRelevance = SearchRelevance.EXCLUDE_HIDDEN,
    ): Set<EntityId>

    @Throws(SqlException::class)
    fun foodSearch(
        keyword: String,
        maxResults: Int = -1,
        minRelevance: SearchRelevance = SearchRelevance.EXCLUDE_HIDDEN,
    ): Set<EntityId>

    @Throws(SqlException::class)
    fun recentFoodIds(howMany: Int, distinct: Boolean): List<EntityId>

    @Throws(SqlException::class)
    fun recentMealIds(howMany: Int, nameFilter: Collection<String>): List<EntityId>

    /*
     * Single-shot functions, just pass through to static queries
     */
    @Throws(SqlException::class)
    fun getFoodIdByIndexName(indexName: String): EntityId?

    @Throws(SqlException::class)
    fun getMealIdsForDay(day: DateStamp): List<EntityId>

    @Throws(SqlException::class)
    fun getDaysForMealIds(mealIds: Collection<EntityId>): List<DateStamp>

    @Throws(SqlException::class)
    fun getMealIdsForFoodIds(foodIds: Collection<EntityId>): List<EntityId>

    @Throws(SqlException::class)
    fun getCommonQuantities(foodId: EntityId, limit: Int = -1): List<Pair<IQuantity, EntityId?>>

    /*
     * Flow functions -- update cache
     */

    @Throws(SqlException::class)
    fun getAllFoodCategories(): Flow<Map<String, FoodCategory>>

    @Throws(SqlException::class)
    fun getFood(id: EntityId): Flow<Food?>

    @Throws(SqlException::class)
    fun getFoods(ids: Collection<EntityId>, preserveOrder: Boolean = false): Flow<Map<EntityId, Food>>

    @Throws(SqlException::class)
    fun getAllFoods(): Flow<Map<EntityId, Food>>

    @Throws(SqlException::class)
    fun getParentFoodIdsContainingFoodIds(foodIds: List<EntityId>): List<EntityId>

    @Throws(SqlException::class)
    fun getMeal(id: EntityId): Flow<Meal?>

    @Throws(SqlException::class)
    fun getMeals(ids: Collection<EntityId>): Flow<Map<EntityId, Meal>>

    @Throws(SqlException::class)
    fun getMealsForDay(day: DateStamp): Flow<Map<EntityId, Meal>>

    fun getMealIdsForFoodPortionIds(foodPortionIds: Collection<EntityId>): List<EntityId>

    /*
     * The functions below mutate the database and require cache updates
     */

    /* These functions save the objects given to them into the database, via INSERT or UPDATE.
     * The caller should ensure that objects with an id of null correspond to new entries
     * (INSERTs) into the database, while those with a non-null id correspond to UPDATES of existing
     * rows in the table.
     *
     * Any data that originated from the user should already have been validated
     */
    // Do we really need the list methods? The user will probably only edit one object at a time
    // except for deleting a bunch of foodPortions from one meal, or servings from a food

    @Throws(SqlException::class)
    fun <I: MacrosEntity, M: I> saveObject(table: Table<I, M>, o: I): Int {
        return saveObjects(table, listOf(o), o.source)
    }

    @Throws(SqlException::class)
    fun <I: MacrosEntity, M: I> saveObjects(table: Table<I, M>, objects: Collection<I>, source: ObjectSource): Int

    @Throws(SqlException::class)
    fun <I: MacrosEntity, M: I> saveObjectReturningId(table: Table<I, M>, o: I): EntityId {
        return saveObjectsReturningIds(table, listOf(o), o.source).first()
    }

    @Throws(SqlException::class)
    fun <I: MacrosEntity, M: I> saveObjectsReturningIds(table: Table<I, M>, objects: Collection<I>, source: ObjectSource): List<EntityId>

    // The following two methods are made redundant because of saveObjects()

    //@Throws(SqlException::class)
    //fun <M : MacrosEntity<M>> insertObjects(objects: Collection<M>, withId: Boolean): Int

    //@Throws(SqlException::class)
    //fun <M : MacrosEntity<M>> updateObjects(objects: Collection<M>): Int

    // returns number of objects saved correctly (i.e. 0 or 1)
    // NB: not (yet) possible to return the ID of the saved object with SQLite JDBC

    @Throws(SqlException::class)
    fun <I: MacrosEntity, M: I> deleteObject(table: Table<I, M>, o: I): Int {
        return deleteObjects(table, listOf(o))
    }

    // TODO make this the general one
    @Throws(SqlException::class)
    fun <I: MacrosEntity, M: I> deleteObjects(table: Table<I, M>, objects: Collection<I>): Int

    @Throws(SqlException::class)
    fun saveNutrientsToFood(foodId: EntityId, nutrients: List<FoodNutrientValue>)

    @Throws(SqlException::class)
    fun forgetFood(f: Food)

}
