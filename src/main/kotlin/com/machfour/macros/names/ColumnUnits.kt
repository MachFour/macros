package com.machfour.macros.names

import com.machfour.macros.core.Column
import com.machfour.macros.objects.NutritionData
import com.machfour.macros.objects.Unit

interface ColumnUnits {
    fun getUnit(col: Column<NutritionData, Double>): Unit
    val columnsWithUnits: Collection<Column<NutritionData, Double>>
}