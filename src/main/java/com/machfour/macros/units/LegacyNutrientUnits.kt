package com.machfour.macros.units

import com.machfour.macros.nutrients.Nutrients

// Keeps legacy behaviour (default energy unit is calories)
val LegacyNutrientUnits = NutrientUnits {
    if (it === Nutrients.ENERGY) CALORIES else StandardNutrientUnits[it]
}