package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.objects.helpers.Factories
import com.machfour.macros.objects.inbuilt.Nutrients.QUANTITY
import com.machfour.macros.objects.inbuilt.Units

class Serving internal constructor(data: ColumnData<Serving>, objectSource: ObjectSource)
    : MacrosEntityImpl<Serving>(data, objectSource), PortionMeasurement {

    companion object {
        // factory has to come before table if it's an instance variable
        val factory: Factory<Serving>
            get() = Factories.serving
        val table: Table<Serving>
            get() = Schema.ServingTable.instance
    }

    val qtyUnitAbbr: String
        get() = getData(Schema.ServingTable.QUANTITY_UNIT)!!

    val qtyUnit = Units.fromAbbreviation(qtyUnitAbbr)

    init {
        Nutrient.checkCompatible(QUANTITY, qtyUnit)
    }

    lateinit var food: Food
        private set

    override val factory: Factory<Serving>
        get() = Companion.factory
    override val table: Table<Serving>
        get() = Companion.table

    val foodId: Long
        get() = getData(Schema.ServingTable.FOOD_ID)!!

    val quantity: Double
        get() = getData(Schema.ServingTable.QUANTITY)!!

    val isDefault: Boolean
        get() = getData(Schema.ServingTable.IS_DEFAULT)!!

    override fun equals(other: Any?): Boolean {
        return other is Serving && super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    fun initFood(f: Food) {
        assert(foreignKeyMatches(this, Schema.ServingTable.FOOD_ID, f))
        food = f
    }

    override val name: String = getData(Schema.ServingTable.NAME)!!

    // Measurement functions
    override val unitMultiplier = quantity

    override val baseUnit
        get() = qtyUnit

    override val isVolumeMeasurement
        get() = qtyUnit.isVolumeMeasurement

}
