package com.machfour.macros.names

import com.machfour.macros.core.Column
import com.machfour.macros.objects.NutritionData

/*
 * Interface to provide dynamic names and units (and hence strings) for Nutrition data columns
 * Combines interfaces ColumnNamer, ColumnUnits, UnitNames
 */
interface ColumnStrings : ColumnNamer, UnitNamer