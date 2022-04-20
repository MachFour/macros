package com.machfour.macros.units

import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.Unit

fun interface NutrientUnits {
    operator fun get(n: Nutrient): Unit
}