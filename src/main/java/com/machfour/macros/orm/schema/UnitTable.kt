package com.machfour.macros.orm.schema

import com.machfour.macros.entities.Unit
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types

private const val TABLE_NAME = "Unit"

// iteration order of COLUMNS is the order in which columns are defined below
private val COLUMNS = ArrayList<Column<Unit, *>>()

private val _ID = idColumnBuildFor(COLUMNS)
private val _CREATE_TIME = createTimeColumnBuildFor(COLUMNS)
private val _MODIFY_TIME = modifyTimeColumnBuildFor(COLUMNS)

private val _NAME =
    builder("name", Types.TEXT).notNull().buildFor(COLUMNS)
private val _TYPE_ID =
    builder("type_id", Types.INTEGER).notEditable().notNull().buildFor(COLUMNS)
private val _ABBREVIATION =
    builder("abbreviation", Types.TEXT).notNull().inSecondaryKey().unique().buildFor(COLUMNS)
private val _METRIC_EQUIVALENT =
    builder("metric_equivalent", Types.REAL).notNull().buildFor(COLUMNS)
private val _INBUILT =
    builder("inbuilt", Types.BOOLEAN).notNull().defaultsTo(false).buildFor(COLUMNS)

// Unit.factory() causes initialisation of Unit, which depends on this class.
// So the columns are initialised as a side effect of calling that function.
object UnitTable: TableImpl<Unit>(TABLE_NAME, Factories.unit, COLUMNS) {

    val ID: Column<Unit, Long>
        get() = _ID
    val CREATE_TIME: Column<Unit, Long>
        get() = _CREATE_TIME
    val MODIFY_TIME: Column<Unit, Long>
        get() = _MODIFY_TIME
    val TYPE_ID: Column<Unit, Int>
        get() = _TYPE_ID
    val NAME: Column<Unit, String>
        get() = _NAME
    val ABBREVIATION: Column<Unit, String>
        get() = _ABBREVIATION
    val METRIC_EQUIVALENT: Column<Unit, Double>
        get() = _METRIC_EQUIVALENT
    val INBUILT: Column<Unit, Boolean>
        get() = _INBUILT
}

