package com.machfour.macros.entities

import com.machfour.macros.sql.entities.Factory
import com.machfour.macros.sql.entities.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.sql.entities.Factories
import com.machfour.macros.schema.NutrientTable
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.rowdata.RowData
import com.machfour.macros.units.UnitType

typealias Nutrient = NutrientImpl

class NutrientImpl internal constructor(data: RowData<Nutrient>, source: ObjectSource)
    : MacrosEntityImpl<Nutrient>(data, source), INutrient {

    companion object {
        // Factory has to be initialised first before table is referenced.
        // This is a problem only if the factory is cached as an instance variable
        val factory: Factory<Nutrient>
            get() = Factories.nutrient

    }

    override val factory: Factory<Nutrient>
        get() = Companion.factory

    override val table: Table<Nutrient>
        get() = NutrientTable

    override val name = this.data[NutrientTable.NAME]!!

    override val isInbuilt: Boolean = this.data[NutrientTable.INBUILT]!!

    private val unitFlags: Int
        get() = this.data[NutrientTable.UNIT_TYPES]!!

    override val unitTypes: Set<UnitType> = UnitType.fromFlags(unitFlags)

    override fun compatibleWith(unit: Unit): Boolean {
        return unit.type in unitTypes
    }

    override fun toString(): String {
        return nutrientToString(this)
    }

    override fun equals(other: Any?): Boolean {
        return nutrientEquals(this, other)
    }

    override fun hashCode(): Int {
        return nutrientHashCode(this)
    }

}
