package com.machfour.macros.queries

import com.machfour.datestamp.DateStamp
import com.machfour.macros.core.EntityId
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.SearchRelevance
import com.machfour.macros.entities.*
import com.machfour.macros.entities.Unit
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
    ): Set<Long>

    @Throws(SqlException::class)
    fun foodSearch(
        keyword: String,
        maxResults: Int = -1,
        minRelevance: SearchRelevance = SearchRelevance.EXCLUDE_HIDDEN,
    ): Set<Long>

    @Throws(SqlException::class)
    fun recentFoodIds(howMany: Int, distinct: Boolean): List<Long>

    @Throws(SqlException::class)
    fun recentMealIds(howMany: Int, nameFilter: Collection<String>): List<Long>

    /*
     * Single-shot functions, just pass through to static queries
     */
    @Throws(SqlException::class)
    fun getFoodIdByIndexName(indexName: String): Long?

    @Throws(SqlException::class)
    fun getMealIdsForDay(day: DateStamp): List<Long>

    @Throws(SqlException::class)
    fun getDaysForMealIds(mealIds: Collection<Long>): List<DateStamp>

    @Throws(SqlException::class)
    fun getMealIdsForFoodIds(foodIds: Collection<Long>): List<Long>

    @Throws(SqlException::class)
    fun getCommonQuantities(foodId: Long, limit: Int = -1): List<Triple<Double, Unit, String?>>

    /*
     * Flow functions -- update cache
     */

    @Throws(SqlException::class)
    fun getAllFoodCategories(): Flow<Map<String, FoodCategory>>

    @Throws(SqlException::class)
    fun getFood(id: Long): Flow<Food?>

    @Throws(SqlException::class)
    fun getFoods(ids: Collection<Long>, preserveOrder: Boolean = false): Flow<Map<Long, Food>>

    @Throws(SqlException::class)
    fun getAllFoods(): Flow<Map<Long, Food>>

    @Throws(SqlException::class)
    fun getParentFoodIdsContainingFoodIds(foodIds: List<Long>): List<Long>

    @Throws(SqlException::class)
    fun getMeal(id: Long): Flow<Meal?>

    @Throws(SqlException::class)
    fun getMeals(ids: Collection<Long>): Flow<Map<Long, Meal>>

    @Throws(SqlException::class)
    fun getMealsForDay(day: DateStamp): Flow<Map<Long, Meal>>

    fun getMealIdsForFoodPortionIds(foodPortionIds: Collection<Long>): List<Long>

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
    fun <M: MacrosEntity> saveObject(table: Table<M>, o: M): Int {
        return saveObjects(table, listOf(o), o.source)
    }

    @Throws(SqlException::class)
    fun <M : MacrosEntity> saveObjects(table: Table<M>, objects: Collection<M>, source: ObjectSource): Int

    @Throws(SqlException::class)
    fun <M: MacrosEntity> saveObjectReturningId(table: Table<M>, o: M): EntityId {
        return saveObjectsReturningIds(table, listOf(o), o.source).first()
    }

    @Throws(SqlException::class)
    fun <M : MacrosEntity> saveObjectsReturningIds(table: Table<M>, objects: Collection<M>, source: ObjectSource): List<EntityId>

    // The following two methods are made redundant because of saveObjects()

    //@Throws(SqlException::class)
    //fun <M : MacrosEntity<M>> insertObjects(objects: Collection<M>, withId: Boolean): Int

    //@Throws(SqlException::class)
    //fun <M : MacrosEntity<M>> updateObjects(objects: Collection<M>): Int

    // returns number of objects saved correctly (i.e. 0 or 1)
    // NB: not (yet) possible to return the ID of the saved object with SQLite JDBC

    @Throws(SqlException::class)
    fun <M : MacrosEntity> deleteObject(table: Table<M>, o: M): Int {
        return deleteObjects(table, listOf(o))
    }

    // TODO make this the general one
    @Throws(SqlException::class)
    fun <M : MacrosEntity> deleteObjects(table: Table<M>, objects: Collection<M>): Int

    @Throws(SqlException::class)
    fun saveNutrientsToFood(foodId: EntityId, nutrients: List<FoodNutrientValue>)

    @Throws(SqlException::class)
    fun forgetFood(f: Food)

}