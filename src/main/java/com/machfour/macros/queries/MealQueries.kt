package com.machfour.macros.queries

import com.machfour.macros.orm.schema.FoodPortionTable
import com.machfour.macros.orm.schema.MealTable
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.entities.Meal
import com.machfour.macros.queries.FoodQueries.getFoodsById
import com.machfour.macros.persistence.MacrosDatabase
import com.machfour.macros.util.DateStamp
import java.sql.SQLException

object MealQueries {

    @Throws(SQLException::class)
    fun getMealsForDay(ds: MacrosDatabase, day: DateStamp): Map<Long, Meal> {
        val mealIds = getMealIdsForDay(ds, day)
        return getMealsById(ds, mealIds)
    }

    // assumes unique meal name per day
    @Throws(SQLException::class)
    fun getMealForDayWithName(ds: MacrosDatabase, day: DateStamp, name: String): Meal? {
        val mealIds = getMealIdsForDay(ds, day)
        return getMealsById(ds, mealIds).filter { it.value.name == name }.values.firstOrNull()
    }

    @Throws(SQLException::class)
    fun getMealIdsForDay(ds: MacrosDatabase, day: DateStamp): List<Long> {
        val ids = CoreQueries.selectSingleColumn(ds, MealTable.instance, MealTable.ID) {
            where(MealTable.DAY, listOf(day))
        }
        return ids.map { requireNotNull(it) { "Null meal ID encountered : $it" } }
    }

    @Throws(SQLException::class)
    fun getMealById(ds: MacrosDatabase, id: Long): Meal? {
        val resultMeals = getMealsById(ds, listOf(id))
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
    //        val updatedFp = QueryHelpers.getRawObjectsByIds(ds, FoodPortion.table, listOf(fp.id))

    //        assert(updatedFp.size == 1) { "more than 1 new food portion returned" }
    //        processRawFoodPortions(ds, newMeal, updatedFp, mapOf(fp.foodId to fp.food))

    //        fp.removeFromMeal()
    //    }
    //}

    @Throws(SQLException::class)
    private fun getRawFoodPortionsForMeal(ds: MacrosDatabase, meal: Meal): Map<Long, FoodPortion> {
        val fpIds = CoreQueries.selectNonNullColumn(ds, FoodPortion.table, FoodPortionTable.ID) {
            where(FoodPortionTable.MEAL_ID, listOf(meal.id))
            distinct()
        }
        return if (fpIds.isNotEmpty()) {
            RawEntityQueries.getRawObjectsByIds(ds, FoodPortion.table, fpIds)
        } else {
            emptyMap()
        }
    }

    @Throws(SQLException::class)
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
    @Throws(SQLException::class)
    fun getMealsById(ds: MacrosDatabase, mealIds: Collection<Long>): Map<Long, Meal> {
        if (mealIds.isEmpty()) {
            return emptyMap()
        }
        // Makes meal objects, filtering by the list of IDs. If mealIds is empty,
        // all meals will be returned.
        val meals = RawEntityQueries.getRawObjectsByKeys(ds, Meal.table, MealTable.ID, mealIds)
        val foodIds = getFoodIdsForMeals(ds, mealIds)
        if (foodIds.isNotEmpty()) {
            val foodMap = getFoodsById(ds, foodIds)
            for (meal in meals.values) {
                val foodPortionMap = getRawFoodPortionsForMeal(ds, meal)
                processRawFoodPortions(meal, foodPortionMap, foodMap)
            }
        }
        return meals
    }

    @Throws(SQLException::class)
    private fun getFoodIdsForMeals(ds: MacrosDatabase, mealIds: Collection<Long>): List<Long> {
        val ids = CoreQueries.selectSingleColumn(ds, FoodPortion.table, FoodPortionTable.FOOD_ID) {
            where(FoodPortionTable.MEAL_ID, mealIds)
            distinct()
        }
        // ensure no null IDs
        return ids.map { requireNotNull(it) { "Error: ID from database was null" }  }
    }

    @Throws(SQLException::class)
    fun getMealIdsForFoodIds(ds: MacrosDatabase, foodIds: Collection<Long>): List<Long> {
        return CoreQueries.selectNonNullColumn(ds, FoodPortion.table, FoodPortionTable.MEAL_ID) {
            where(FoodPortionTable.FOOD_ID, foodIds)
            distinct()
        }
    }

    @Throws(SQLException::class)
    fun getDaysForMealIds(ds: MacrosDatabase, mealIds: Collection<Long>): List<DateStamp> {
        return CoreQueries.selectNonNullColumn(ds, Meal.table, MealTable.DAY) {
            where(MealTable.ID, mealIds)
        }
    }
}