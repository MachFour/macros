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
        private val MILLIGRAMS_COLS = Collections.unmodifiableList(listOf(
            NutritionDataTable.SODIUM
            , NutritionDataTable.CALCIUM
            , NutritionDataTable.POTASSIUM
            , NutritionDataTable.IRON
            , NutritionDataTable.OMEGA_3_FAT
            , NutritionDataTable.OMEGA_6_FAT
        ))

        private fun unitForNutrientCol(col: Column<NutritionData, Double>): Unit {
            return when {
                MILLIGRAMS_COLS.contains(col) -> QtyUnits.MILLIGRAMS
                col == NutritionDataTable.CALORIES -> EnergyUnit.CALORIES
                col == NutritionDataTable.KILOJOULES -> EnergyUnit.KILOJOULES
                else -> QtyUnits.GRAMS
            }
        }

        // add all the columns and units
        // TODO use buildMap() when it becomes stable API
        private val UNIT_MAP: Map<Column<NutritionData, Double>, Unit> = run {
            val map = HashMap<Column<NutritionData, Double>, Unit>()
            NutritionData.NUTRIENT_COLUMNS.forEach { col ->
                map[col] = unitForNutrientCol(col)
            }
            Collections.unmodifiableMap(map)
        }

        @JvmStatic
        val instance = DefaultColumnUnits()
    }

    override fun getUnit(col: Column<NutritionData, Double>): Unit {
        return UNIT_MAP[col]
                ?: throw IllegalArgumentException("No such nutrient column: ${col.sqlName}")
    }

    override fun columnsWithUnits(): Collection<Column<NutritionData, Double>> {
        return Collections.unmodifiableCollection(UNIT_MAP.keys)
    }
}
