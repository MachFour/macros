package com.machfour.macros.entities

import com.machfour.macros.core.Factory
import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.PortionMeasurement
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.schema.UnitTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table
import com.machfour.macros.units.UnitType

/*
 * Units for measuring quantities of food (only). Not for nutrition measurements.
 */
class Unit internal constructor(data: RowData<Unit>, source: ObjectSource)
    : MacrosEntityImpl<Unit>(data, source), PortionMeasurement {
    companion object {
        // factory before table
        val factory: Factory<Unit>
            get() = Factories.unit

    }

    override val factory: Factory<Unit>
        get() = Companion.factory

    override val table: Table<Unit>
        get() = UnitTable

    // values are cached here instead of using get() because there aren't many units but they're used a lot

    override val name: String = this.data[UnitTable.NAME]!!

    val abbr: String = this.data[UnitTable.ABBREVIATION]!!
    val type: UnitType = UnitType.fromId(this.data[UnitTable.TYPE_ID]!!)
    val metricEquivalent = this.data[UnitTable.METRIC_EQUIVALENT]!!
    val isInbuilt = this.data[UnitTable.INBUILT]!!

    // Measurement interface - for interop with Servings
    override val unitMultiplier = 1.0
    override val baseUnit = this
    override val isVolumeMeasurement = type === UnitType.VOLUME

    private val string = "$name (${abbr})" // [${type.name.firstOrNull() ?: "?"}]"
    override fun toString(): String = string


}
