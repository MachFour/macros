package com.machfour.macros.queries

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.entities.*
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.Table
import com.machfour.macros.util.DateStamp
import java.sql.SQLException

// Implements a higher level query interface than the database
// which allows for caching of objects returned from query results.
interface MacrosDataSource {

    // TODO don't expose this publicly
    val database: SqlDatabase

    /*
     * The following functions are just simple passthoughs to static queries
     */

    @Throws(SQLException::class)
    fun foodSearch(keywords: List<String>, matchAll: Boolean = true): Set<Long>

    @Throws(SQLException::class)
    fun foodSearch(keyword: String): Set<Long>

    @Throws(SQLException::class)
    fun recentFoodIds(howMany: Int) : List<Long>


    /*
     * The functions below do not mutate the database but are useful to populate the cache
     */

    /*
     * FoodQueries
     */
    @Throws(SQLException::class)
    fun getAllFoodCategories(): Map<String, FoodCategory>

    @Throws(SQLException::class)
    fun getFoodByIndexName(indexName: String): Food?

    @Throws(SQLException::class)
    fun getFoodById(id: Long): Food?

    // creates a map of entries from SELECT index_name, id FROM Food WHERE index_name IN (indexNames)
    // items in indexNames that do not correspond to a food, will not appear in the output map
    @Throws(SQLException::class)
    fun getFoodIdsByIndexName(indexNames: Collection<String>): Map<String, Long>

    @Throws(SQLException::class)
    fun getFoodIdByIndexName(indexName: String): Long?

    // The proper way to get all foods
    fun getAllFoodsMap(): Map<Long, Food>

    @Throws(SQLException::class)
    fun getFoodsById(foodIds: Collection<Long>, preserveOrder: Boolean = false): Map<Long, Food>

    @Throws(SQLException::class)
    fun getServingsById(servingIds: Collection<Long>): Map<Long, Serving>

    /*
     * Constructs full food objects by their index name
     * Returns a map of index name to food object
     */
    @Throws(SQLException::class)
    fun getFoodsByIndexName(indexNames: Collection<String>): Map<String, Food>

    @Throws(SQLException::class)
    fun getParentFoodIdsContainingFoodIds(foodIds: List<Long>): List<Long>

    /*
     * MealQueries
     */
    @Throws(SQLException::class)
    fun getMealsForDay(day: DateStamp): Map<Long, Meal>

    // assumes unique meal name per day
    @Throws(SQLException::class)
    fun getMealForDayWithName(day: DateStamp, name: String): Meal?

    @Throws(SQLException::class)
    fun getMealIdsForDay(day: DateStamp): List<Long>

    @Throws(SQLException::class)
    fun getDaysForMealIds(mealIds: Collection<Long>): List<DateStamp>

    @Throws(SQLException::class)
    fun getMealById(id: Long): Meal?

    @Throws(SQLException::class)
    fun getMealsById(mealIds: Collection<Long>): Map<Long, Meal>

    @Throws(SQLException::class)
    fun getMealIdsForFoodIds(foodIds: Collection<Long>): List<Long>

    /*
     * Flow versions of the above functions
     */

    /*
     * FoodQueries
     */

    @Throws(SQLException::class)
    fun getFood(indexName: String): Food?

    @Throws(SQLException::class)
    fun getFood(id: Long): Food?

    @Throws(SQLException::class)
    fun getFoods(ids: Collection<Long>): Map<Long, Food>

    @Throws(SQLException::class)
    fun getAllFoods(): Map<Long, Food>

    /*
     * MealQueries
     */
    @Throws(SQLException::class)
    fun getMeals(date: DateStamp): Map<Long, Meal>

    @Throws(SQLException::class)
    fun getMeal(id: Long): Meal?

    @Throws(SQLException::class)
    fun getMeals(ids: Collection<Long>): Map<Long, Meal>

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
    fun <M : MacrosEntity<M>> insertObjects(objects: Collection<M>, withId: Boolean): Int

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> updateObjects(objects: Collection<M>): Int

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> deleteObject(o: M): Int

    // TODO make this the general one
    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> deleteObjects(objects: Collection<M>): Int

    // deletes objects with the given ID from
    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> deleteObjectsById(table: Table<M>, ids: Collection<Long>): Int

    // returns number of objects saved correctly (i.e. 0 or 1)
    // NB: not (yet) possible to return the ID of the saved object with SQLite JDBC
    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> saveObject(o: M): Int

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> saveObjects(objects: Collection<M>, source: ObjectSource): Int

    @Throws(SQLException::class)
    fun saveNutrientsToFood(food: Food, nutrients: List<FoodNutrientValue>)

    @Throws(SQLException::class)
    fun deleteAllIngredients()

    @Throws(SQLException::class)
    fun deleteAllFoodPortions()

    @Throws(SQLException::class)
    fun deleteAllCompositeFoods(): Int

    @Throws(SQLException::class)
    fun forgetFood(f: Food)

}