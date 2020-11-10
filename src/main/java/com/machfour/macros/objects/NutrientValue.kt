package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.core.Schema.NutrientValueTable
import com.machfour.macros.objects.inbuilt.Nutrients
import com.machfour.macros.objects.inbuilt.Nutrients.QUANTITY
import com.machfour.macros.objects.inbuilt.Units

class NutrientValue private constructor(dataMap: ColumnData<NutrientValue>, objectSource: ObjectSource)
    : MacrosEntityImpl<NutrientValue>(dataMap, objectSource) {

    companion object {
        // Factory has to be initialised first before table is referenced.
        // This is a problem only if the factory is cached as an instance variable
        val factory = Factory<NutrientValue> { dataMap, objectSource -> NutrientValue(dataMap, objectSource) }

        val table: Table<NutrientValue>
            get() = NutrientValueTable.instance

        fun makeObject(
                value: Double,
                nutrient: Nutrient,
                unit: Unit,
                id: Long? = null,
                food: Food? = null,
                objectSource: ObjectSource = ObjectSource.USER_NEW,
        ) : NutrientValue {
            val data = ColumnData(table).apply {
                put(NutrientValueTable.ID, id ?: MacrosEntity.NO_ID)
                put(NutrientValueTable.FOOD_ID, food?.id ?: MacrosEntity.NO_ID)
                put(NutrientValueTable.VALUE, value)
                put(NutrientValueTable.NUTRIENT_ID, nutrient.id)
                put(NutrientValueTable.UNIT_ID, unit.id)
            }
            return factory.construct(data, objectSource).apply {
                food?.let{ setFood(it) }
            }
        }

        fun makeComputedValue(value: Double, nutrient: Nutrient, unit: Unit) : NutrientValue {
            return makeObject(value, nutrient, unit, objectSource = ObjectSource.COMPUTED)
        }

        // Converts this value into the given unit, if possible.
        // Density is required to convert quantities between mass and volume
        // An exception is thrown if the conversion is not possible
        private fun convertValue(nv: NutrientValue, newUnit: Unit, density: Double? = null) : Double {
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

    override val factory: Factory<NutrientValue>
        get() = Companion.factory

    override val table: Table<NutrientValue>
        get() = Companion.table

    val value: Double = getData(NutrientValueTable.VALUE)!!
    val unit: Unit = Units.fromId(getData(NutrientValueTable.UNIT_ID)!!)
    val nutrient: Nutrient = Nutrients.fromId(getData(NutrientValueTable.NUTRIENT_ID)!!)

    val foodId: Long
        get() = getData(NutrientValueTable.FOOD_ID)!!
    val nutrientId: Long
        get() = getData(NutrientValueTable.NUTRIENT_ID)!!


    fun rescale(ratio: Double): NutrientValue {
        return makeComputedValue(value * ratio, nutrient, unit)
    }

    fun convert(newUnit: Unit, overrideDensity: Double? = null) : NutrientValue {
        return makeComputedValue(convertValueTo(newUnit, overrideDensity), nutrient, newUnit)
    }

    // An exception is thrown if the conversion is not possible
    fun convertValueTo(newUnit: Unit, overrideDensity: Double? = null) : Double {
        val density = overrideDensity ?: food?.density
        return convertValue(this, newUnit, density)
    }

    var food: Food? = null
        private set

    // should only be called by Food class when this object is added to it
    internal fun setFood(f: Food) {
        assert(food == null) { "Food already set" }
        assert(foreignKeyMatches(this, NutrientValueTable.FOOD_ID, f)) { "Food ID does not match" }
        food = f
    }

    override fun toString(): String {
        return "$value ${unit.abbr}"
    }


}
