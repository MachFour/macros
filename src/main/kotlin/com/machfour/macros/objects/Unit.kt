package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.core.Schema.UnitTable.Companion.ABBREVIATION
import com.machfour.macros.core.Schema.UnitTable.Companion.UNIT_TYPE
import com.machfour.macros.core.Schema.UnitTable.Companion.METRIC_EQUIVALENT
import com.machfour.macros.core.Schema.UnitTable.Companion.NAME

/*
 * Units for measuring quantities of food (only). Not for nutrition measurements.
 */
class Unit private constructor(data: ColumnData<Unit>, objectSource: ObjectSource)
    : MacrosEntityImpl<Unit>(data, objectSource), PortionMeasurement {
    companion object {
        // factory before table
        val factory : Factory<Unit> = Factory { dataMap, objectSource -> Unit(dataMap, objectSource) }

        val table: Table<Unit>
            get() = Schema.UnitTable.instance
    }

    override val factory: Factory<Unit>
        get() = Companion.factory

    override val table: Table<Unit>
        get() = Companion.table

    val unitType: UnitType = UnitType.fromId(getData(UNIT_TYPE)!!)

    override fun toString(): String {
        return "$name (${abbr}), type ${unitType.name}"
    }

    override val name: String
        get() = getData(NAME)!!

    val abbr: String
        get() = getData(ABBREVIATION)!!

    val metricEquivalent: Double
        get() = getData(METRIC_EQUIVALENT)!!

    // Measurement interface - for interop with Servings
    override val unitMultiplier = 1.0
    override val baseUnit = this
    override val isVolumeMeasurement = unitType === UnitType.VOLUME // TODO make into enum

}
