package com.machfour.macros.queries

import com.machfour.datestamp.DateStamp
import com.machfour.macros.core.EntityId
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.entities.Meal
import com.machfour.macros.schema.FoodPortionTable
import com.machfour.macros.schema.MealTable
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException

@Throws(SqlException::class)
fun getMealsForDay(db: SqlDatabase, day: DateStamp): Map<EntityId, Meal> {
    val mealIds = getMealIdsForDay(db, day)
    return getMealsById(db, mealIds)
}

// assumes unique meal name per day
@Throws(SqlException::class)
fun getMealForDayWithName(db: SqlDatabase, day: DateStamp, name: String): Meal? {
    val mealIds = getMealIdsForDay(db, day)
    return getMealsById(db, mealIds).filter { it.value.name == name }.values.firstOrNull()
}

@Throws(SqlException::class)
fun getMealIdsForDay(db: SqlDatabase, day: DateStamp): List<EntityId> {
    val ids = selectSingleColumn(db, MealTable.ID) {
        where(MealTable.DAY, listOf(day))
    }
    return ids.map { requireNotNull(it) { "Null meal ID encountered : $it" } }
}

@Throws(SqlException::class)
fun getMealById(db: SqlDatabase, id: EntityId): Meal? {
    val resultMeals = getMealsById(db, listOf(id))
    return resultMeals.getOrDefault(id, null)
}

// Creates a new foodportion from the old one, with an updated meal ID.
// Old meal will have the old foodPortion removed and provided no other references to it are kept,
// it will be garbage collected.
// newMeal will have the newly constructed object (with the correct ID) added.
// if the food portion already belongs to newMeal, nothing is done
//@Throws(SqlException::class)
//fun moveFoodPortion(ds: MacrosDataSource, fp: FoodPortion, newMeal: Meal) {
//    if (fp.meal != newMeal) {
//        val editedFp = MacrosBuilder(fp).run {
//            setField(FoodPortionTable.MEAL_ID, newMeal.id)
//            build()
//        }
//        WriteQueries.saveObject(ds, editedFp)
//        val updatedFp = QueryHelpers.getRawObjectsByIds(ds, FoodPortionTable, listOf(fp.id))

//        check(updatedFp.size == 1) { "more than 1 new food portion returned" }
//        processRawFoodPortions(ds, newMeal, updatedFp, mapOf(fp.foodId to fp.food))

//        fp.removeFromMeal()
//    }
//}

@Throws(SqlException::class)
private fun getRawFoodPortionsForMeal(db: SqlDatabase, meal: Meal): Map<EntityId, FoodPortion> {
    val fpIds = selectNonNullColumn(db, FoodPortionTable.ID) {
        where(FoodPortionTable.MEAL_ID, listOf(meal.id))
        distinct()
    }
    return if (fpIds.isNotEmpty()) {
        getRawObjectsWithIds(db, FoodPortionTable, fpIds)
    } else {
        emptyMap()
    }
}

@Throws(SqlException::class)
private fun processRawFoodPortions(meal: Meal, fpMap: Map<EntityId, FoodPortion>, foodMap: Map<EntityId, Food>) {
    // sort by create time - but if adding to existing meals, they are added to the end
    val fpList = fpMap.values.toList().sortedBy { it.createTime }
    for (fp in fpList) {
        val portionFood = foodMap[fp.foodId]
        require(portionFood != null) { "foodMap did not contain food with ID ${fp.foodId}" }
        fp.initFoodAndNd(portionFood)
        fp.servingId?.let {
            val serving = portionFood.getServingById(it)
            checkNotNull(serving) { "Serving specified by FoodPortion not found in its food!" }
            fp.initServing(serving)
        }
        meal.addFoodPortion(fp)
    }
}

/* The getXXXBy(Id|Key) functions construct objects for all necessary entities that match the query,
 * as well as all other entities referenced by them.
 * For example, getMealsForDay constructs all the MealTable objects for one particular day,
 * along with their FoodPortions, their Foods, and all the Servings of those Foods.
 * It's probably worth caching the results of these!
 */
@Throws(SqlException::class)
fun getMealsById(db: SqlDatabase, mealIds: Collection<EntityId>): Map<EntityId, Meal> {
    if (mealIds.isEmpty()) {
        return emptyMap()
    }
    // Makes meal objects, filtering by the list of IDs. If mealIds is empty,
    // all meals will be returned.
    val meals = getRawObjectsWithIds(db, MealTable, mealIds)
    val foodIds = getFoodIdsForMeals(db, mealIds)
    if (foodIds.isNotEmpty()) {
        val foodMap = getFoodsById(db, foodIds)
        for (meal in meals.values) {
            val foodPortionMap = getRawFoodPortionsForMeal(db, meal)
            processRawFoodPortions(meal, foodPortionMap, foodMap)
        }
    }
    return meals
}

@Throws(SqlException::class)
private fun getFoodIdsForMeals(db: SqlDatabase, mealIds: Collection<EntityId>): List<EntityId> {
    if (mealIds.isEmpty()) {
        return emptyList()
    }
    val ids = selectSingleColumn(db, FoodPortionTable.FOOD_ID) {
        where(FoodPortionTable.MEAL_ID, mealIds)
        distinct()
    }
    // ensure no null IDs
    return ids.map { requireNotNull(it) { "Error: ID from database was null" }  }
}

@Throws(SqlException::class)
fun getMealIdsForFoodIds(db: SqlDatabase, foodIds: Collection<EntityId>): List<EntityId> {
    if (foodIds.isEmpty()) {
        return emptyList()
    }
    return selectNonNullColumn(db, FoodPortionTable.MEAL_ID) {
        where(FoodPortionTable.FOOD_ID, foodIds)
        distinct()
    }
}

@Throws(SqlException::class)
fun getMealIdsForFoodPortionIds(db: SqlDatabase, foodPortionIds: Collection<EntityId>): List<EntityId> {
    if (foodPortionIds.isEmpty()) {
        return emptyList()
    }
    return selectNonNullColumn(db, FoodPortionTable.MEAL_ID) {
        where(FoodPortionTable.ID, foodPortionIds)
        distinct()
    }
}

@Throws(SqlException::class)
fun getDaysForMealIds(db: SqlDatabase, mealIds: Collection<EntityId>): List<DateStamp> {
    if (mealIds.isEmpty()) {
        return emptyList()
    }
    return selectNonNullColumn(db, MealTable.DAY) {
        where(MealTable.ID, mealIds)
    }
}