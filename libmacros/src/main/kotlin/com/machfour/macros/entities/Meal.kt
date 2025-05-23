
package com.machfour.macros.entities

import com.machfour.datestamp.DateStamp
import com.machfour.macros.sql.entities.Factory
import com.machfour.macros.sql.entities.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.sql.entities.Factories
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.schema.FoodPortionTable
import com.machfour.macros.schema.MealTable
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.rowdata.RowData

// don't need hashcode override since equals implies super.equals true, so hashcode will match
@Suppress("EqualsOrHashCode")
class Meal internal constructor(data: RowData<Meal>, source: ObjectSource) : MacrosEntityImpl<Meal>(data, source) {
    companion object {
        fun sumNutrientData(meals: Iterable<Meal>): FoodNutrientData {
            return FoodNutrientData.sum(meals.map { it.nutrientTotal() })
        }

        // factory before table
        val factory: Factory<Meal, Meal>
            get() = Factories.meal
    }

    private val foodPortionsMutable = ArrayList<FoodPortion>()

    val foodPortions: List<FoodPortion>
        get() = foodPortionsMutable

    override fun getTable(): Table<*, Meal> {
        return MealTable
    }

    fun nutrientTotal(): FoodNutrientData {
        val allNutrientData = foodPortions.map { it.nutrientData }
        return FoodNutrientData.sum(allNutrientData)
    }

    val name: String
        get() = data[MealTable.NAME]!!

    /*
     * 'Day' is the day for which the nutrition data should be counted.
     * i.e. the total nutrition value for day X is the sum of nutrition data
     * for all meals having that Day field
     */
    val day: DateStamp
        get() = data[MealTable.DAY]!!

    /*
     * Start time is the time that the meal was actually consumed. Note that
     * it's a full timestamp - because for various reasons (timezones, night shifts, eating past midnight)
     * we allow the day that the meal is labelled with in the Day column to be different from
     * the calendar date on which it was actually eaten.
     */
    // returns time in Unix time, aka seconds since Jan 1 1970
    val startTime: Long
        get() = data[MealTable.START_TIME]!!

    // in seconds, how long the meal lasted.
    val durationSeconds: Int
        get() = data[MealTable.DURATION]!!

    val durationMinutes: Int
        get() = durationSeconds / 60

    val notes: String?
        get() = data[MealTable.NOTES]

    override fun equals(other: Any?): Boolean {
        return other is Meal
                && super.equals(other)
                && foodPortions == other.foodPortions
    }

    fun addFoodPortion(fp: FoodPortion) {
        // can't assert !foodPortions.contains(fp) since user-created food portions can look identical
        require(foreignKeyMatches(fp, FoodPortionTable.MEAL_ID, this))
        foodPortionsMutable.add(fp)
    }

    override fun toString(): String = "$name, $day"
}
