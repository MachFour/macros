package com.machfour.macros.entities.inbuilt

import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.Unit


object DefaultUnits {
    private val defaultUnitMap = mapOf(
        Nutrients.QUANTITY to Units.GRAMS,
        Nutrients.ENERGY to Units.CALORIES,
        Nutrients.PROTEIN to Units.GRAMS,
        Nutrients.FAT to Units.GRAMS,
        Nutrients.SATURATED_FAT to Units.GRAMS,
        Nutrients.CARBOHYDRATE to Units.GRAMS,
        Nutrients.SUGAR to Units.GRAMS,
        Nutrients.FIBRE to Units.GRAMS,
        Nutrients.SODIUM to Units.MILLIGRAMS,
        Nutrients.POTASSIUM to Units.MILLIGRAMS,
        Nutrients.CALCIUM to Units.MILLIGRAMS,
        Nutrients.IRON to Units.MILLIGRAMS,
        Nutrients.MONOUNSATURATED_FAT to Units.GRAMS,
        Nutrients.POLYUNSATURATED_FAT to Units.GRAMS,
        Nutrients.OMEGA_3_FAT to Units.MILLIGRAMS,
        Nutrients.OMEGA_6_FAT to Units.MILLIGRAMS,
        Nutrients.STARCH to Units.GRAMS,
        Nutrients.SALT to Units.GRAMS,
        Nutrients.WATER to Units.MILLILITRES,
        Nutrients.CARBOHYDRATE_BY_DIFF to Units.GRAMS,
        Nutrients.ALCOHOL to Units.GRAMS,
        Nutrients.SUGAR_ALCOHOL to Units.GRAMS,
        Nutrients.CAFFEINE to Units.MILLIGRAMS,
    )

    fun get(n: Nutrient) : Unit {
        return defaultUnitMap.getValue(n)
    }
}