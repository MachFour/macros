package com.machfour.macros.objects

import com.machfour.macros.core.*
import kotlin.math.roundToInt

class FoodPortion private constructor(data: ColumnData<FoodPortion>, objectSource: ObjectSource)
    : MacrosEntityImpl<FoodPortion>(data, objectSource) {

    companion object {
        val factory: Factory<FoodPortion> = Factory { dataMap, objectSource -> FoodPortion(dataMap, objectSource) }
        val table: Table<FoodPortion>
            get() = Schema.FoodPortionTable.instance
    }

    /* These are not set on construction, but are only settable once: "pseudo-immutable".
     * This makes it easier to create the objects from the DB.
     */
    lateinit var food: Food
        private set
    lateinit var nutritionData: NutritionData
        private set
    lateinit var meal: Meal
        private set

    val qtyUnit = QtyUnits.fromAbbreviation(data[Schema.FoodPortionTable.QUANTITY_UNIT]!!)

    // this is the only thing that may remain null after all initialisation is complete
    var serving: Serving? = null
        private set


    override val table: Table<FoodPortion>
        get() = Companion.table
    override val factory: Factory<FoodPortion>
        get() = Companion.factory

    val mealId: Long
        get() = getData(Schema.FoodPortionTable.MEAL_ID)!!

    val foodId: Long
        get() = getData(Schema.FoodPortionTable.FOOD_ID)!!

    val servingId: Long?
        get() = getData(Schema.FoodPortionTable.SERVING_ID)

    val quantity: Double
        get() = getData(Schema.FoodPortionTable.QUANTITY)!!

    val notes: String?
        get() = getData(Schema.FoodPortionTable.NOTES)

    fun prettyFormat(withNotes: Boolean): String {
        val sb = StringBuilder()
        sb.append(food.mediumName)
        sb.append(", ")
        sb.append(String.format("%.1f", getData(Schema.FoodPortionTable.QUANTITY)))
        sb.append(qtyUnit.abbr)
        if (serving != null) {
            sb.append(" (").append(servingCountString()).append(" ").append(serving!!.name).append(")")
        }
        if (withNotes) {
            val notes = notes
            if (notes != null && notes.isNotEmpty()) {
                sb.append(" [").append(notes).append("]")
            }
        }
        return sb.toString()
    }

    // we already use polymorphism to check the data is equal for subclasses of MacrosEntity;
    // the only thing that it misses out is checking that o is actually an instance of the subclass.
    override fun equals(other: Any?): Boolean {
        return other is FoodPortion && super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    fun initMeal(m: Meal) {
        assert(foreignKeyMatches(this, Schema.FoodPortionTable.MEAL_ID, m))
        meal = m
    }

    fun initFood(f: Food) {
        assert(foreignKeyMatches(this, Schema.FoodPortionTable.FOOD_ID, f))
        food = f
        nutritionData = NutritionCalculations.rescale(f.getNutritionData(), quantity, qtyUnit)
    }

    // for use during construction
    fun initServing(s: Serving) {
        assert(serving == null && foreignKeyMatches(this, Schema.FoodPortionTable.SERVING_ID, s))
        assert(foodId == s.foodId)
        serving = s
    }

    // only do this when moving Fp from one meal to another
    fun removeFromMeal() {
        meal.removeFoodPortion(this)

        // XXX can't do this because of lateinit
        //this.meal = null
    }

    // returns a string containing the serving count. If the serving count is close to an integer,
    // it is formatted as an integer.
    private fun servingCountString(): String {
        // test if can round
        val intVersion = servingCount().roundToInt()
        return if (intVersion - servingCount() < 0.001) {
            intVersion.toString()
        } else {
            servingCount().toString()
        }
    }

    private fun servingCount(): Double {
        return serving?. let { quantity/ it.quantity } ?: 0.0
    }

}

