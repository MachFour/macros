package com.machfour.macros.queries

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.SearchRelevance
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodCategory
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.Meal
import com.machfour.macros.util.DateStamp
import kotlinx.coroutines.flow.Flow
import java.sql.SQLException

// Implements a higher level query interface than the database
// which allows for caching of objects returned from query results.
interface MacrosDataSource {

    @Throws(SQLException::class)
    fun foodSearch(
        keywords: List<String>,
        matchAll: Boolean = true,
        minRelevance: Int = SearchRelevance.EXCLUDE_HIDDEN.value
    ): Set<Long>

    @Throws(SQLException::class)
    fun foodSearch(keyword: String, minRelevance: Int = SearchRelevance.EXCLUDE_HIDDEN.value): Set<Long>

    @Throws(SQLException::class)
    fun recentFoodIds(howMany: Int): List<Long>

    /*
     * Single-shot functions, just passthrough to static queries
     */
    @Throws(SQLException::class)
    fun getFoodIdByIndexName(indexName: String): Long?

    @Throws(SQLException::class)
    fun getMealIdsForDay(day: DateStamp): List<Long>

    @Throws(SQLException::class)
    fun getDaysForMealIds(mealIds: Collection<Long>): List<DateStamp>

    @Throws(SQLException::class)
    fun getMealIdsForFoodIds(foodIds: Collection<Long>): List<Long>

    /*
     * Flow functions -- update cache
     */

    @Throws(SQLException::class)
    fun getAllFoodCategories(): Flow<Map<String, FoodCategory>>

    @Throws(SQLException::class)
    fun getFood(id: Long): Flow<Food?>

    @Throws(SQLException::class)
    fun getFoods(ids: Collection<Long>, preserveOrder: Boolean = false): Flow<Map<Long, Food>>

    @Throws(SQLException::class)
    fun getAllFoods(): Flow<Map<Long, Food>>

    @Throws(SQLException::class)
    fun getParentFoodIdsContainingFoodIds(foodIds: List<Long>): List<Long>

    @Throws(SQLException::class)
    fun getMeal(id: Long): Flow<Meal?>

    @Throws(SQLException::class)
    fun getMeals(ids: Collection<Long>): Flow<Map<Long, Meal>>

    @Throws(SQLException::class)
    fun getMealsForDay(day: DateStamp): Flow<Map<Long, Meal>>

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


    @Throws(SQLException::class)
    fun <M: MacrosEntity<M>> saveObject(o: M): Int {
        return saveObjects(listOf(o), o.source)
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> saveObjects(objects: Collection<M>, source: ObjectSource): Int

    // The following two methods are made redundant because of saveObjects()

    //@Throws(SQLException::class)
    //fun <M : MacrosEntity<M>> insertObjects(objects: Collection<M>, withId: Boolean): Int

    //@Throws(SQLException::class)
    //fun <M : MacrosEntity<M>> updateObjects(objects: Collection<M>): Int

    // returns number of objects saved correctly (i.e. 0 or 1)
    // NB: not (yet) possible to return the ID of the saved object with SQLite JDBC

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> deleteObject(o: M): Int {
        return deleteObjects(listOf(o))
    }

    // TODO make this the general one
    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> deleteObjects(objects: Collection<M>): Int

    @Throws(SQLException::class)
    fun saveNutrientsToFood(food: Food, nutrients: List<FoodNutrientValue>)

    @Throws(SQLException::class)
    fun forgetFood(f: Food)

}