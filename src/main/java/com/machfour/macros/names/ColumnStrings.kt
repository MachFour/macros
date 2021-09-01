package com.machfour.macros.names

/*
 * Interface to provide dynamic names and units (and hence strings) for Nutrition data columns
 * Combines interfaces ColumnNamer, ColumnUnits, UnitNames
 */
// TODO rename ColumnStrings to DisplayStrings
interface ColumnStrings : ColumnNamer, UnitStrings, NutrientStrings