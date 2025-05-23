package com.machfour.macros.units

import com.machfour.macros.entities.INutrient
import com.machfour.macros.entities.Unit
import com.machfour.macros.nutrients.*

private val standardUnitMap = mapOf(
    QUANTITY to GRAMS,
    ENERGY to KILOJOULES,
    PROTEIN to GRAMS,
    FAT to GRAMS,
    SATURATED_FAT to GRAMS,
    CARBOHYDRATE to GRAMS,
    SUGAR to GRAMS,
    FIBRE to GRAMS,
    SODIUM to MILLIGRAMS,
    POTASSIUM to MILLIGRAMS,
    CALCIUM to MILLIGRAMS,
    IRON to MILLIGRAMS,
    MONOUNSATURATED_FAT to GRAMS,
    POLYUNSATURATED_FAT to GRAMS,
    OMEGA_3_FAT to MILLIGRAMS,
    OMEGA_6_FAT to MILLIGRAMS,
    STARCH to GRAMS,
    SALT to GRAMS,
    WATER to MILLILITRES,
    CARBOHYDRATE_BY_DIFF to GRAMS,
    ALCOHOL to GRAMS,
    SUGAR_ALCOHOL to GRAMS,
    CAFFEINE to MILLIGRAMS,
    ERYTHRITOL to GRAMS,
    GLYCEROL to GRAMS,
    ISOMALT to GRAMS,
    LACTITOL to GRAMS,
    MALTITOL to GRAMS,
    MANNITOL to GRAMS,
    SORBITOL to GRAMS,
    XYLITOL to GRAMS,
    POLYDEXTROSE to GRAMS,
)

// standard metric units, using milligrams where appropriate
object StandardNutrientUnits: NutrientUnits {
    override operator fun get(n: INutrient) : Unit {
        return standardUnitMap[n]
            ?: unitsCompatibleWith(n).firstOrNull()
            ?: throw IllegalArgumentException("Nutrient $n has no compatible units")
    }
}