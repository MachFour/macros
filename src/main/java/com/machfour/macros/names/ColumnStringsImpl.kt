package com.machfour.macros.names

import com.machfour.macros.core.Column
import com.machfour.macros.objects.Nutrient
import com.machfour.macros.objects.Unit

abstract class ColumnStringsImpl protected constructor(
    private val columnNames: ColumnNamer,
    private val unitNames: UnitNamer
) : ColumnStrings {

    // columnNames
    override fun getName(col: Column<*, *>): String = columnNames.getName(col)
    override fun getAbbreviatedName(col: Column<*, *>): String = columnNames.getAbbreviatedName(col)

    override fun getName(n: Nutrient): String = columnNames.getName(n)
    override fun getAbbreviatedName(n: Nutrient): String = columnNames.getAbbreviatedName(n)

    // unitNames
    override fun getName(unit: Unit): String = unitNames.getName(unit)
    override fun getAbbr(unit: Unit): String = unitNames.getAbbr(unit)
    override val availableUnits: Collection<Unit>
        get() = unitNames.availableUnits

}