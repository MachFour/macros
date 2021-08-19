package com.machfour.macros.entities.inbuilt

import com.machfour.macros.orm.ColumnData
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.orm.schema.UnitTable
import com.machfour.macros.entities.Unit
import com.machfour.macros.entities.UnitType

// Class of inbuilt quantity units
// These definitions need to be outside the Unit class itself,
// to avoid static initialisation problems between the Unit and UnitTable classes
object Units {
    val GRAMS: Unit
    val MILLIGRAMS: Unit
    val MILLILITRES: Unit
    val LITRES: Unit
    val KILOJOULES: Unit
    val CALORIES: Unit

    private val abbrMap: MutableMap<String, Unit> = LinkedHashMap(6, 1f)
    private val idMap: MutableMap<Long, Unit> = LinkedHashMap(6, 1f)

    private var nextIndex = 0L

    // this is lowercase so that CSV units can be lower case
    private val String.toMapKey: String
        get() = this.lowercase()

    private fun registerUnit(name: String, abbr: String, metricEquivalent: Double, unitType: UnitType, inbuilt: Boolean): Unit {
        val id = nextIndex++
        val data = ColumnData(Unit.table).apply {
            put(UnitTable.ID, id)
            put(UnitTable.NAME, name)
            put(UnitTable.ABBREVIATION, abbr)
            put(UnitTable.METRIC_EQUIVALENT, metricEquivalent)
            put(UnitTable.TYPE_ID, unitType.id)
            put(UnitTable.INBUILT, inbuilt)
        }

        return Unit.factory.construct(data, ObjectSource.INBUILT).also {
            idMap[id] = it
            val abbrKey = abbr.toMapKey
            require (!abbrMap.containsKey(abbrKey)) {
                "Cannot register unit $name - abbr $abbrKey is already used by ${abbrMap[abbrKey]}"
            }
            abbrMap[abbrKey] = it
        }
    }

    private fun registerInbuiltUnit(name: String, abbr: String, metricEquivalent: Double, unitType: UnitType): Unit {
        return registerUnit(name, abbr, metricEquivalent, unitType, true)
    }

    fun registerUnit(name: String, abbr: String, metricEquivalent: Double, unitType: UnitType): Unit {
        return registerUnit(name, abbr, metricEquivalent, unitType, false)
    }

    init {
        GRAMS = registerInbuiltUnit("grams", "g", 1.0, UnitType.MASS)
        MILLIGRAMS = registerInbuiltUnit("milligrams", "mg", 0.001, UnitType.MASS)
        MILLILITRES = registerInbuiltUnit("milligrams", "ml", 1.0, UnitType.VOLUME)
        LITRES = registerInbuiltUnit("litres", "L", 1000.0, UnitType.VOLUME)
        KILOJOULES = registerInbuiltUnit("kilojoules", "kJ", 1.0, UnitType.ENERGY)
        CALORIES = registerInbuiltUnit("calories", "kcal", 4.186, UnitType.ENERGY)
    }

    // case insensitive
    fun fromAbbreviationNoThrow(abbr: String): Unit? = abbrMap[abbr.toMapKey]

    // case insensitive
    fun fromAbbreviation(abbr: String): Unit = abbrMap.getValue(abbr.toMapKey)

    fun fromIdNoThrow(id: Long): Unit? = idMap[id]

    fun fromId(id: Long): Unit = idMap.getValue(id)
}
