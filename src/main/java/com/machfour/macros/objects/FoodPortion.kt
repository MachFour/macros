package com.machfour.macros.objects

import com.machfour.macros.core.*
import kotlin.math.roundToInt

class FoodPortion internal constructor(data: ColumnData<FoodQuantity>, objectSource: ObjectSource)
    : FoodQuantity(data, objectSource) {

    init {
        assert (getData(Schema.FoodQuantityTable.MEAL_ID) != null) { "Meal ID cannot be null for FoodPortion" }
    }

    lateinit var meal: Meal
        private set

    val mealId: Long
        get() = getData(Schema.FoodQuantityTable.MEAL_ID)!!

    fun initMeal(m: Meal) {
        assert(foreignKeyMatches(this, Schema.FoodQuantityTable.MEAL_ID, m))
        meal = m
    }

    // only do this when moving Fp from one meal to another
    fun removeFromMeal() {
        meal.removeFoodPortion(this)

        // XXX can't do this because of lateinit
        //this.meal = null
    }


    // we already use polymorphism to check the data is equal for subclasses of MacrosEntity;
    // the only thing that it misses out is checking that o is actually an instance of the subclass.
    override fun equals(other: Any?): Boolean {
        return other is FoodPortion && super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}

