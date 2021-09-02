package com.machfour.macros.units

import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.Unit
import com.machfour.macros.nutrients.Nutrients


// Keeps legacy behaviour (default energy unit is calories)
object LegacyNutrientUnits: NutrientUnits {
    override operator fun get(n: Nutrient) : Unit {
        return when(n) {
            Nutrients.ENERGY -> Units.CALORIES
            else -> standardUnitMap[n] ?: throw IllegalArgumentException("Nutrient $n has no default unit")
        }
    }
}