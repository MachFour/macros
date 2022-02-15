package com.machfour.macros.entities

import com.machfour.macros.core.Factory
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.schema.FoodNutrientValueTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table


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
        val factory: Factory<FoodNutrientValue>
            get() = Factories.foodNutrientValue

        // makes an object without ID or food
        fun makeComputedValue(value: Double, nutrient: Nutrient, unit: Unit): FoodNutrientValue {
            val data = RowData(FoodNutrientValueTable).apply {
                //put(NutrientValueTable.ID, id ?: MacrosEntity.NO_ID)
                //put(NutrientValueTable.FOOD_ID, food?.id ?: MacrosEntity.NO_ID)
                put(FoodNutrientValueTable.ID, MacrosEntity.NO_ID)
                put(FoodNutrientValueTable.VALUE, value)
                put(FoodNutrientValueTable.NUTRIENT_ID, nutrient.id)
                put(FoodNutrientValueTable.UNIT_ID, unit.id)
            }
            return factory.construct(data, ObjectSource.COMPUTED)
        }
    }


    override val factory: Factory<FoodNutrientValue>
        get() = Companion.factory

    override val table: Table<FoodNutrientValue>
        get() = FoodNutrientValueTable


    val foodId: Long
        get() = data[FoodNutrientValueTable.FOOD_ID]!!

    var food: Food? = null
        private set

    // should only be called by Food class when this object is added to it
    internal fun setFood(f: Food) {
        assert(food == null) { "Food already set" }
        assert(foreignKeyMatches(this, FoodNutrientValueTable.FOOD_ID, f)) { "Food ID does not match" }
        food = f
    }

    override fun toString(): String {
        return "$value ${unit.abbr}"
    }

    fun rescale(ratio: Double): FoodNutrientValue {
        return makeComputedValue(value * ratio, nutrient, unit)
    }

    // if the food associated with this NutrientValue has a density, it will be used instead of the given one
    fun convert(newUnit: Unit, densityIfNoFood: Double? = null): FoodNutrientValue {
        return makeComputedValue(convertValueTo(newUnit, densityIfNoFood), nutrient, newUnit)
    }

    // Food's density will be used if present. If food does not have a density, it can be specified here.
    override fun convertValueTo(newUnit: Unit, density: Double?): Double {
        return super.convertValueTo(newUnit, food?.density ?: density)
    }


}
