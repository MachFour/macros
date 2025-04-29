package com.machfour.macros.entities

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.nutrients.INutrientValue
import com.machfour.macros.schema.FoodNutrientValueTable
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.entities.Factories
import com.machfour.macros.sql.entities.Factory
import com.machfour.macros.sql.rowdata.RowData


class FoodNutrientValue internal constructor(
    data: RowData<FoodNutrientValue>,
    objectSource: ObjectSource
) : NutrientValue<FoodNutrientValue>(
    data,
    objectSource,
    FoodNutrientValueTable.NUTRIENT_ID,
    FoodNutrientValueTable.UNIT_ID,
    FoodNutrientValueTable.VALUE,
    FoodNutrientValueTable.CONSTRAINT_SPEC
) {

    companion object {
        // Factory has to be initialised first before table is referenced.
        // This is a problem only if the factory is cached as an instance variable
        val factory: Factory<INutrientValue, FoodNutrientValue>
            get() = Factories.foodNutrientValue

        // makes an object without ID or food
        fun makeComputedValue(value: Double, nutrient: INutrient, unit: Unit): FoodNutrientValue {
            return factory.construct(RowData(FoodNutrientValueTable).apply {
                //put(NutrientValueTable.ID, id ?: MacrosEntity.NO_ID)
                //put(NutrientValueTable.FOOD_ID, food?.id ?: MacrosEntity.NO_ID)
                put(FoodNutrientValueTable.ID, MacrosEntity.NO_ID)
                put(FoodNutrientValueTable.VALUE, value)
                put(FoodNutrientValueTable.NUTRIENT_ID, nutrient.id)
                put(FoodNutrientValueTable.UNIT_ID, unit.id)
            }, ObjectSource.COMPUTED)
        }
    }


    override fun getTable(): Table<*, FoodNutrientValue> {
        return FoodNutrientValueTable
    }

    val foodId: EntityId
        get() = data[FoodNutrientValueTable.FOOD_ID]!!

    val version: Int
        get() = this.data[FoodNutrientValueTable.VERSION]!!


    override fun toString(): String {
        return "$value ${unit.abbr}"
    }

    fun scale(ratio: Double): FoodNutrientValue {
        return makeComputedValue(value * ratio, nutrient, unit)
    }

}
