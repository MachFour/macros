package com.machfour.macros.orm.schema

import com.machfour.macros.entities.FoodAttribute
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types

private const val TABLE_NAME = "FoodAttribute"

// iteration order of COLUMNS is the order in which columns are defined below
private val COLUMNS = ArrayList<Column<FoodAttribute, *>>()

private val _ID = idColumnBuildFor(COLUMNS)
private val _CREATE_TIME = createTimeColumnBuildFor(COLUMNS)
private val _MODIFY_TIME = modifyTimeColumnBuildFor(COLUMNS)
private val _NAME = builder("name", Types.TEXT).notNull().inSecondaryKey().unique().buildFor(COLUMNS)

object FoodAttributeTable: TableImpl<FoodAttribute>(TABLE_NAME, FoodAttribute.factory, COLUMNS) {
    val ID: Column<FoodAttribute, Long>
        get() = _ID
    val CREATE_TIME: Column<FoodAttribute, Long>
        get() = _CREATE_TIME
    val MODIFY_TIME: Column<FoodAttribute, Long>
        get() = _MODIFY_TIME
    val NAME: Column<FoodAttribute, String>
        get() = _NAME
}

