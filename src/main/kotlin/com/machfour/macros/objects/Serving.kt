package com.machfour.macros.objects

import com.machfour.macros.core.*

class Serving internal constructor(data: ColumnData<Serving>, objectSource: ObjectSource)
    : MacrosEntityImpl<Serving>(data, objectSource), PortionMeasurement {

    lateinit var food: Food
        private set

    val qtyUnitAbbr: String
        get() = getData(Schema.ServingTable.QUANTITY_UNIT)!!

    val qtyUnit: QtyUnit = QtyUnits.fromAbbreviation(qtyUnitAbbr)

    override val table: Table<Serving>
        get() = table()
    override val factory: Factory<Serving>
        get() = factory()

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
        get() = qtyUnit.isVolumeUnit

    override val isServing = true

    companion object {
        fun table(): Table<Serving> {
            return Schema.ServingTable.instance
        }

        fun factory(): Factory<Serving> {
            return object : Factory<Serving> {
                override fun construct(dataMap: ColumnData<Serving>, objectSource: ObjectSource): Serving {
                    return Serving(dataMap, objectSource)
                }
            }
        }
    }
}
