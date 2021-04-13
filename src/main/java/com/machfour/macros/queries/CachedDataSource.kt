package com.machfour.macros.queries

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.Table
import com.machfour.macros.core.schema.FoodTable
import com.machfour.macros.core.schema.MealTable
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.entities.Meal
import com.machfour.macros.entities.Serving
import com.machfour.macros.persistence.MacrosDatabase
import java.sql.SQLException

class CachedDataSource(database: MacrosDatabase): UncachedDataSource(database) {
    /*
     * Object caches
     */
    private val allFoodsCache: List<Food> = ArrayList(100)
    private val mealCache: MutableMap<Long, Meal> = LinkedHashMap(100)
    private val foodCache: MutableMap<Long, Food> = LinkedHashMap(100)
    private var allFoodsNeedsRefresh: Boolean = true

    @Throws(SQLException::class)
    override fun <M : MacrosEntity<M>> deleteObject(o: M): Int {
        onDbWrite(o)
        return super.deleteObject(o)
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> deleteObjects(objects: List<M>): Int {
        for (obj in objects) {
            onDbWrite(obj)
        }
        return super.deleteObjects(objects)
    }

    @Throws(SQLException::class)
    override fun getAllFoodsMap(): Map<Long, Food> {
        if (foodCache.isEmpty() || allFoodsNeedsRefresh) {
            val foods = super.getAllFoodsMap()
            foodCache.clear()
            foodCache.putAll(foods)
            allFoodsNeedsRefresh = false
        }
        return foodCache
    }

    @Throws(SQLException::class)
    override fun getMealsById(mealIds: Collection<Long>): Map<Long, Meal> {
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
        return super.getMealsById(mealIds).also {
            mealCache.putAll(it)
        }
    }

    @Throws(SQLException::class)
    override fun <M : MacrosEntity<M>> saveObject(o: M): Int {
        onDbWrite(o)
        return super.saveObject(o)
    }

    @Throws(SQLException::class)
    override fun <M : MacrosEntity<M>> saveObjects(objects: Collection<M>, source: ObjectSource): Int {
        objects.forEach { onDbWrite(it) }
        return super.saveObjects(objects, source)
    }

    @Throws(SQLException::class)
    override fun <M : MacrosEntity<M>> updateObjects(objects: Collection<M>): Int {
        objects.forEach { onDbWrite(it) }
        return super.updateObjects(objects)
    }

    @Throws(SQLException::class)
    override fun <M : MacrosEntity<M>> insertObjects(objects: Collection<M>, withId: Boolean): Int {
        objects.forEach { onDbWrite(it) }
        return super.updateObjects(objects)
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