package com.machfour.macros.entities

import com.machfour.macros.core.Factory
import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.PortionMeasurement
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.nutrients.IQuantity
import com.machfour.macros.nutrients.QUANTITY
import com.machfour.macros.nutrients.Quantity
import com.machfour.macros.schema.ServingTable
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.rowdata.RowData
import com.machfour.macros.units.unitWithAbbr

class Serving internal constructor(data: RowData<Serving>, objectSource: ObjectSource) :
    MacrosEntityImpl<Serving>(data, objectSource), IServing, PortionMeasurement {

    companion object {
        // factory has to come before table if it's an instance variable
        val factory: Factory<Serving>
            get() = Factories.serving
    }

    override val name: String
        get() = data[ServingTable.NAME]!!

    // Measurement functions
    override val amount
        get() = quantity.amount

    val qtyUnitAbbr: String
        get() = data[ServingTable.QUANTITY_UNIT]!!

    override val unit = unitWithAbbr(qtyUnitAbbr)

    override val isVolumeMeasurement
        get() = unit.isVolumeMeasurement


    init {
        check(QUANTITY.compatibleWith(unit)) { "Invalid quantity unit $unit" }
    }

    override val factory: Factory<Serving>
        get() = Companion.factory
    override val table: Table<Serving>
        get() = ServingTable

    override fun toRowData(): RowData<Serving> {
        return super<MacrosEntityImpl>.toRowData()
    }

    override val foodId: Long
        get() = data[ServingTable.FOOD_ID]!!

    override val quantity: IQuantity = Quantity(
        amount = data[ServingTable.QUANTITY]!!,
        unit = unit,
    )

    override val isDefault: Boolean
        get() = data[ServingTable.IS_DEFAULT]!!

    override val notes: String?
        get() = data[ServingTable.NOTES]

    override fun equals(other: Any?): Boolean {
        return other is Serving && super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

}
