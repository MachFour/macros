package com.machfour.macros.names

/*
 * Interface to provide dynamic names and units (and hence strings) for Nutrition data columns
 * Combines interfaces ColumnNamer, ColumnUnits, UnitNames
 */
interface ColumnStrings : ColumnNamer, UnitNamer