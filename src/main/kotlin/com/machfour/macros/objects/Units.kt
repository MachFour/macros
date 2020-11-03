package com.machfour.macros.objects

import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.Schema

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

    val INBUILT: List<Unit>
    private val ABBREVIATION_MAP: MutableMap<String, Unit>
    private val ID_MAP: MutableMap<Long, Unit>

    init {
        val factory = Unit.factory

        GRAMS = factory.construct(ColumnData(Unit.table).apply {
                put(Schema.UnitTable.ID, 1L)
                put(Schema.UnitTable.NAME, "grams")
                put(Schema.UnitTable.ABBREVIATION, "g")
                put(Schema.UnitTable.METRIC_EQUIVALENT, 1.0)
                put(Schema.UnitTable.UNIT_TYPE, UnitType.MASS.id)
            }, ObjectSource.INBUILT)

        MILLIGRAMS = factory.construct(ColumnData(Unit.table).apply {
            put(Schema.UnitTable.ID, 2L)
            put(Schema.UnitTable.NAME, "milligrams")
            put(Schema.UnitTable.ABBREVIATION, "mg")
            put(Schema.UnitTable.METRIC_EQUIVALENT, 0.001)
            put(Schema.UnitTable.UNIT_TYPE, UnitType.MASS.id)
        }, ObjectSource.INBUILT)

        MILLILITRES = factory.construct(ColumnData(Unit.table).apply {
            put(Schema.UnitTable.ID, 3L)
            put(Schema.UnitTable.NAME, "millilitres")
            put(Schema.UnitTable.ABBREVIATION, "ml")
            put(Schema.UnitTable.METRIC_EQUIVALENT, 1.0)
            put(Schema.UnitTable.UNIT_TYPE, UnitType.VOLUME.id)
        }, ObjectSource.INBUILT)

        LITRES = factory.construct(ColumnData(Unit.table).apply {
            put(Schema.UnitTable.ID, 4L)
            put(Schema.UnitTable.NAME, "litres")
            put(Schema.UnitTable.ABBREVIATION, "L")
            put(Schema.UnitTable.METRIC_EQUIVALENT, 1000.0)
            put(Schema.UnitTable.UNIT_TYPE, UnitType.VOLUME.id)
        }, ObjectSource.INBUILT)

        KILOJOULES = factory.construct(ColumnData(Unit.table).apply {
            put(Schema.UnitTable.ID, 5L)
            put(Schema.UnitTable.NAME, "kilojoules")
            put(Schema.UnitTable.ABBREVIATION, "kJ")
            put(Schema.UnitTable.METRIC_EQUIVALENT, 1.0)
            put(Schema.UnitTable.UNIT_TYPE, UnitType.ENERGY.id)
        }, ObjectSource.INBUILT)

        CALORIES = factory.construct(ColumnData(Unit.table).apply {
            put(Schema.UnitTable.ID, 6L)
            put(Schema.UnitTable.NAME, "calories")
            put(Schema.UnitTable.ABBREVIATION, "kcal")
            put(Schema.UnitTable.METRIC_EQUIVALENT, 4.186)
            put(Schema.UnitTable.UNIT_TYPE, UnitType.ENERGY.id)
        }, ObjectSource.INBUILT)



        INBUILT = listOf(
            GRAMS,
            MILLIGRAMS,
            LITRES,
            MILLILITRES,
            KILOJOULES,
            CALORIES,
        )

        ABBREVIATION_MAP = HashMap(INBUILT.size, 1.0f)
        ID_MAP = HashMap(INBUILT.size, 1.0f)

        for (u in INBUILT) {
            ABBREVIATION_MAP[u.abbr.toLowerCase()] = u
            ID_MAP[u.id] = u
        }
    }

    /*
     * Case insensitive matching of abbreviation
     */
    private fun fromAbbreviation(abbreviation: String, throwIfNotFound: Boolean): Unit? {
        val abbr = abbreviation.toLowerCase()
        return when {
            ABBREVIATION_MAP.containsKey(abbr) -> ABBREVIATION_MAP[abbr]
            throwIfNotFound -> throw IllegalArgumentException("No Unit exists with abbreviation '$abbreviation'")
            else -> null
        }
    }

    fun fromAbbreviationNoThrow(abbreviation: String): Unit? {
        return fromAbbreviation(abbreviation, false)
    }

    fun fromAbbreviation(abbreviation: String): Unit {
        return fromAbbreviation(abbreviation, true)!!
    }

    fun fromId(id: Long): Unit? = ID_MAP[id]
}
