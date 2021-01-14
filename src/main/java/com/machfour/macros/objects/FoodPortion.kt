package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.core.schema.FoodPortionTable
import com.machfour.macros.objects.helpers.Factories

class FoodPortion internal constructor(data: ColumnData<FoodPortion>, objectSource: ObjectSource
) : FoodQuantity<FoodPortion>(
    data, objectSource,
    FoodPortionTable.FOOD_ID,
    FoodPortionTable.SERVING_ID,
    FoodPortionTable.QUANTITY,
    FoodPortionTable.QUANTITY_UNIT,
    FoodPortionTable.NOTES,
    FoodPortionTable.NUTRIENT_MAX_VERSION,
) {

    companion object {
        val factory: Factory<FoodPortion>
            get() = Factories.foodPortion

        val table: Table<FoodPortion>
            get() = FoodPortionTable.instance
    }

    override val table: Table<FoodPortion>
        get() = Companion.table
    override val factory: Factory<FoodPortion>
        get() = Companion.factory


    init {
        assert (getData(FoodPortionTable.MEAL_ID) != null) { "Meal ID cannot be null for FoodPortion" }
    }

    lateinit var meal: Meal
        private set

    val mealId: Long
        get() = getData(FoodPortionTable.MEAL_ID)!!

    fun initMeal(m: Meal) {
        assert(foreignKeyMatches(this, FoodPortionTable.MEAL_ID, m))
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

