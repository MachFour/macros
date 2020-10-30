package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.core.Schema.QtyUnitTable.Companion.ABBREVIATION
import com.machfour.macros.core.Schema.QtyUnitTable.Companion.IS_VOLUME_UNIT
import com.machfour.macros.core.Schema.QtyUnitTable.Companion.METRIC_EQUIVALENT
import com.machfour.macros.core.Schema.QtyUnitTable.Companion.NAME

/*
 * Units for measuring quantities of food (only). Not for nutrition measurements.
 */
class QtyUnit private constructor(data: ColumnData<QtyUnit>, objectSource: ObjectSource)
    : MacrosEntityImpl<QtyUnit>(data, objectSource), PortionMeasurement, Unit {
    companion object {
        // factory before table
        val factory : Factory<QtyUnit> = Factory { dataMap, objectSource -> QtyUnit(dataMap, objectSource) }

        val table: Table<QtyUnit>
            get() = Schema.QtyUnitTable.instance
    }

    override val factory: Factory<QtyUnit>
        get() = Companion.factory

    override val table: Table<QtyUnit>
        get() = Companion.table

    val isVolumeUnit: Boolean
        get() = getData(IS_VOLUME_UNIT)!!

    override fun toString(): String {
        return "$name (${abbr})"
    }

    override val name: String
        get() = getData(NAME)!!

    override val abbr: String
        get() = getData(ABBREVIATION)!!

    fun metricEquivalent(): Double {
        return getData(METRIC_EQUIVALENT)!!
    }

    // Measurement interface
    override val unitMultiplier = 1.0
    override val baseUnit = this
    override val isVolumeMeasurement = isVolumeUnit

}
