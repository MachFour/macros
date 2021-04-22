package com.machfour.macros.names

abstract class ColumnStringsImpl protected constructor(
    private val columnNames: ColumnNamer,
    private val unitNames: UnitNamer
) : ColumnStrings, ColumnNamer by columnNames, UnitNamer by unitNames