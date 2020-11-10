package com.machfour.macros.objects

import com.machfour.macros.core.*

class Nutrient private constructor(dataMap: ColumnData<Nutrient>, objectSource: ObjectSource)
    : MacrosEntityImpl<Nutrient>(dataMap, objectSource) {

    companion object {

        // Factory has to be initialised first before table is referenced.
        // This is a problem only if the factory is cached as an instance variable
        val factory = Factory<Nutrient> { dataMap, objectSource -> Nutrient(dataMap, objectSource) }

        val table: Table<Nutrient>
            get() = Schema.NutrientTable.instance

        fun checkCompatible(n: Nutrient, u: Unit) {
            check(n.unitTypes.contains(u.type)) { "Invalid unit $u for nutrient $n" }
        }
    }

    override val factory: Factory<Nutrient>
        get() = Companion.factory

    override val table: Table<Nutrient>
        get() = Companion.table

    val csvName: String = getData(Schema.NutrientTable.NAME)!!

    val unitTypes: Set<UnitType> = UnitType.fromFlags(getData(Schema.NutrientTable.UNIT_TYPES)!!)

    val isInbuilt: Boolean = getData(Schema.NutrientTable.INBUILT)!!

    fun isConvertibleTo(unit: Unit) : Boolean {
        return unitTypes.contains(unit.type)

    }

    override fun toString() = "$csvName (types: $unitTypes)"
}
