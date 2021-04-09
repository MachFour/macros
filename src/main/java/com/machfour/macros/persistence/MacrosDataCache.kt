package com.machfour.macros.persistence

import com.machfour.macros.core.Column
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.schema.FoodTable
import com.machfour.macros.core.schema.MealTable
import com.machfour.macros.core.Table
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.entities.Meal
import com.machfour.macros.entities.Serving
import com.machfour.macros.queries.FoodQueries
import com.machfour.macros.queries.MealQueries.getMealsById
import com.machfour.macros.queries.Queries.deleteObject
import com.machfour.macros.queries.Queries.deleteObjects
import com.machfour.macros.queries.Queries.saveObject
import com.machfour.macros.queries.Queries.saveObjects
import com.machfour.macros.queries.Queries.updateObjects
import java.sql.SQLException

class MacrosDataCache private constructor(private val upstream: MacrosDataSource) {

    companion object {
        private var INSTANCE: MacrosDataCache? = null
        fun initialise(upstream: MacrosDataSource) {
            INSTANCE = MacrosDataCache(upstream)
        }

        val instance: MacrosDataCache?
            get() {
                checkNotNull(INSTANCE) { "Not initialised with upstream data source" }
                return INSTANCE
            }
    }

    /*
     * Object caches
     */
    private val allFoodsCache: List<Food> = ArrayList(100)
    private val mealCache: MutableMap<Long, Meal> = LinkedHashMap(100)
    private val foodCache: MutableMap<Long, Food> = LinkedHashMap(100)
    private var allFoodsNeedsRefresh: Boolean = true

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> deleteObject(obj: M): Int {
        onDbWrite(obj)
        return deleteObject(upstream, obj)
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> deleteObjects(objects: List<M>): Int {
        for (obj in objects) {
            onDbWrite(obj)
        }
        return deleteObjects(upstream, objects)
    }

    @Throws(SQLException::class)
    fun allFoodsMap(): Map<Long, Food> {
        if (foodCache.isEmpty() || allFoodsNeedsRefresh) {
            val foods = FoodQueries.getAllFoodsMap(upstream)
            foodCache.clear()
            foodCache.putAll(foods)
            allFoodsNeedsRefresh = false
        }
        return foodCache
    }

    @Throws(SQLException::class)
    fun getMealsById(mealIds: List<Long>): Map<Long, Meal> {
        val unCachedIds: MutableList<Long> = ArrayList(mealIds.size)
        val mealsToReturn: MutableMap<Long, Meal> = LinkedHashMap(mealIds.size, 1.0f)
        for (id in mealIds) {
            if (mealCache.containsKey(id)) {
                mealsToReturn[id] = mealCache.getValue(id)
            } else {
                unCachedIds.add(id)
            }
        }
        val freshMeals = getUncachedMealsById(unCachedIds)
        mealsToReturn.putAll(freshMeals)
        return mealsToReturn
    }

    @Throws(SQLException::class)
    private fun getUncachedMealsById(mealIds: List<Long>): Map<Long, Meal> {
        return getMealsById(upstream, mealIds).also {
            mealCache.putAll(it)
        }
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> saveObject(obj: M): Int {
        onDbWrite(obj)
        return saveObject(upstream, obj)
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> saveObjects(objects: Collection<M>, objectSource: ObjectSource): Int {
        objects.forEach { onDbWrite(it) }
        return saveObjects(upstream, objects, objectSource)
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> updateObjects(objects: Collection<M>): Int {
        objects.forEach { onDbWrite(it) }
        return updateObjects(upstream, objects)
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> insertObjects(objects: Collection<M>, withId: Boolean): Int {
        objects.forEach { onDbWrite(it) }
        return updateObjects(upstream, objects)
    }

    @Throws(SQLException::class)
    fun <M, J> deleteByColumn(t: Table<M>, whereColumn: Column<M, J>, whereValues: Collection<J>): Int {
        return upstream.deleteByColumn(t, whereColumn, whereValues)
    }

    private fun <M : MacrosEntity<M>> onDbWrite(obj: M) {
        when (obj) {
            is Food -> {
                allFoodsNeedsRefresh = true
                unCache(obj.id, FoodTable.instance)
            }
            is Meal -> unCache(obj.id, MealTable.instance)
            is FoodPortion -> unCache(obj.mealId, MealTable.instance)
            is Serving -> unCache(obj.foodId, FoodTable.instance)
        }
    }

    private fun <M : MacrosEntity<M>> unCache(id: Long, type: Table<M>) {
        when (type) {
            is FoodTable -> foodCache.remove(id)
            is MealTable -> mealCache.remove(id)
        }
    }
}