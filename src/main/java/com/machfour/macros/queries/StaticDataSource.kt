package com.machfour.macros.queries

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.Table
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodCategory
import com.machfour.macros.entities.Meal
import com.machfour.macros.entities.Serving
import com.machfour.macros.persistence.MacrosDatabase
import com.machfour.macros.util.DateStamp
import kotlinx.coroutines.flow.flowOf

// Each query returns a single value (or set of values) that is not updated when the database changes
open class StaticDataSource(override val database: MacrosDatabase): MacrosDataSource {

    override fun beginTransaction() {
        database.beginTransaction()
    }

    override fun endTransaction() {
        database.endTransaction()
    }

    // Flow functions
    override fun getFood(id: Long): Food? {
        return getFoodById(id)
    }

    override fun getFood(indexName: String): Food? {
        return getFoodByIndexName(indexName)
    }

    override fun getFoods(ids: Collection<Long>): Map<Long, Food> {
        return getFoodsById(ids)
    }

    override fun getAllFoods(): Map<Long, Food> {
        return getAllFoodsMap()
    }

    override fun getMeal(id: Long): Meal? {
        return getMealById(id)
    }

    override fun getMeals(date: DateStamp): Map<Long, Meal> {
        return getMealsForDay(date)
    }

    override fun getMeals(ids: Collection<Long>): Map<Long, Meal> {
        return getMealsById(ids)
    }

    // TODO remove all the methods below from the interface and make them protected

    override fun getAllFoodCategories(): Map<String, FoodCategory> {
        return FoodQueries.getAllFoodCategories(database)
    }

    override fun getFoodByIndexName(indexName: String): Food? {
        return FoodQueries.getFoodByIndexName(database, indexName)
    }

    override fun getFoodById(id: Long): Food? {
        return FoodQueries.getFoodById(database, id)
    }

    override fun getFoodIdsByIndexName(indexNames: Collection<String>): Map<String, Long> {
        return FoodQueries.getFoodIdsByIndexName(database, indexNames)
    }

    override fun getFoodIdByIndexName(indexName: String): Long? {
        return FoodQueries.getFoodIdByIndexName(database, indexName)
    }

    override fun getAllFoodsMap(): Map<Long, Food> {
        return FoodQueries.getAllFoodsMap(database)
    }

    override fun getFoodsById(foodIds: Collection<Long>, preserveOrder: Boolean): Map<Long, Food> {
        return FoodQueries.getFoodsById(database, foodIds, preserveOrder)
    }

    override fun getServingsById(servingIds: Collection<Long>): Map<Long, Serving> {
        return FoodQueries.getServingsById(database, servingIds)
    }

    override fun getFoodsByIndexName(indexNames: Collection<String>): Map<String, Food> {
        return FoodQueries.getFoodsByIndexName(database, indexNames)
    }

    override fun getParentFoodIdsContainingFoodIds(foodIds: List<Long>): List<Long> {
        return FoodQueries.getParentFoodIdsContainingFoodIds(database, foodIds)
    }

    override fun getMealsForDay(day: DateStamp): Map<Long, Meal> {
        return MealQueries.getMealsForDay(database, day)
    }

    override fun getMealForDayWithName(day: DateStamp, name: String): Meal? {
        return MealQueries.getMealForDayWithName(database, day, name)
    }

    override fun getMealIdsForDay(day: DateStamp): List<Long> {
        return MealQueries.getMealIdsForDay(database, day)
    }

    override fun getMealById(id: Long): Meal? {
        return MealQueries.getMealById(database, id)
    }

    override fun getMealsById(mealIds: Collection<Long>): Map<Long, Meal> {
        return MealQueries.getMealsById(database, mealIds)
    }

    override fun getMealIdsForFoodIds(foodIds: Collection<Long>): List<Long> {
        return MealQueries.getMealIdsForFoodIds(database, foodIds)
    }

    override fun getDaysForMealIds(mealIds: Collection<Long>): List<DateStamp> {
        return MealQueries.getDaysForMealIds(database, mealIds)
    }

    override fun <M : MacrosEntity<M>> insertObjects(objects: Collection<M>, withId: Boolean): Int {
        return WriteQueries.insertObjects(database, objects, withId)
    }

    override fun <M : MacrosEntity<M>> updateObjects(objects: Collection<M>): Int {
        return WriteQueries.updateObjects(database, objects)
    }

    override fun <M : MacrosEntity<M>> deleteObject(o: M): Int {
        return WriteQueries.deleteObject(database, o)
    }

    override fun <M : MacrosEntity<M>> deleteObjects(objects: Collection<M>): Int {
        return WriteQueries.deleteObjects(database, objects)
    }

    override fun <M : MacrosEntity<M>> deleteObjectsById(
        table: Table<M>,
        ids: Collection<Long>
    ): Int {
        return WriteQueries.deleteObjectsById(database, table, ids)
    }

    override fun <M : MacrosEntity<M>> saveObject(o: M): Int {
        return WriteQueries.saveObject(database, o)
    }

    override fun <M : MacrosEntity<M>> saveObjects(
        objects: Collection<M>,
        source: ObjectSource
    ): Int {
        return WriteQueries.saveObjects(database, objects, source)
    }

    override fun deleteAllIngredients() {
        return WriteQueries.deleteAllIngredients(database)
    }

    override fun deleteAllFoodPortions() {
        return WriteQueries.deleteAllFoodPortions(database)
    }

    override fun deleteAllCompositeFoods(): Int {
        return WriteQueries.deleteAllCompositeFoods(database)
    }

    override fun forgetFood(f: Food) {
        return WriteQueries.forgetFood(database, f)
    }

    /*
     * Passthrough fuunctions
     */

    override fun foodSearch(keywords: List<String>, matchAll: Boolean): Set<Long> {
        return FoodQueries.foodSearch(database, keywords, matchAll)
    }

    override fun foodSearch(keyword: String): Set<Long> {
        return FoodQueries.foodSearch(database, keyword)
    }

    override fun recentFoodIds(howMany: Int): List<Long> {
        return FoodPortionQueries.recentFoodIds(database, howMany)
    }

}