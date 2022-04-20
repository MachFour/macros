package com.machfour.macros.names

import com.machfour.macros.entities.Nutrient

interface NutrientStrings {
    fun getFullName(n: Nutrient): String
    fun getDisplayName(n: Nutrient): String
    fun getAbbreviatedName(n: Nutrient): String = getDisplayName(n)
}