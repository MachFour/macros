package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.core.Schema.UnitTable

/*
 * Units for measuring quantities of food (only). Not for nutrition measurements.
 */
class Unit private constructor(data: ColumnData<Unit>, objectSource: ObjectSource)
    : MacrosEntityImpl<Unit>(data, objectSource), PortionMeasurement {
    companion object {
        // factory before table
        val factory : Factory<Unit> = Factory { dataMap, objectSource -> Unit(dataMap, objectSource) }

        val table: Table<Unit>
            get() = UnitTable.instance
    }

    override val factory: Factory<Unit>
        get() = Companion.factory

    override val table: Table<Unit>
        get() = Companion.table

    // values are cached here instead of using get() because there aren't many units and they're used a lot

    val type: UnitType = UnitType.fromId(getData(UnitTable.TYPE_ID)!!)

    override fun toString(): String = "$name (${abbr}), type ${type.name}"

    override val name: String = getData(UnitTable.NAME)!!

    val abbr: String = getData(UnitTable.ABBREVIATION)!!

    val metricEquivalent = getData(UnitTable.METRIC_EQUIVALENT)!!

    val isInbuilt = getData(UnitTable.INBUILT)!!

    // Measurement interface - for interop with Servings
    override val unitMultiplier = 1.0
    override val baseUnit = this
    override val isVolumeMeasurement = type === UnitType.VOLUME

}
