package com.machfour.macros.entities

import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.nutrients.QUANTITY
import com.machfour.macros.nutrients.nutrientWithId
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.RowData
import com.machfour.macros.units.unitWithId

abstract class NutrientValue<M : NutrientValue<M>> protected constructor(
    data: RowData<M>,
    objectSource: ObjectSource,

    private val nutrientIdCol: Column.Fk<M, Long, Nutrient>,
    /* private val */
    unitIdCol: Column.Fk<M, Long, Unit>,
    /* private val */
    valueCol: Column<M, Double>,
    private val constraintSpecCol: Column<M, Int>,
) : MacrosEntityImpl<M>(data, objectSource) {


    val nutrientId: Long
        get() = getData(nutrientIdCol)!!

    val value: Double = getData(valueCol)!!
    val unit: Unit = unitWithId(getData(unitIdCol)!!)
    val nutrient: Nutrient = nutrientWithId(nutrientId)

    // Converts this value into the given unit, if possible.
    // Density is only used when converting quantity (usually in the context of a FoodNutrientValue)
    // An exception is thrown if the conversion is not possible
    open fun convertValueTo(newUnit: Unit, density: Double? = null): Double {
        if (unit == newUnit) {
            return value
        }

        require(nutrient.compatibleWith(newUnit)) { "Cannot convert $nutrient to $newUnit (incompatible types)" }

        var conversionRatio = unit.metricEquivalent / newUnit.metricEquivalent

        if (nutrient == QUANTITY && unit.type != newUnit.type) {
            requireNotNull(density) { "Density required to convert quantity across mass and volume units" }
            if (!unit.isVolumeMeasurement && newUnit.isVolumeMeasurement) {
                // solid units to liquid units
                conversionRatio /= density
            } else if (unit.isVolumeMeasurement && !newUnit.isVolumeMeasurement) {
                // liquid units to solid units
                conversionRatio *= density
            } else {
                error { "Units are of different type but neither one is volume" }
            }
        }

        return value * conversionRatio
    }
}
