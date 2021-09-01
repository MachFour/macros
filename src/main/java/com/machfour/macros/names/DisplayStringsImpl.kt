package com.machfour.macros.names

abstract class DisplayStringsImpl protected constructor(
    private val columnNames: ColumnStrings,
    private val nutrientStrings: NutrientStrings,
    private val unitNames: UnitStrings,
) : DisplayStrings, ColumnStrings by columnNames, NutrientStrings by nutrientStrings, UnitStrings by unitNames