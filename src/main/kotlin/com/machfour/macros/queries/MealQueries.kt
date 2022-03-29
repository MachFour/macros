package com.machfour.macros.queries

import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.entities.Meal
import com.machfour.macros.schema.FoodPortionTable
import com.machfour.macros.schema.MealTable
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import com.machfour.macros.util.DateStamp

@Throws(SqlException::class)
internal fun getMealsForDay(db: SqlDatabase, day: DateStamp): Map<Long, Meal> {
    val mealIds = getMealIdsForDay(db, day)
    return getMealsById(db, mealIds)
}

// assumes unique meal name per day
@Throws(SqlException::class)
internal fun getMealForDayWithName(db: SqlDatabase, day: DateStamp, name: String): Meal? {
    val mealIds = getMealIdsForDay(db, day)
    return getMealsById(db, mealIds).filter { it.value.name == name }.values.firstOrNull()
}

@Throws(SqlException::class)
internal fun getMealIdsForDay(db: SqlDatabase, day: DateStamp): List<Long> {
    val ids = selectSingleColumn(db, MealTable.ID) {
        where(MealTable.DAY, listOf(day))
    }
    return ids.map { requireNotNull(it) { "Null meal ID encountered : $it" } }
}

@Throws(SqlException::class)
internal fun getMealById(db: SqlDatabase, id: Long): Meal? {
    val resultMeals = getMealsById(db, listOf(id))
    return resultMeals.getOrDefault(id, null)
}

// Creates a new foodportion from the old one, with an updated meal ID.
// Old meal will have the old foodPortion removed and provided no other references to it are kept,
// it will be garbage collected.
// newMeal will have the newly constructed object (with the correct ID) added.
// if the food portion already belongs to newMeal, nothing is done
//@Throws(SQLException::class)
//fun moveFoodPortion(ds: MacrosDataSource, fp: FoodPortion, newMeal: Meal) {
//    if (fp.meal != newMeal) {
//        val editedFp = MacrosBuilder(fp).run {
//            setField(FoodPortionTable.MEAL_ID, newMeal.id)
//            build()
//        }
//        WriteQueries.saveObject(ds, editedFp)
//        val updatedFp = QueryHelpers.getRawObjectsByIds(ds, FoodPortionTable, listOf(fp.id))

//        assert(updatedFp.size == 1) { "more than 1 new food portion returned" }
//        processRawFoodPortions(ds, newMeal, updatedFp, mapOf(fp.foodId to fp.food))

//        fp.removeFromMeal()
//    }
//}

@Throws(SqlException::class)
private fun getRawFoodPortionsForMeal(db: SqlDatabase, meal: Meal): Map<Long, FoodPortion> {
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
private fun processRawFoodPortions(meal: Meal, fpMap: Map<Long, FoodPortion>, foodMap: Map<Long, Food>) {
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
        fp.initMeal(meal)
        meal.addFoodPortion(fp)
    }
}

/* The getXXXBy(Id|Key) functions construct objects for all necessary entities that match the query,
 * as well as all other entities referenced by them.
 * For example, getMealsForDay constructs all of the MealTable objects for one particular day,
 * along with their FoodPortions, their Foods, and all of the Servings of those Foods.
 * It's probably worth caching the results of these!
 */
@Throws(SqlException::class)
fun getMealsById(db: SqlDatabase, mealIds: Collection<Long>): Map<Long, Meal> {
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
private fun getFoodIdsForMeals(db: SqlDatabase, mealIds: Collection<Long>): List<Long> {
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
fun getMealIdsForFoodIds(db: SqlDatabase, foodIds: Collection<Long>): List<Long> {
    if (foodIds.isEmpty()) {
        return emptyList()
    }
    return selectNonNullColumn(db, FoodPortionTable.MEAL_ID) {
        where(FoodPortionTable.FOOD_ID, foodIds)
        distinct()
    }
}

@Throws(SqlException::class)
fun getMealIdsForFoodPortionIds(db: SqlDatabase, foodPortionIds: Collection<Long>): List<Long> {
    if (foodPortionIds.isEmpty()) {
        return emptyList()
    }
    return selectNonNullColumn(db, FoodPortionTable.MEAL_ID) {
        where(FoodPortionTable.ID, foodPortionIds)
        distinct()
    }
}

@Throws(SqlException::class)
fun getDaysForMealIds(db: SqlDatabase, mealIds: Collection<Long>): List<DateStamp> {
    if (mealIds.isEmpty()) {
        return emptyList()
    }
    return selectNonNullColumn(db, MealTable.DAY) {
        where(MealTable.ID, mealIds)
    }
}