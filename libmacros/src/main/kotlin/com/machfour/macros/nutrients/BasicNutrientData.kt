package com.machfour.macros.nutrients

import com.machfour.macros.entities.INutrient
import com.machfour.macros.entities.Unit
import com.machfour.macros.units.NutrientUnits

interface BasicNutrientData<out E: INutrientValue> {

    // amount may be zero if this object is not related to a food
    val perQuantity: IQuantity

    // Average density of food, if this NutrientData corresponds to a food
    // and its density is known, otherwise null. If not null, the value
    // allows conversion between liquid and solid quantity units.
    val foodDensity: Double?
    fun getUnit(n: INutrient): Unit?
    fun getUnit(n: INutrient, defaultUnits: NutrientUnits): Unit {
        return getUnit(n) ?: defaultUnits[n]
    }

    fun amountOf(n: INutrient, unit: Unit? = null): Double? {
        return if (unit != null) {
            require(n.compatibleWith(unit)) { "Cannot convert nutrient $n to $unit" }
            getValueOrNull(n)?.convertValueTo(unit)
        } else {
            getValueOrNull(n)?.amount
        }
    }

    fun amountOf(n: INutrient, unit: Unit? = null, defaultValue: Double): Double {
        return amountOf(n, unit) ?: defaultValue
    }

    fun getValue(n: INutrient): E {
        return checkNotNull(getValueOrNull(n))
    }

    fun getValueOrNull(n: INutrient): E?

    fun hasNutrient(n: INutrient): Boolean
    fun hasIncompleteData(n: INutrient): Boolean

    fun incompleteDataNutrients(): Set<INutrient>

    fun getEnergyProportion(n: INutrient) : Double

    fun nutrientValues(): Map<INutrient, E>

}

fun nutrientDataToString(nd: BasicNutrientData<*>): String {
    return buildString {
        append("NutrientData(${nd.javaClass.simpleName}) [")
        for ((n, nv) in nd.nutrientValues()) {
            append("${n.name}: ${nv.amount} ${nv.unit.abbr}")
            if (nd.hasIncompleteData(n)) {
                append(" (*)")
            }
            append(", ")
        }
        append("]")
    }
}