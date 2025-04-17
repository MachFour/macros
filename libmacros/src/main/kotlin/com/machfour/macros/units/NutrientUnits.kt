package com.machfour.macros.units

import com.machfour.macros.entities.INutrient
import com.machfour.macros.entities.Unit

fun interface NutrientUnits {
    operator fun get(n: INutrient): Unit
}