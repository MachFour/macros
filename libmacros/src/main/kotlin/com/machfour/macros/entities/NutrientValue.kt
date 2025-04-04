package com.machfour.macros.entities

import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.nutrients.convertUnit
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
        get() = data[nutrientIdCol]!!

    val value: Double = this.data[valueCol]!!
    val unit: Unit = unitWithId(this.data[unitIdCol]!!)
    val nutrient: Nutrient = nutrientWithId(nutrientId)
    val constraintSpec: Int
        get() = this.data[constraintSpecCol]!!

    // Converts this value into the given unit, if possible.
    // Density is only used when converting quantity (usually in the context of a FoodNutrientValue)
    // An exception is thrown if the conversion is not possible
    open fun convertValueTo(newUnit: Unit, density: Double? = null): Double {
        return convertUnit(nutrient, value, unit, newUnit, density)
    }
}
