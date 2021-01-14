package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.core.schema.FoodNutrientValueTable
import com.machfour.macros.objects.helpers.Factories
import com.machfour.macros.objects.inbuilt.Nutrients.QUANTITY


class FoodNutrientValue internal constructor(
    data: ColumnData<FoodNutrientValue>,
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
        val factory : Factory<FoodNutrientValue>
            get() = Factories.foodNutrientValue

        val table: Table<FoodNutrientValue>
            get() = FoodNutrientValueTable.instance

        // makes an object without ID or food
        fun makeComputedValue(value: Double, nutrient: Nutrient, unit: Unit) : FoodNutrientValue {
            val data = ColumnData(table).apply {
                //put(NutrientValueTable.ID, id ?: MacrosEntity.NO_ID)
                //put(NutrientValueTable.FOOD_ID, food?.id ?: MacrosEntity.NO_ID)
                put(FoodNutrientValueTable.ID, MacrosEntity.NO_ID)
                put(FoodNutrientValueTable.VALUE, value)
                put(FoodNutrientValueTable.NUTRIENT_ID, nutrient.id)
                put(FoodNutrientValueTable.UNIT_ID, unit.id)
            }
            return factory.construct(data, ObjectSource.COMPUTED)
        }

        // Converts this value into the given unit, if possible.
        // Density is required to convert quantities between mass and volume
        // An exception is thrown if the conversion is not possible
        private fun convertValue(nv: FoodNutrientValue, newUnit: Unit, density: Double? = null) : Double {
            if (nv.unit == newUnit) {
                return nv.value
            }

            require(nv.nutrient.isConvertibleTo(newUnit)) { "Cannot convert $nv.nutrient to $newUnit (incompatible types)" }

            var conversionRatio = nv.unit.metricEquivalent / newUnit.metricEquivalent

            if (nv.nutrient == QUANTITY && nv.unit.type != newUnit.type) {
                require(density != null) { "Density required for quantity conversions between mass and volume units" }

                if (!nv.unit.isVolumeMeasurement && newUnit.isVolumeMeasurement) {
                    conversionRatio /= density
                } else if (nv.unit.isVolumeMeasurement && !newUnit.isVolumeMeasurement) { // liquid units to solid units
                    conversionRatio *= density
                } else {
                    assert(false) { "Somehow have no volume type units?" }
                }
            }

            return nv.value * conversionRatio

        }
    }

    override val factory: Factory<FoodNutrientValue>
        get() = Companion.factory

    override val table: Table<FoodNutrientValue>
        get() = Companion.table


    val foodId: Long
        get() = getData(FoodNutrientValueTable.FOOD_ID)!!

    fun rescale(ratio: Double): FoodNutrientValue {
        return makeComputedValue(value * ratio, nutrient, unit)
    }

    // if the food associated with this NutrientValue has a density, it will be used instead of the given one
    fun convert(newUnit: Unit, densityIfNoFood: Double? = null) : FoodNutrientValue {
        return makeComputedValue(convertValueTo(newUnit, densityIfNoFood), nutrient, newUnit)
    }

    // An exception is thrown if the conversion is not possible
    fun convertValueTo(newUnit: Unit, densityIfNoFood: Double? = null) : Double {
        val density = food?.density ?: densityIfNoFood
        return convertValue(this, newUnit, density)
    }

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


}
