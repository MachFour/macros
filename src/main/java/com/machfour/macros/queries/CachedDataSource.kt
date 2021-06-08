package com.machfour.macros.queries

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.Table
import com.machfour.macros.core.schema.FoodTable
import com.machfour.macros.core.schema.MealTable
import com.machfour.macros.entities.*
import com.machfour.macros.persistence.MacrosDatabase
import com.machfour.macros.util.DateStamp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.sql.SQLException

class CachedDataSource(
    database: MacrosDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
): StaticDataSource(database) {
    private var allFoodsNeedsRefresh: Boolean = true

    private val foodRefreshQueue = HashSet<Long>()
    private val mealRefreshQueue = HashSet<Long>()
    private var pauseRefreshes = false

    private val foods: MutableMap<Long, Food> = LinkedHashMap(100)
    private val meals: MutableMap<Long, Meal> = LinkedHashMap(100)

    // need to use SharedFlow we want to emit a new value when servings (or ingredients)
    // are updated, so using == doesn't work.
    //private val foodsFlow: MutableSharedFlow<Map<Long, Food>> = MutableSharedFlow(
    //    replay = 1,
    //    onBufferOverflow = BufferOverflow.DROP_OLDEST,
    //)

    //// same for meals (= foods) and foodportions (= servings)
    //private val mealsFlow: MutableSharedFlow<Map<Long, Meal>> = MutableSharedFlow(
    //    replay = 1,
    //    onBufferOverflow = BufferOverflow.DROP_OLDEST
    //)

    //override fun beginTransaction() {
    //    check (!pauseRefreshes) { "Already in transaction" }
    //    pauseRefreshes = true
    //    super.beginTransaction()
    //}

    //override fun endTransaction() {
    //    check (pauseRefreshes) { "Not in transaction" }
    //    pauseRefreshes = false
    //    super.endTransaction()

    //    runBlocking {
    //        if (allFoodsNeedsRefresh) {
    //            refreshAllFoods()
    //            allFoodsNeedsRefresh = false
    //        } else if (foodRefreshQueue.isNotEmpty()){
    //            refreshFoods(foodRefreshQueue)
    //        }
    //        if (mealRefreshQueue.isNotEmpty()) {
    //            refreshMeals(mealRefreshQueue)
    //        }
    //
    //        foodRefreshQueue.clear()
    //        mealRefreshQueue.clear()
    //    }
    //}

    @Throws(SQLException::class)
    override fun getFood(indexName: String): Food? {
        val id: Long
        runBlocking {
            id = getFoodIdByIndexName(indexName) ?: MacrosEntity.NO_ID
        }
        return getFood(id)
    }

    private fun <T> Map<Long, T>.idMissing(id: Long): Boolean {
        return !containsKey(id) && id != MacrosEntity.NO_ID
    }

    private fun <T> Map<Long, T>.missingIdsFrom(ids: Collection<Long>): List<Long> {
        return ids.filter { idMissing(it) }
    }

    @Throws(SQLException::class)
    override fun getFood(id: Long): Food? {
        if (foods.idMissing(id)) {
            runBlocking {
                refreshFoods(listOf(id))
            }
        }
        //return foodsFlow.distinctUntilChanged { old, new -> old[id] === new[id] }.map { it[id] }
        return foods[id]
    }

    @Throws(SQLException::class)
    override fun getFoods(ids: Collection<Long>): Map<Long, Food> {
        val missingIds = foods.missingIdsFrom(ids)
        runBlocking {
            refreshFoods(missingIds)
        }

        val idSet = ids.toHashSet()
        //return foodsFlow.map { foods -> foods.filter { idSet.contains(it.key) } }
        return foods.filter { idSet.contains(it.key) }
    }

    @Throws(SQLException::class)
    override fun getAllFoods(): Map<Long, Food> {
        if (allFoodsNeedsRefresh) {
            runBlocking { refreshAllFoods() }
            allFoodsNeedsRefresh = false
        }
        //return foodsFlow
        return foods
    }

    @Throws(SQLException::class)
    override fun getMeal(id: Long): Meal? {
        if (meals.idMissing(id)) {
            runBlocking {
                refreshMeals(listOf(id))
            }
        }
        //return mealsFlow.distinctUntilChanged { old, new -> old[id] === new[id] }.map { it[id] }
        return meals[id]
    }

    @Throws(SQLException::class)
    override fun getMeals(date: DateStamp): Map<Long, Meal> {
        val ids: Set<Long> = getMealIdsForDay(date).toSet()
        //withContext(dispatcher) {}
        val missingIds = meals.missingIdsFrom(ids)
        runBlocking {
            refreshMeals(missingIds)
        }
        //return mealsFlow.map { meals -> meals.filter { ids.contains(it.key) } }
        return meals.filter { ids.contains(it.key) }
    }

    @Throws(SQLException::class)
    override fun getMeals(ids: Collection<Long>): Map<Long, Meal> {
        val missingIds = meals.missingIdsFrom(ids)
        runBlocking { refreshMeals(missingIds) }
        val idSet = ids.toHashSet()
        //return mealsFlow.map { meals -> meals.filter { idSet.contains(it.key) } }
        return meals.filter { idSet.contains(it.key) }
    }

    @Throws(SQLException::class)
    private fun refreshAllFoods() {
        if (pauseRefreshes) {
            allFoodsNeedsRefresh = true
        } else {
            val newData: Map<Long, Food> = getAllFoodsMap()
            foods.putAll(newData)
            //foodsFlow.emit(foods)
        }
    }

    @Throws(SQLException::class)
    private fun refreshFoods(ids: Collection<Long>) {
        if (pauseRefreshes) {
            foodRefreshQueue.addAll(ids)
        } else {
            val newData: Map<Long, Food> = getFoodsById(ids)
            foods.putAll(newData)
            for (missingId in newData.missingIdsFrom(ids)) {
                foods.remove(missingId)
            }
            //foodsFlow.emit(foods)
            
            // TODO refresh meals containing these foods
        }
    }

    @Throws(SQLException::class)
    private fun refreshMeals(ids: Collection<Long>) {
        if (pauseRefreshes) {
            mealRefreshQueue.addAll(ids)
        } else {
            val newData: Map<Long, Meal> = getMealsById(ids)
            meals.putAll(newData)
            for (missingId in newData.missingIdsFrom(ids)) {
                meals.remove(missingId)
            }

            //mealsFlow.emit(meals)
        }
    }

    @Throws(SQLException::class)
    override fun <M : MacrosEntity<M>> deleteObject(o: M): Int {
        val numDeleted = super.deleteObject(o)
        afterDbChange(o)
        return numDeleted
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> deleteObjects(objects: List<M>): Int {
        val numDeleted = super.deleteObjects(objects)
        for (obj in objects) {
            afterDbChange(obj)
        }
        return numDeleted
    }


    @Throws(SQLException::class)
    override fun <M : MacrosEntity<M>> saveObject(o: M): Int {
        val wasSaved = super.saveObject(o)
        afterDbChange(o)
        return wasSaved
    }

    @Throws(SQLException::class)
    override fun <M : MacrosEntity<M>> saveObjects(objects: Collection<M>, source: ObjectSource): Int {
        val numSaved = super.saveObjects(objects, source)
        objects.forEach { afterDbChange(it) }
        return numSaved
    }

    @Throws(SQLException::class)
    override fun <M : MacrosEntity<M>> updateObjects(objects: Collection<M>): Int {
        val numUpdated = super.updateObjects(objects)
        objects.forEach { afterDbChange(it) }
        return numUpdated
    }

    @Throws(SQLException::class)
    override fun <M : MacrosEntity<M>> insertObjects(objects: Collection<M>, withId: Boolean): Int {
        val numInserted = super.insertObjects(objects, withId)
        objects.forEach { afterDbChange(it) }
        return numInserted
    }

    private fun <M : MacrosEntity<M>> afterDbChange(obj: M) {
        when (obj) {
            is Food -> afterDbChange(obj.id, FoodTable.instance)
            is Meal -> afterDbChange(obj.id, MealTable.instance)

            is FoodNutrientValue -> afterDbChange(obj.foodId, FoodTable.instance)
            is FoodPortion -> afterDbChange(obj.mealId, MealTable.instance)
            is Serving -> afterDbChange(obj.foodId, FoodTable.instance)
        }
    }

    private fun <M : MacrosEntity<M>> afterDbChange(id: Long, cacheType: Table<M>) {
        when (cacheType) {
            is FoodTable -> foods.remove(id)
            is MealTable -> meals.remove(id)
        }
    }
}