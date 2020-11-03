package com.machfour.macros.names

import com.machfour.macros.core.Column
import com.machfour.macros.objects.NutritionData
import com.machfour.macros.objects.IUnit

interface ColumnUnits {
    fun getUnit(col: Column<NutritionData, Double>): IUnit
    val columnsWithUnits: Collection<Column<NutritionData, Double>>
}