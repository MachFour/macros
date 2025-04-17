package com.machfour.macros.nutrients

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.entities.INutrient
import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.Unit

interface INutrientValue: MacrosEntity {
    val nutrient: INutrient
    val amount: Double
    val unit: Unit
    val constraintSpec: Int

    // true if this value is part of an aggregation (sum) of nutrient data, and
    // at least one nutrient data object is missing a value for this nutrient
    //val isIncompleteTotal: Boolean

    // Converts this value into the given unit, if possible.
    // An exception is thrown if the conversion is not possible
    fun convertValueTo(newUnit: Unit): Double {
        require (nutrient != QUANTITY) { "use IQuantity.convertAmountTo() for quantity conversions"}
        return convertNutrient(nutrient, amount, unit, newUnit)
    }
}

fun nutrientValueEquals(nv: INutrientValue, other: Any?): Boolean {
    if (other !is INutrientValue) {
        return false
    }
    return nv.id == other.id
            && nv.createTime == other.createTime
            && nv.modifyTime == other.modifyTime
            && nv.source == other.source
            && nv.nutrient == other.nutrient
            && nv.amount == other.amount
            && nv.unit == other.unit
            && nv.constraintSpec == other.constraintSpec
}

interface IQuantity: INutrientValue {
    override val nutrient: Nutrient
        get() = QUANTITY
    override val amount: Double
    override val unit: Unit

    override val constraintSpec: Int
        get() = 0

    // Converts this quantity into the given unit, if possible.
    // An exception is thrown if the conversion is not possible
    fun convertAmountTo(newUnit: Unit, density: Double? = null): Double {
        return convertQuantity(amount, unit, newUnit, density)
    }
}
