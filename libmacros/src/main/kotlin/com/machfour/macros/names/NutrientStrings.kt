package com.machfour.macros.names

import com.machfour.macros.entities.INutrient

interface NutrientStrings {
    fun getFullName(n: INutrient): String
    fun getDisplayName(n: INutrient): String
    fun getAbbreviatedName(n: INutrient): String = getDisplayName(n)
}