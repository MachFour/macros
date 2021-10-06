package com.machfour.macros.queries

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.*
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.MealTable
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.Table
import com.machfour.macros.util.DateStamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.sql.SQLException

class FlowDataSource(
    private val database: SqlDatabase,
): StaticDataSource(database) {
    var allFoodsNeedsRefresh: Boolean = true

    private val foodRefreshQueue = HashSet<Long>()
    private val mealRefreshQueue = HashSet<Long>()
    private var pauseRefreshes = false

    private val foods: MutableMap<Long, Food> = LinkedHashMap(100)
    private val meals: MutableMap<Long, Meal> = LinkedHashMap(100)

    private val foodsFlow: MutableStateFlow<Map<Long, Food>> = MutableStateFlow(emptyMap())
    private val mealsFlow: MutableStateFlow<Map<Long, Meal>> = MutableStateFlow(emptyMap())

    private fun <T> Map<Long, T>.idMissing(id: Long): Boolean {
        return !containsKey(id) && id != MacrosEntity.NO_ID
    }

    private fun <T> Map<Long, T>.missingIdsFrom(ids: Collection<Long>): List<Long> {
        return ids.filter { idMissing(it) }
    }

    @Throws(SQLException::class)
    override fun getFood(id: Long): Flow<Food?> {
        if (foods.idMissing(id)) {
            refreshFoods(listOf(id))
        }
        return foodsFlow.map { it[id] }
    }

    @Throws(SQLException::class)
    override fun getFoods(ids: Collection<Long>, preserveOrder: Boolean): Flow<Map<Long, Food>> {
        val missingIds = foods.missingIdsFrom(ids)
        refreshFoods(missingIds)

        val idSet = ids.toHashSet()
        return foodsFlow.map { foods -> foods.filterKeys { idSet.contains(it) } }
    }

    @Throws(SQLException::class)
    override fun getAllFoods(): Flow<Map<Long, Food>> {
        if (allFoodsNeedsRefresh) {
            refreshAllFoods()
        }
        return foodsFlow
    }

    @Throws(SQLException::class)
    override fun getMeal(id: Long): Flow<Meal?> {
        if (meals.idMissing(id)) {
            refreshMeals(listOf(id))
        }
        return mealsFlow.map { it[id] }
    }

    @Throws(SQLException::class)
    override fun getMealsForDay(day: DateStamp): Flow<Map<Long, Meal>> {
        val ids = getMealIdsForDay(day)
        return getMeals(ids)
    }

    @Throws(SQLException::class)
    override fun getMeals(ids: Collection<Long>): Flow<Map<Long, Meal>> {
        val missingIds = meals.missingIdsFrom(ids)
        refreshMeals(missingIds)
        val idSet = ids.toHashSet()

        return mealsFlow.map { meals -> meals.filterKeys { idSet.contains(it) } }
    }

    @Throws(SQLException::class)
    private fun refreshAllFoods() {
        if (pauseRefreshes) {
            allFoodsNeedsRefresh = true
        } else {
            val newData = getAllFoodsMap(database)
            foods.clear()
            foods.putAll(newData)
            foodsFlow.value = newData
            allFoodsNeedsRefresh = false
        }
    }

    @Throws(SQLException::class)
    private fun refreshFoods(ids: Collection<Long>) {
        if (pauseRefreshes) {
            foodRefreshQueue.addAll(ids)
        } else {
            
            val newData = getFoodsById(database, ids)
            foods.putAll(newData)
            for (missingId in newData.missingIdsFrom(ids)) {
                // if any requested IDs did not return a food, remove them from the cache
                foods.remove(missingId)
            }
            foodsFlow.value = foods.toMap() // copy for distinct

            refreshMealsContainingFoods(ids)
        }
    }

    @Throws(SQLException::class)
    private fun refreshMealsContainingFoods(ids: Collection<Long>) {
        val mealIds = getMealIdsForFoodIds(ids)
        // only refresh meals that are actually loaded
        val idsToRefresh = mealIds.intersect(meals.keys)
        refreshMeals(idsToRefresh)
    }

    @Throws(SQLException::class)
    private fun refreshMeals(ids: Collection<Long>) {
        if (pauseRefreshes) {
            mealRefreshQueue.addAll(ids)
        } else {
            val newData = getMealsById(database, ids)
            meals.putAll(newData)
            for (missingId in newData.missingIdsFrom(ids)) {
                // if any requested IDs did not return a meal, remove them from the cache
                meals.remove(missingId)
            }

            mealsFlow.value = meals.toMap() // copy for distinct
        }
    }

    @Throws(SQLException::class)
    override fun <M : MacrosEntity<M>> deleteObjects(objects: Collection<M>): Int {
        val numDeleted = deleteObjects(database, objects)
        for (obj in objects) {
            afterDbEdit(obj)
        }
        return numDeleted
    }

    override fun forgetFood(f: Food) {
        forgetFood(database, f)
        afterDbEdit(f)
    }

    @Throws(SQLException::class)
    override fun <M : MacrosEntity<M>> saveObjects(objects: Collection<M>, source: ObjectSource): Int {
        val numSaved = saveObjects(database, objects, source)
        // TODO copied from WriteQueries
        when (source) {
            ObjectSource.IMPORT, ObjectSource.USER_NEW -> { objects.forEach { afterDbInsert(it) } }
            ObjectSource.DB_EDIT -> { objects.forEach { afterDbEdit(it) } }
            else -> {}
        }
        return numSaved
    }

    override fun saveNutrientsToFood(food: Food, nutrients: List<FoodNutrientValue>) {
        super.saveNutrientsToFood(food, nutrients)
        afterDbEdit(food)
    }

    private fun <M : MacrosEntity<M>> afterDbInsert(obj: M) {
        when (obj) {
            is Food ->  { allFoodsNeedsRefresh = true }
            is Meal ->  { /* TODO refresh day */ }

            is FoodNutrientValue -> afterCacheEdit(obj.foodId, FoodTable)
            is FoodPortion -> afterCacheEdit(obj.mealId, MealTable)
            is Serving -> afterCacheEdit(obj.foodId, FoodTable)
        }
    }
    private fun <M : MacrosEntity<M>> afterDbEdit(obj: M) {
        when (obj) {
            is Food -> afterCacheEdit(obj.id, FoodTable)
            is Meal -> afterCacheEdit(obj.id, MealTable)

            is FoodNutrientValue -> afterCacheEdit(obj.foodId, FoodTable)
            is FoodPortion -> afterCacheEdit(obj.mealId, MealTable)
            is Serving -> afterCacheEdit(obj.foodId, FoodTable)
        }
    }

    private fun <M : MacrosEntity<M>> afterCacheEdit(id: Long, cacheType: Table<M>) {
        val idList = listOf(id)
        when (cacheType) {
            is FoodTable -> {
                refreshFoods(idList)
            }
            is MealTable -> {
                refreshMeals(idList)
            }
        }
    }
}