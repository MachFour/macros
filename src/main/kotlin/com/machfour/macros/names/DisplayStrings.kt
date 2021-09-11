package com.machfour.macros.names

/*
 * Interface to provide dynamic names and units (and hence strings) for Nutrition data columns
 * Combines interfaces ColumnStrings, UnitStrings, NutrientStrings
 */
interface DisplayStrings: ColumnStrings, NutrientStrings, UnitStrings