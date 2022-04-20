package com.machfour.macros.entities

import com.machfour.macros.core.Factory
import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.schema.NutrientTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table
import com.machfour.macros.units.UnitType

class Nutrient internal constructor(data: RowData<Nutrient>, source: ObjectSource)
    : MacrosEntityImpl<Nutrient>(data, source) {

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

    val csvName: String = this.data[NutrientTable.NAME]!!
    val isInbuilt: Boolean = this.data[NutrientTable.INBUILT]!!

    private val unitFlags: Int = this.data[NutrientTable.UNIT_TYPES]!!
    private val unitTypes: Set<UnitType> = UnitType.fromFlags(unitFlags)

    fun compatibleWith(unit: Unit): Boolean {
        return unit.type.matchedByFlags(unitFlags)
    }

    override fun toString() = "$csvName (types: $unitTypes)"
}
