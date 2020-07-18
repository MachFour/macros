package com.machfour.macros.objects

import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.Factory
import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.Schema
import com.machfour.macros.core.Table
import com.machfour.macros.util.DateStamp

import java.time.Instant

class Meal private constructor(data: ColumnData<Meal>, objectSource: ObjectSource) : MacrosEntityImpl<Meal>(data, objectSource) {
    companion object {
        fun sumNutritionData(meals: Collection<Meal>): NutritionData {
            val totalPerMeal = ArrayList<NutritionData>(meals.size)
            for (m in meals) {
                totalPerMeal.add(m.nutritionTotal)
            }
            return NutritionData.sum(totalPerMeal)
        }

        fun factory(): Factory<Meal> {
            return object : Factory<Meal> {
                override fun construct(dataMap: ColumnData<Meal>, objectSource: ObjectSource): Meal {
                    return Meal(dataMap, objectSource)
                }
            }
        }

        fun table(): Table<Meal> {
            return Schema.MealTable.instance
        }
    }

    private val foodPortions: MutableList<FoodPortion> = ArrayList()

    override val factory: Factory<Meal>
        get() = factory()
    override val table: Table<Meal>
        get() = table()

    val nutritionTotal: NutritionData
        get() {
            val nutritionComponents = ArrayList<NutritionData>(foodPortions.size)
            for (fp in foodPortions) {
                nutritionComponents.add(fp.nutritionData)
            }
            return NutritionData.sum(nutritionComponents)
        }

    val name: String
        get() = getData(Schema.MealTable.NAME)!!

    /*
     * 'Day' is the day for which the nutrition data should be counted.
     * i.e. the total nutrition value for day X is the sum of nutrition data
     * for all meals having that Day field
     */
    val day: DateStamp
        get() = getData(Schema.MealTable.DAY)!!

    /*
     * Start time is the time that the meal was actually consumed. Note that
     * it's a full timestamp - because for various reasons (timezones, night shifts, eating past midnight)
     * we allow the day that the meal is labelled with in the Day column to be different from
     * the calendar date on which it was actually eaten.
     */
    // returns time in Unix time, aka seconds since Jan 1 1970
    val startTime: Long
        get() = getData(Schema.MealTable.START_TIME)!!

    // in seconds, how long the meal lasted.
    val durationSeconds: Long
        get() = getData(Schema.MealTable.DURATION)!!

    val durationMinutes: Long
        get() = durationSeconds / 60

    val startTimeInstant: Instant
        get() = Instant.ofEpochSecond(startTime)


    override fun equals(other: Any?): Boolean {
        return other is Meal && super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    fun getFoodPortions(): List<FoodPortion> {
        return ArrayList(foodPortions)
    }

    fun addFoodPortion(fp: FoodPortion) {
        // can't assert !foodPortions.contains(fp) since user-created food portions can look identical
        assert(foreignKeyMatches(fp, Schema.FoodPortionTable.MEAL_ID, this))
        foodPortions.add(fp)
    }

}
