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
import com.machfour.macros.util.DateStamp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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

    override fun beginTransaction() {
        check (!pauseRefreshes) { "Already in transaction" }
        pauseRefreshes = true
        super.beginTransaction()
    }

    override fun endTransaction() {
        check (pauseRefreshes) { "Not in transaction" }
        pauseRefreshes = false
        super.endTransaction()

        runBlocking {
            if (allFoodsNeedsRefresh) {
                refreshAllFoods()
                allFoodsNeedsRefresh = false
            } else if (foodRefreshQueue.isNotEmpty()){
                refreshFoods(foodRefreshQueue)
            }
            if (mealRefreshQueue.isNotEmpty()) {
                refreshMeals(mealRefreshQueue)
            }
            
            foodRefreshQueue.clear()
            mealRefreshQueue.clear()
        }
    }

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
            runBlocking { refreshFood(id) }
        }
        //return foodsFlow.distinctUntilChanged { old, new -> old[id] === new[id] }.map { it[id] }
        return foods[id]
    }

    @Throws(SQLException::class)
    override fun getFoods(ids: Collection<Long>): Map<Long, Food> {
        val missingIds = foods.missingIdsFrom(ids)
        runBlocking { refreshFoods(missingIds) }

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
            runBlocking { refreshMeal(id) }
        }
        //return mealsFlow.distinctUntilChanged { old, new -> old[id] === new[id] }.map { it[id] }
        return meals[id]
    }

    @Throws(SQLException::class)
    override fun getMeals(date: DateStamp): Map<Long, Meal> {
        val ids: Set<Long> = getMealIdsForDay(date).toSet()
        //withContext(dispatcher) {}
        val missingIds = meals.missingIdsFrom(ids)
        runBlocking { refreshMeals(missingIds) }
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
    private suspend fun refreshFood(id: Long) {
        if (pauseRefreshes) {
            foodRefreshQueue.add(id)
        } else {
            foods.getAndUpdateSingle(id) { getFoodById(it) }
        }
    }

    @Throws(SQLException::class)
    private suspend fun refreshAllFoods() {
        if (pauseRefreshes) {
            allFoodsNeedsRefresh = true
        } else {
            foods.getAndUpdate { getAllFoodsMap() }
            //foodsFlow.emit(foods)
        }
    }

    @Throws(SQLException::class)
    private suspend fun refreshFoods(ids: Collection<Long>) {
        if (pauseRefreshes) {
            foodRefreshQueue.addAll(ids)
        } else {
            foods.getAndUpdate { getFoodsById(ids) }
            //foodsFlow.emit(foods)
        }
    }

    @Throws(SQLException::class)
    private suspend fun refreshMeal(id: Long) {
        if (pauseRefreshes) {
           mealRefreshQueue.add(id)
        } else {
            meals.getAndUpdateSingle(id) { getMealById(it) }
            //mealsFlow.emit(meals)
        }
    }

    @Throws(SQLException::class)
    private suspend fun refreshMeals(ids: Collection<Long>) {
        if (pauseRefreshes) {
            mealRefreshQueue.addAll(ids)
        } else {
            meals.getAndUpdate { getMealsById(ids) }
            //mealsFlow.emit(meals)
        }
    }

    private suspend fun <T> MutableMap<Long, T>.getAndUpdate(getNewData: () -> Map<Long, T>) {
        val newData: Map<Long, T>
        withContext(dispatcher) {
            newData = getNewData()
        }
        putAll(newData)
    }

    private suspend fun <T> MutableMap<Long, T>.getAndUpdateSingle(id: Long, getId: (Long) -> T?) {
        val newObj: T?
        withContext(dispatcher) {
            newObj = getId(id)
        }
        when (newObj == null) {
            true -> remove(id) // remove map entry if it existed
            else -> this[id] = newObj
        }
    }

    @Throws(SQLException::class)
    override fun <M : MacrosEntity<M>> deleteObject(o: M): Int {
        val numDeleted = super.deleteObject(o)
        afterDbWrite(o)
        return numDeleted
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> deleteObjects(objects: List<M>): Int {
        val numDeleted = super.deleteObjects(objects)
        for (obj in objects) {
            afterDbWrite(obj)
        }
        return numDeleted
    }


    @Throws(SQLException::class)
    override fun <M : MacrosEntity<M>> saveObject(o: M): Int {
        val wasSaved = super.saveObject(o)
        afterDbWrite(o)
        return wasSaved
    }

    @Throws(SQLException::class)
    override fun <M : MacrosEntity<M>> saveObjects(objects: Collection<M>, source: ObjectSource): Int {
        val numSaved = super.saveObjects(objects, source)
        objects.forEach { afterDbWrite(it) }
        return numSaved
    }

    @Throws(SQLException::class)
    override fun <M : MacrosEntity<M>> updateObjects(objects: Collection<M>): Int {
        val numUpdated = super.updateObjects(objects)
        objects.forEach { afterDbWrite(it) }
        return numUpdated
    }

    @Throws(SQLException::class)
    override fun <M : MacrosEntity<M>> insertObjects(objects: Collection<M>, withId: Boolean): Int {
        val numInserted = super.insertObjects(objects, withId)
        objects.forEach { afterDbWrite(it) }
        return numInserted
    }

    private fun <M : MacrosEntity<M>> afterDbWrite(obj: M) {
        when (obj) {
            is Food -> {
                allFoodsNeedsRefresh = true
                afterDbWrite(obj.id, FoodTable.instance)
            }
            is Meal -> afterDbWrite(obj.id, MealTable.instance)
            is FoodPortion -> afterDbWrite(obj.mealId, MealTable.instance)
            is Serving -> afterDbWrite(obj.foodId, FoodTable.instance)
        }
    }

    private fun <M : MacrosEntity<M>> afterDbWrite(id: Long, cacheType: Table<M>) {
        when (cacheType) {
            is FoodTable -> runBlocking { refreshFood(id) }
            is MealTable -> runBlocking { refreshMeal(id) }
        }
    }
}