package com.machfour.macros.objects

import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.core.Schema

// Class of inbuilt quantity units
// These definitions need to be outside the QtyUnit class itself,
// to avoid static initialisation problems between the QtyUnit and QtyUnitTable classes
object QtyUnits {
    val GRAMS: QtyUnit
    val MILLILITRES: QtyUnit
    val MILLIGRAMS: QtyUnit

    val INBUILT: List<QtyUnit>
    private val ABBREVIATION_MAP: MutableMap<String, QtyUnit>
    private val ID_MAP: MutableMap<Long, QtyUnit>

    init {
        val gramsData = ColumnData(QtyUnit.table)
        gramsData.put(Schema.QtyUnitTable.ID, 1L)
        gramsData.put(Schema.QtyUnitTable.NAME, "grams")
        gramsData.put(Schema.QtyUnitTable.ABBREVIATION, "g")
        gramsData.put(Schema.QtyUnitTable.METRIC_EQUIVALENT, 1.0)
        gramsData.put(Schema.QtyUnitTable.IS_VOLUME_UNIT, false)
        GRAMS = QtyUnit.factory.construct(gramsData, ObjectSource.INBUILT)

        val milsData = ColumnData(QtyUnit.table)
        milsData.put(Schema.QtyUnitTable.ID, 2L)
        milsData.put(Schema.QtyUnitTable.NAME, "millilitres")
        milsData.put(Schema.QtyUnitTable.ABBREVIATION, "ml")
        milsData.put(Schema.QtyUnitTable.METRIC_EQUIVALENT, 1.0)
        milsData.put(Schema.QtyUnitTable.IS_VOLUME_UNIT, true)
        MILLILITRES = QtyUnit.factory.construct(milsData, ObjectSource.INBUILT)

        val mgData = ColumnData(QtyUnit.table)
        mgData.put(Schema.QtyUnitTable.ID, 3L)
        mgData.put(Schema.QtyUnitTable.NAME, "milligrams")
        mgData.put(Schema.QtyUnitTable.ABBREVIATION, "mg")
        mgData.put(Schema.QtyUnitTable.METRIC_EQUIVALENT, 0.001)
        mgData.put(Schema.QtyUnitTable.IS_VOLUME_UNIT, false)
        MILLIGRAMS = QtyUnit.factory.construct(mgData, ObjectSource.INBUILT)

        INBUILT = listOf(GRAMS, MILLIGRAMS, MILLILITRES)

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
    fun fromAbbreviation(abbreviation: String, throwIfNotFound: Boolean): QtyUnit? {
        val abbr = abbreviation.toLowerCase()
        return when {
            ABBREVIATION_MAP.containsKey(abbr) -> ABBREVIATION_MAP[abbr]
            throwIfNotFound -> throw IllegalArgumentException("No QtyUnit exists with abbreviation '$abbreviation'")
            else -> null
        }
    }

    private fun fromId(id: Long, throwIfNotFound: Boolean): QtyUnit? {
        return when {
            ID_MAP.containsKey(id) -> ID_MAP[id]
            throwIfNotFound -> throw IllegalArgumentException("No QtyUnit exists with ID '$id'")
            else -> null
        }
    }

    fun fromAbbreviationNoThrow(abbreviation: String): QtyUnit? {
        return fromAbbreviation(abbreviation, false)
    }

    fun fromAbbreviation(abbreviation: String): QtyUnit {
        return fromAbbreviation(abbreviation, true)!!
    }

    fun fromId(id: Long): QtyUnit? {
        return fromId(id, false)
    }
}
