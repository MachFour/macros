package com.machfour.macros.names

import com.machfour.macros.core.Column
import com.machfour.macros.objects.NutritionData
import com.machfour.macros.objects.Unit

abstract class ColumnStringsImpl protected constructor(
        private val columnNames: ColumnNamer,
        private val unitNames: UnitNamer,
        private val columnUnits: ColumnUnits) : ColumnStrings {

    // columnNames
    override fun getName(col: Column<*, *>): String = columnNames.getName(col)
    override fun getAbbreviatedName(col: Column<*, *>): String = columnNames.getAbbreviatedName(col)

    // unitNames
    override fun getName(unit: Unit): String = unitNames.getName(unit)
    override fun getAbbr(unit: Unit): String = unitNames.getAbbr(unit)
    override fun availableUnits(): Collection<Unit> = unitNames.availableUnits()

    // columnUnits
    override fun getUnit(col: Column<NutritionData, Double>): Unit = columnUnits.getUnit(col)
    override fun columnsWithUnits(): Collection<Column<NutritionData, Double>> = columnUnits.columnsWithUnits()

    // convenience / linking
    override fun getUnitName(col: Column<NutritionData, Double>): String = getName(getUnit(col))
    override fun getUnitAbbr(col: Column<NutritionData, Double>): String = getAbbr(getUnit(col))



}