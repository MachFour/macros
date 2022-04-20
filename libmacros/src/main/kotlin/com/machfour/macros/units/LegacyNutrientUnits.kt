package com.machfour.macros.units

import com.machfour.macros.nutrients.ENERGY

// Keeps legacy behaviour (default energy unit is calories)
val LegacyNutrientUnits = NutrientUnits {
    if (it === ENERGY) CALORIES else StandardNutrientUnits[it]
}