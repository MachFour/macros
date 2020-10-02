package com.machfour.macros.names

import com.machfour.macros.core.Column
import com.machfour.macros.core.Schema.NutritionDataTable
import com.machfour.macros.objects.EnergyUnit
import com.machfour.macros.objects.NutritionData
import com.machfour.macros.objects.QtyUnits
import com.machfour.macros.objects.Unit
import java.util.Collections;
import kotlin.collections.HashMap

/*
 * Singleton pattern
 */
class DefaultColumnUnits private constructor() : ColumnUnits {
    companion object {
        private val milligramsColumns = linkedSetOf(
            NutritionDataTable.SODIUM
            , NutritionDataTable.CALCIUM
            , NutritionDataTable.POTASSIUM
            , NutritionDataTable.IRON
            , NutritionDataTable.OMEGA_3_FAT
            , NutritionDataTable.OMEGA_6_FAT
        )

        private fun unitForNutrientCol(col: Column<NutritionData, Double>): Unit {
            return when {
                milligramsColumns.contains(col) -> QtyUnits.MILLIGRAMS
                col == NutritionDataTable.CALORIES -> EnergyUnit.Calories
                col == NutritionDataTable.KILOJOULES -> EnergyUnit.Kilojoules
                else -> QtyUnits.GRAMS
            }
        }

        // add all the columns and units
        // TODO use buildMap() when it becomes stable API
        private val unitMap = HashMap<Column<NutritionData, Double>, Unit>().let {
            for (col in NutritionData.nutrientColumns) {
                    it[col] = unitForNutrientCol(col)
            }
            Collections.unmodifiableMap(it)
        }

        val instance = DefaultColumnUnits()
    }

    override fun getUnit(col: Column<NutritionData, Double>): Unit = unitMap.getValue(col)

    override val columnsWithUnits: Collection<Column<NutritionData, Double>>
        get() = Collections.unmodifiableCollection(unitMap.keys)
}
