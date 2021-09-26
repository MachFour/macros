package com.machfour.macros.entities

import com.machfour.macros.core.Factory
import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.PortionMeasurement
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.nutrients.QUANTITY
import com.machfour.macros.schema.ServingTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table
import com.machfour.macros.units.unitWithAbbr

class Serving internal constructor(data: RowData<Serving>, objectSource: ObjectSource) :
    MacrosEntityImpl<Serving>(data, objectSource), PortionMeasurement {

    companion object {
        // factory has to come before table if it's an instance variable
        val factory: Factory<Serving>
            get() = Factories.serving
        val table: Table<Serving>
            get() = ServingTable
    }

    val qtyUnitAbbr: String
        get() = getData(ServingTable.QUANTITY_UNIT)!!

    val qtyUnit = unitWithAbbr(qtyUnitAbbr)

    init {
        check(QUANTITY.compatibleWith(qtyUnit)) { "Invalid unit $qtyUnit for nutrient $QUANTITY" }
    }

    lateinit var food: Food
        private set

    override val factory: Factory<Serving>
        get() = Companion.factory
    override val table: Table<Serving>
        get() = Companion.table

    val foodId: Long
        get() = getData(ServingTable.FOOD_ID)!!

    val quantity: Double
        get() = getData(ServingTable.QUANTITY)!!

    val isDefault: Boolean
        get() = getData(ServingTable.IS_DEFAULT)!!

    val notes: String?
        get() = getData(ServingTable.NOTES)

    override fun equals(other: Any?): Boolean {
        return other is Serving && super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    fun initFood(f: Food) {
        assert(foreignKeyMatches(this, ServingTable.FOOD_ID, f))
        food = f
    }

    override val name: String
        get() = getData(ServingTable.NAME)!!


    // Measurement functions
    override val unitMultiplier = quantity

    override val baseUnit
        get() = qtyUnit

    override val isVolumeMeasurement
        get() = qtyUnit.isVolumeMeasurement

}