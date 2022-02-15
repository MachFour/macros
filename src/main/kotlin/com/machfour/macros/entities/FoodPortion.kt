package com.machfour.macros.entities

import com.machfour.macros.core.Factory
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.schema.FoodPortionTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table

// don't need hashcode override since equals implies super.equals true, so hashcode will match
@Suppress("EqualsOrHashCode")
class FoodPortion internal constructor(
    data: RowData<FoodPortion>, objectSource: ObjectSource
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
            get() = FoodPortionTable
    }

    override val table: Table<FoodPortion>
        get() = Companion.table
    override val factory: Factory<FoodPortion>
        get() = Companion.factory


    init {
        // TODO is this check actually needed? MacrosEntity might take care of it already.
        assert(this.data[FoodPortionTable.MEAL_ID] != null) { "Meal ID cannot be null for FoodPortion" }
    }

    lateinit var meal: Meal
        private set

    val mealId: Long
        get() = data[FoodPortionTable.MEAL_ID]!!

    fun initMeal(m: Meal) {
        assert(foreignKeyMatches(this, FoodPortionTable.MEAL_ID, m))
        meal = m
    }

    override fun equals(other: Any?): Boolean {
        return other is FoodPortion
                && super.equals(other)
                && food == other.food
    }

}

