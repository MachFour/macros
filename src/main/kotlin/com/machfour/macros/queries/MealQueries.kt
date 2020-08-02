package com.machfour.macros.queries

import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.Schema
import com.machfour.macros.objects.FoodPortion
import com.machfour.macros.objects.Meal
import com.machfour.macros.queries.FoodQueries.getFoodsById
import com.machfour.macros.storage.MacrosDataSource
import com.machfour.macros.util.DateStamp
import com.machfour.macros.util.DateStamp.Companion.currentDate
import java.sql.SQLException

object MealQueries {
    @Throws(SQLException::class)
    fun saveFoodPortions(ds: MacrosDataSource, m: Meal) {
        for (fp in m.getFoodPortions()) {
            if (fp.objectSource != ObjectSource.DATABASE) {
                Queries.saveObject(ds, fp)
            }
        }
    }

    @Throws(SQLException::class)
    fun getOrCreateMeal(ds: MacrosDataSource, day: DateStamp, name: String): Meal {
        var mealsForDay = getMealsForDay(ds, day)
        var nameMatch = findMealWithName(mealsForDay, name)
        return if (nameMatch != null) {
            nameMatch
        } else {
            val newMealData = ColumnData(Meal.table())
            newMealData.put(Schema.MealTable.DAY, day)
            newMealData.put(Schema.MealTable.NAME, name)
            val newMeal = Meal.factory().construct(newMealData, ObjectSource.USER_NEW)
            Queries.saveObject(ds, newMeal)
            // get it back again, so that it has an ID and stuff
            mealsForDay = getMealsForDay(ds, day)
            nameMatch = findMealWithName(mealsForDay, name)
            assert(nameMatch != null) { "didn't find saved meal in meals for its day" }
            nameMatch!!
        }
    }

    // finds whether there is a 'current meal', and returns it if so.
    // defined as the most recently modified meal created for the current date
    // if no meals exist for the current date, returns null
    @Throws(SQLException::class)
    fun getCurrentMeal(ds: MacrosDataSource): Meal? {
        return getMealsForDay(ds, currentDate()).values.maxByOrNull { it.startTime }
    }

    @Throws(SQLException::class)
    fun getMealsForDay(ds: MacrosDataSource, day: DateStamp): Map<Long, Meal> {
        val mealIds = getMealIdsForDay(ds, day)
        return getMealsById(ds, mealIds)
    }

    @Throws(SQLException::class)
    fun getMealIdsForDay(ds: MacrosDataSource, day: DateStamp): List<Long> {
        val ids = Queries.selectColumn(ds, Schema.MealTable.instance, Schema.MealTable.ID, Schema.MealTable.DAY, listOf(day))
        return ids.map { requireNotNull(it) { "Null meal ID encountered : $it" } }

        // TODO: need "DATE(" + Meal.Column.DAY + ") = DATE ( ? )"; ???
    }

    /* The get<Object>By(Id|Key) functions construct objects for all necessary entities that match the query,
     * as well as all other entities referenced by them.
     * For example, getMealsForDay constructs all of the MealTable objects for one particular day,
     * along with their FoodPortions, their Foods, and all of the Servings of those Foods.
     * It's probably worth caching the results of these!
     */
    @Throws(SQLException::class)
    fun getMealById(ds: MacrosDataSource, id: Long): Meal? {
        val resultMeals = getMealsById(ds, listOf(id))
        return resultMeals.getOrDefault(id, null)
    }

    @Throws(SQLException::class)
    fun getMealsById(ds: MacrosDataSource, mealIds: List<Long>): Map<Long, Meal> {
        if (mealIds.isEmpty()) {
            return emptyMap()
        }
        val foodIds = getFoodIdsForMeals(ds, mealIds)
        val meals = QueryHelpers.getRawMealsById(ds, mealIds)
        // this check stops an unnecessary lookup of all foods, which happens if no IDs are passed
        // into getFoodsById;
        if (foodIds.isNotEmpty()) {
            val foodMap = getFoodsById(ds, foodIds)
            for (meal in meals.values) {
                QueryHelpers.applyFoodPortionsToRawMeal(ds, meal, foodMap)
            }
        }
        return meals
    }

    @Throws(SQLException::class)
    fun getFoodIdsForMeals(ds: MacrosDataSource, mealIds: List<Long>): List<Long> {
        val ids = ds.selectColumn(FoodPortion.table(), Schema.FoodPortionTable.FOOD_ID, Schema.FoodPortionTable.MEAL_ID, mealIds, true)
        // ensure no null IDs
        return ids.map { requireNotNull(it) { "Error: ID from database was null" }  }
    }

    fun findMealWithName(mealMap: Map<Long, Meal>, name: String): Meal? {
        return mealMap.values.firstOrNull { it.name == name }
    }
}