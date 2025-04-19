package com.machfour.macros.entities

import com.machfour.macros.core.EntityId
import com.machfour.macros.sql.entities.Factory
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.sql.entities.Factories
import com.machfour.macros.schema.FoodPortionTable
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.rowdata.RowData

// don't need hashcode override since equals implies super.equals true, so hashcode will match
@Suppress("EqualsOrHashCode")
class FoodPortion internal constructor(
    data: RowData<FoodPortion>, objectSource: ObjectSource
) : FoodQuantityImpl<FoodPortion, FoodNutrientValue> (
    data, objectSource,
    FoodPortionTable.FOOD_ID,
    FoodPortionTable.SERVING_ID,
    FoodPortionTable.QUANTITY,
    FoodPortionTable.QUANTITY_UNIT,
    FoodPortionTable.NOTES,
    FoodPortionTable.NUTRIENT_MAX_VERSION,
), IFoodPortion<FoodNutrientValue> {

    companion object {
        val factory: Factory<FoodPortion>
            get() = Factories.foodPortion

    }

    override val table: Table<FoodPortion>
        get() = FoodPortionTable
    override val factory: Factory<FoodPortion>
        get() = Companion.factory


    override val mealId: EntityId
        get() = data[FoodPortionTable.MEAL_ID]!!

    override fun equals(other: Any?): Boolean {
        return other is FoodPortion
                && super.equals(other)
                && food == other.food
    }

}

