package com.machfour.macros.names

import com.machfour.macros.core.Column
import com.machfour.macros.objects.NutritionData

/*
 * Interface to provide dynamic names and units (and hence strings) for Nutrition data columns
 * Combines interfaces ColumnNamer, ColumnUnits, UnitNames
 */
interface ColumnStrings : ColumnNamer, UnitNamer, ColumnUnits {
    // these methods combine methods of the parent interfaces
    fun getUnitName(col: Column<NutritionData, Double>): String
    fun getUnitAbbr(col: Column<NutritionData, Double>): String
}