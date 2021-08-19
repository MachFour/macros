package com.machfour.macros.entities

import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.orm.Factory
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.orm.schema.NutrientTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table

class Nutrient internal constructor(dataMap: RowData<Nutrient>, objectSource: ObjectSource)
    : MacrosEntityImpl<Nutrient>(dataMap, objectSource) {

    companion object {
        // Factory has to be initialised first before table is referenced.
        // This is a problem only if the factory is cached as an instance variable
        val factory : Factory<Nutrient>
            get() = Factories.nutrient

        val table: Table<Nutrient>
            get() = NutrientTable.instance
    }

    override val factory: Factory<Nutrient>
        get() = Companion.factory

    override val table: Table<Nutrient>
        get() = Companion.table

    val csvName: String = getData(NutrientTable.NAME)!!
    val isInbuilt: Boolean = getData(NutrientTable.INBUILT)!!

    private val unitFlags: Int = getData(NutrientTable.UNIT_TYPES)!!
    private val unitTypes: Set<UnitType> = UnitType.fromFlags(unitFlags)

    fun compatibleWithUnit(unit: Unit) : Boolean {
        return unit.type.matchedByFlags(unitFlags)
    }

    override fun toString() = "$csvName (types: $unitTypes)"
}
