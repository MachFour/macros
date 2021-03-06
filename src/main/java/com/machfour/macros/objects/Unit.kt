package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.core.schema.UnitTable
import com.machfour.macros.objects.helpers.Factories

/*
 * Units for measuring quantities of food (only). Not for nutrition measurements.
 */
class Unit internal constructor(data: ColumnData<Unit>, objectSource: ObjectSource)
    : MacrosEntityImpl<Unit>(data, objectSource), PortionMeasurement {
    companion object {
        // factory before table
        val factory : Factory<Unit>
            get() = Factories.unit

        val table: Table<Unit>
            get() = UnitTable.instance
    }

    override val factory: Factory<Unit>
        get() = Companion.factory

    override val table: Table<Unit>
        get() = Companion.table

    // values are cached here instead of using get() because there aren't many units but they're used a lot

    override val name: String = getData(UnitTable.NAME)!!

    val abbr: String = getData(UnitTable.ABBREVIATION)!!
    val type: UnitType = UnitType.fromId(getData(UnitTable.TYPE_ID)!!)
    val metricEquivalent = getData(UnitTable.METRIC_EQUIVALENT)!!
    val isInbuilt = getData(UnitTable.INBUILT)!!

    // Measurement interface - for interop with Servings
    override val unitMultiplier = 1.0
    override val baseUnit = this
    override val isVolumeMeasurement = type === UnitType.VOLUME

    private val string = "$name (${abbr})" // [${type.name.firstOrNull() ?: "?"}]"
    override fun toString(): String = string



}
