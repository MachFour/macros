package com.machfour.macros.entities

import com.machfour.macros.sql.entities.Factory
import com.machfour.macros.sql.entities.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.PortionMeasurement
import com.machfour.macros.sql.entities.Factories
import com.machfour.macros.schema.UnitTable
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.rowdata.RowData
import com.machfour.macros.units.UnitType

class Unit internal constructor(data: RowData<Unit>, source: ObjectSource)
    : MacrosEntityImpl<Unit>(data, source), PortionMeasurement {
    companion object {
        // factory before table
        val factory: Factory<Unit>
            get() = Factories.unit

    }

    override fun getTable(): Table<Unit> {
        return UnitTable
    }

    override val name: String = data[UnitTable.NAME]!!

    val abbr: String = data[UnitTable.ABBREVIATION]!!
    val type: UnitType = UnitType.fromId(this.data[UnitTable.TYPE_ID]!!)
    val metricEquivalent = data[UnitTable.METRIC_EQUIVALENT]!!
    val isInbuilt = data[UnitTable.INBUILT]!!

    // Measurement interface - for interop with Servings
    override val amount = 1.0
    override val unit = this
    override val isVolumeMeasurement = type === UnitType.VOLUME

    private val string = "$name (${abbr})"
    override fun toString(): String = string


}
