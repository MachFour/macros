package com.machfour.macros.nutrients

import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.Unit
import com.machfour.macros.units.NutrientUnits

interface NutrientData {

    // Average density of food, if this NutrientData corresponds to a food
    // and its density is known, otherwise null. If not null, the value
    // allows conversion between liquid and solid quantity units.
    val foodDensity: Double?
    fun getUnit(n: Nutrient): Unit?
    fun getUnit(n: Nutrient, defaultUnits: NutrientUnits): Unit

    fun amountOf(n: Nutrient, unit: Unit? = null): Double?
    fun amountOf(n: Nutrient, unit: Unit? = null, defaultValue: Double): Double {
        return amountOf(n, unit) ?: defaultValue
    }

    fun hasNutrient(n: Nutrient): Boolean
    fun hasCompleteData(n: Nutrient): Boolean

    fun getEnergyProportion(n: Nutrient) : Double
}