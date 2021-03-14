package com.machfour.macros.entities

import com.machfour.macros.core.*
import com.machfour.macros.core.schema.NutrientTable
import com.machfour.macros.entities.auxiliary.Factories

class Nutrient internal constructor(dataMap: ColumnData<Nutrient>, objectSource: ObjectSource)
    : MacrosEntityImpl<Nutrient>(dataMap, objectSource) {

    companion object {

        // Factory has to be initialised first before table is referenced.
        // This is a problem only if the factory is cached as an instance variable
        val factory : Factory<Nutrient>
            get() = Factories.nutrient

        val table: Table<Nutrient>
            get() = NutrientTable.instance

        fun checkCompatible(n: Nutrient, u: Unit) {
            check(n.unitTypes.contains(u.type)) { "Invalid unit $u for nutrient $n" }
        }
    }

    override val factory: Factory<Nutrient>
        get() = Companion.factory

    override val table: Table<Nutrient>
        get() = Companion.table

    val csvName: String = getData(NutrientTable.NAME)!!

    val unitTypes: Set<UnitType> = UnitType.fromFlags(getData(NutrientTable.UNIT_TYPES)!!)

    val isInbuilt: Boolean = getData(NutrientTable.INBUILT)!!

    fun isConvertibleTo(unit: Unit) : Boolean {
        return unitTypes.contains(unit.type)

    }

    override fun toString() = "$csvName (types: $unitTypes)"
}
