package com.machfour.macros.units

import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.Unit
import com.machfour.macros.nutrients.Nutrients

internal val standardUnitMap = mapOf(
    Nutrients.QUANTITY to GRAMS,
    Nutrients.ENERGY to KILOJOULES,
    Nutrients.PROTEIN to GRAMS,
    Nutrients.FAT to GRAMS,
    Nutrients.SATURATED_FAT to GRAMS,
    Nutrients.CARBOHYDRATE to GRAMS,
    Nutrients.SUGAR to GRAMS,
    Nutrients.FIBRE to GRAMS,
    Nutrients.SODIUM to MILLIGRAMS,
    Nutrients.POTASSIUM to MILLIGRAMS,
    Nutrients.CALCIUM to MILLIGRAMS,
    Nutrients.IRON to MILLIGRAMS,
    Nutrients.MONOUNSATURATED_FAT to GRAMS,
    Nutrients.POLYUNSATURATED_FAT to GRAMS,
    Nutrients.OMEGA_3_FAT to MILLIGRAMS,
    Nutrients.OMEGA_6_FAT to MILLIGRAMS,
    Nutrients.STARCH to GRAMS,
    Nutrients.SALT to GRAMS,
    Nutrients.WATER to MILLILITRES,
    Nutrients.CARBOHYDRATE_BY_DIFF to GRAMS,
    Nutrients.ALCOHOL to GRAMS,
    Nutrients.SUGAR_ALCOHOL to GRAMS,
    Nutrients.CAFFEINE to MILLIGRAMS,
)

// standard metric units, using milligrams where appropriate
object StandardNutrientUnits: NutrientUnits {
    override operator fun get(n: Nutrient) : Unit {
        return standardUnitMap[n] ?: throw IllegalArgumentException("Nutrient $n has no default unit")
    }
}