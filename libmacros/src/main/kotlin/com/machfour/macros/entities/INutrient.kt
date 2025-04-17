package com.machfour.macros.entities

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.units.UnitType

interface INutrient: MacrosEntity {
    val name: String
    val isInbuilt: Boolean
    val unitTypes: Set<UnitType>

    fun compatibleWith(unit: Unit): Boolean {
        return unit.type in unitTypes
    }
}

fun nutrientToString(n: INutrient): String {
    return n.name
}

fun nutrientEquals(n: INutrient, o: Any?): Boolean {
    if (n === o) {
        return true
    }
    if (o !is INutrient) {
        return false
    }
    return (n.id == o.id)
}

fun nutrientHashCode(n: INutrient): Int {
    return n.id.hashCode()
}
