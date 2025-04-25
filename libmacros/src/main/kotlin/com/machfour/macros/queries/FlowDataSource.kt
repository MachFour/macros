package com.machfour.macros.queries

import com.machfour.datestamp.DateStamp
import com.machfour.macros.core.EntityId
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.*
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.Table
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

// Caches foods and meals from queries to upstream static data source
// NOTE: the cached meal objects do NOT reference the cached food objects,
// however changes to foods are tracked and trigger reloads of the meals.
class FlowDataSource(
    private val database: SqlDatabase,
): StaticDataSource(database) {

    override fun reset() {
        foods.clear()
        meals.clear()
        foodsFlow.value = emptyMap()
        mealsFlow.value = emptyMap()
        allFoodsNeedsRefresh = true

        // TODO should clear refresh queue?
    }


    var allFoodsNeedsRefresh: Boolean = true

    private val foodRefreshQueue = HashSet<EntityId>()
    private val mealRefreshQueue = HashSet<EntityId>()
    private var pauseRefreshes = false

    private val foods: MutableMap<EntityId, Food> = LinkedHashMap(100)
    private val meals: MutableMap<EntityId, Meal> = LinkedHashMap(100)
    private val mealIdsForDays: MutableMap<DateStamp, Set<EntityId>> = LinkedHashMap(10)

    private val foodsFlow: MutableStateFlow<Map<EntityId, Food>> = MutableStateFlow(emptyMap())
    private val mealsFlow: MutableStateFlow<Map<EntityId, Meal>> = MutableStateFlow(emptyMap())
    private val mealIdsForDaysFlow: MutableStateFlow<Map<DateStamp, Set<EntityId>>> = MutableStateFlow(emptyMap())

    private fun <T> Map<EntityId, T>.idMissing(id: EntityId): Boolean {
        return !containsKey(id) && id != MacrosEntity.NO_ID
    }

    private fun <T> Map<EntityId, T>.missingIdsFrom(ids: Collection<EntityId>): List<EntityId> {
        return ids.filter { idMissing(it) }
    }

    @Throws(SqlException::class)
    override fun getFood(id: EntityId): Flow<Food?> {
        if (foods.idMissing(id)) {
            refreshFoods(listOf(id))
        }
        return foodsFlow.map { it[id] }
    }


    @Throws(SqlException::class)
    // order always preserved
    override fun getFoods(ids: Collection<EntityId>, preserveOrder: Boolean): Flow<Map<EntityId, Food>> {
        val missingIds = foods.missingIdsFrom(ids)
        refreshFoods(missingIds)

        return foodsFlow.map { foods -> ids.associateNotNullWith { foods[it] } }
    }

    @Throws(SqlException::class)
    override fun getAllFoods(): Flow<Map<EntityId, Food>> {
        if (allFoodsNeedsRefresh) {
            refreshAllFoods()
        }
        return foodsFlow
    }

    @Throws(SqlException::class)
    override fun getMeal(id: EntityId): Flow<Meal?> {
        if (meals.idMissing(id)) {
            refreshMeals(listOf(id))
        }
        return mealsFlow.map { it[id] }
    }

    @Throws(SqlException::class)
    override fun getMealsForDay(day: DateStamp): Flow<Map<EntityId, Meal>> {
        refreshDay(day)

        return combine(mealsFlow, mealIdsForDaysFlow) { meals, mealIdsForDay ->
            // key should exist in map refreshDay() but just in case
            val ids = mealIdsForDay[day] ?: return@combine emptyMap()
            meals.filterKeys { ids.contains(it) }
        }
    }

    @Throws(SqlException::class)
    private fun refreshDay(day: DateStamp) {
        val mealIds = getMealIdsForDay(day)
        refreshMeals(meals.missingIdsFrom(mealIds))
        mealIdsForDays[day] = mealIds.toSet()
        mealIdsForDaysFlow.value = mealIdsForDays.toMap()
    }

    @Throws(SqlException::class)
    override fun getMeals(ids: Collection<EntityId>): Flow<Map<EntityId, Meal>> {
        refreshMeals(meals.missingIdsFrom(ids))
        val idSet = ids.toHashSet()

        return mealsFlow.map { meals -> meals.filterKeys { idSet.contains(it) } }
    }

    @Throws(SqlException::class)
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

    // Refreshes the given foods in the cache, as well as meals containing those foods
    @Throws(SqlException::class)
    private fun refreshFoods(ids: Collection<EntityId>) {
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

    private fun refreshFood(id: EntityId) {
        refreshFoods(listOf(id))
    }

    private fun refreshMeal(id: EntityId) {
        refreshMeals(listOf(id))
    }

    @Throws(SqlException::class)
    private fun refreshMealsContainingFoods(ids: Collection<EntityId>) {
        val mealIds = getMealIdsForFoodIds(ids)
        // only refresh meals that are actually loaded
        val idsToRefresh = mealIds.intersect(meals.keys)
        refreshMeals(idsToRefresh)
    }

    @Throws(SqlException::class)
    private fun refreshMeals(ids: Collection<EntityId>) {
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

    @Throws(SqlException::class)
    override fun <I: MacrosEntity, M: I> deleteObjects(table: Table<I, M>, objects: Collection<I>): Int {
        val numDeleted = deleteObjects(database, table, objects)
        for (obj in objects) {
            afterDbEdit(obj)
        }
        return numDeleted
    }

    override fun forgetFood(f: Food) {
        forgetFood(database, f)
        afterDbEdit(f)
    }

    @Throws(SqlException::class)
    override fun <I: MacrosEntity, M: I> saveObjects(table: Table<I, M>, objects: Collection<I>, source: ObjectSource): Int {
        val numSaved = saveObjects(database, table, objects, source)
        // TODO copied from WriteQueries
        // problem: don't know id after saving here (it was NO_ID)
        when (source) {
            ObjectSource.IMPORT, ObjectSource.USER_NEW -> { objects.forEach { afterDbInsert(it) } }
            ObjectSource.DB_EDIT -> { objects.forEach { afterDbEdit(it) } }
            else -> {}
        }
        return numSaved
    }

    @Throws(SqlException::class)
    override fun <I: MacrosEntity, M: I> saveObjectsReturningIds(
        table: Table<I, M>,
        objects: Collection<I>,
        source: ObjectSource
    ): List<EntityId> {
        val ids = saveObjectsReturningIds(database, table, objects, source)
        // TODO copied from WriteQueries
        // problem: don't know id after saving here (it was NO_ID)
        // --> but now we do!!
        when (source) {
            ObjectSource.IMPORT, ObjectSource.USER_NEW -> { objects.forEach { afterDbInsert(it) } }
            ObjectSource.DB_EDIT -> { objects.forEach { afterDbEdit(it) } }
            else -> {}
        }
        return ids
    }

    override fun saveNutrientsToFood(foodId: EntityId, nutrients: List<FoodNutrientValue>) {
        super.saveNutrientsToFood(foodId, nutrients)
        afterNutrientsSaved(foodId)
    }

    private fun <M : MacrosEntity> afterDbInsert(obj: M) {
        // problem: don't know id after saving here (it was NO_ID)
        when (obj) {
            is Food ->  { allFoodsNeedsRefresh = true }
            is Meal ->  { refreshDay(obj.day) }

            is FoodNutrientValue -> refreshFood(obj.foodId)
            is FoodPortion -> refreshMeal(obj.mealId)
            is Serving -> refreshFood(obj.foodId)
        }
    }

    private fun cachedMealForFpId(id: EntityId): Meal? {
        return meals.values.find { it.foodPortions.any { fp -> fp.id == id } }
    }

    private fun afterNutrientsSaved(foodId: EntityId) {
        refreshFood(foodId)
    }

    private fun <M : MacrosEntity> afterDbEdit(obj: M) {
        when (obj) {
            is Food -> refreshFood(obj.id)
            is Meal -> refreshMeal(obj.id)

            is FoodNutrientValue -> refreshFood(obj.foodId)
            is FoodPortion -> {
                // if FP was moved between meals, make sure to refresh the old meal
                cachedMealForFpId(obj.id)?.let {
                    oldMeal -> refreshMeal(oldMeal.id)
                }

                refreshMeal(obj.mealId)
            }
            is Serving -> refreshFood(obj.foodId)
        }
    }
}
