package com.machfour.macros.queries

import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.MacrosBuilder
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.schema.FoodPortionTable
import com.machfour.macros.core.schema.MealTable
import com.machfour.macros.objects.FoodPortion
import com.machfour.macros.objects.Meal
import com.machfour.macros.queries.FoodQueries.getFoodsById
import com.machfour.macros.persistence.MacrosDataSource
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

    // only used by mealquery - this isn't a great function
    @Throws(SQLException::class)
    internal fun getOrCreateMeal(ds: MacrosDataSource, day: DateStamp, name: String): Meal {
        // if it already exists return it
        val matchingMeal = getMealsForDayWithName(ds, day, name).firstOrNull()
        if (matchingMeal != null) {
            return matchingMeal
        }

        // else create a new meal, save and return it
        val newMeal = ColumnData(Meal.table).run {
            put(MealTable.DAY, day)
            put(MealTable.NAME, name)
            Meal.factory.construct(this, ObjectSource.USER_NEW)
        }
        
        Queries.saveObject(ds, newMeal)
        // get it back again, so that it has an ID and stuff
        val newlySavedMeal = getMealsForDayWithName(ds, day, name).firstOrNull()
        check(newlySavedMeal != null) { "Couldn't find newly saved meal in day $day" }
        return newlySavedMeal
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
    fun getMealsForDayWithName(ds: MacrosDataSource, day: DateStamp, name: String): Collection<Meal> {
        val mealIds = getMealIdsForDay(ds, day)
        return getMealsById(ds, mealIds).filter { it.value.name == name }.values
    }


    @Throws(SQLException::class)
    fun getMealIdsForDay(ds: MacrosDataSource, day: DateStamp): List<Long> {
        val ids = Queries.selectSingleColumn(ds, MealTable.instance, MealTable.ID) {
            where(MealTable.DAY, listOf(day))
        }
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

    // Creates a new foodportion from the old one, with an updated meal ID.
    // Old meal will have the old foodPortion removed and provided no other references to it are kept,
    // it will be garbage collected.
    // newMeal will have the newly constructed object (with the correct ID) added.
    // if the food portion already belongs to newMeal, nothing is done
    @Throws(SQLException::class)
    fun moveFoodPortion(ds: MacrosDataSource, fp: FoodPortion, newMeal: Meal) {
        if (fp.meal != newMeal) {
            val editedFp = MacrosBuilder(fp).run {
                setField(FoodPortionTable.MEAL_ID, newMeal.id)
                build()
            }
            Queries.saveObject(ds, editedFp)
            val updatedFp = QueryHelpers.getRawObjectsByIds(ds, FoodPortion.table, listOf(fp.id))

            assert(updatedFp.size == 1) { "more than 1 new food portion returned" }
            QueryHelpers.processRawFoodPortions(ds, newMeal, updatedFp, mapOf(fp.foodId to fp.food))

            fp.removeFromMeal()
        }
    }

    @Throws(SQLException::class)
    fun getMealsById(ds: MacrosDataSource, mealIds: Collection<Long>): Map<Long, Meal> {
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
    fun getFoodIdsForMeals(ds: MacrosDataSource, mealIds: Collection<Long>): List<Long> {
        val ids = Queries.selectSingleColumn(ds, FoodPortion.table, FoodPortionTable.FOOD_ID) {
            where(FoodPortionTable.MEAL_ID, mealIds)
            distinct()
        }
        // ensure no null IDs
        return ids.map { requireNotNull(it) { "Error: ID from database was null" }  }
    }

    @Throws(SQLException::class)
    fun getMealIdsForFoodIds(ds: MacrosDataSource, foodIds: List<Long>): List<Long> {
        return Queries.selectNonNullColumn(ds, FoodPortion.table, FoodPortionTable.MEAL_ID) {
            where(FoodPortionTable.FOOD_ID, foodIds)
            distinct()
        }
    }

    fun searchForName(mealMap: Map<Long, Meal>, name: String): Meal? {
        return mealMap.values.firstOrNull { it.name == name }
    }
}