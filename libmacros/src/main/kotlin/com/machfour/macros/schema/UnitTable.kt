package com.machfour.macros.schema

import com.machfour.macros.entities.Unit
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types

private const val tableName = "Unit"

// iteration order of columns is the order in which columns are defined below
private val columns = ArrayList<Column<Unit, out Any>>()

private val id = idColumnBuildFor(columns)
private val createTime = createTimeColumnBuildFor(columns)
private val modifyTime = modifyTimeColumnBuildFor(columns)

private val name =
    builder("name", Types.TEXT).notNull().buildFor(columns)
private val typeId =
    builder("type_id", Types.INTEGER).notEditable().notNull().buildFor(columns)
private val abbreviation =
    builder("abbreviation", Types.TEXT).notNull().inSecondaryKey().unique().buildFor(columns)
private val metricEquivalent =
    builder("metric_equivalent", Types.REAL).notNull().buildFor(columns)
private val inbuilt =
    builder("inbuilt", Types.BOOLEAN).notNull().defaultsTo(false).buildFor(columns)

// Unit.factory() causes initialisation of Unit, which depends on this class.
// So the columns are initialised as a side effect of calling that function.
object UnitTable: TableImpl<Unit>(tableName, Factories.unit, columns) {
    val ID: Column<Unit, Long>
        get() = id
    val CREATE_TIME: Column<Unit, Long>
        get() = createTime
    val MODIFY_TIME: Column<Unit, Long>
        get() = modifyTime
    val TYPE_ID: Column<Unit, Int>
        get() = typeId
    val NAME: Column<Unit, String>
        get() = com.machfour.macros.schema.name
    val ABBREVIATION: Column<Unit, String>
        get() = abbreviation
    val METRIC_EQUIVALENT: Column<Unit, Double>
        get() = metricEquivalent
    val INBUILT: Column<Unit, Boolean>
        get() = inbuilt
}

