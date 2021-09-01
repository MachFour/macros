package com.machfour.macros.names

abstract class ColumnStringsImpl protected constructor(
    private val columnNames: ColumnNamer,
    private val unitNames: UnitStrings,
    private val nutrientStrings: NutrientStrings,
) : ColumnStrings, ColumnNamer by columnNames, UnitStrings by unitNames, NutrientStrings by nutrientStrings