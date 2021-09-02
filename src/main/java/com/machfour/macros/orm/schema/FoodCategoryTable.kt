package com.machfour.macros.orm.schema

import com.machfour.macros.entities.FoodCategory
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types

private const val TABLE_NAME = "FoodCategory"

// iteration order of COLUMNS is the order in which columns are defined below
private val COLUMNS = ArrayList<Column<FoodCategory, *>>()

private val _ID = idColumnBuildFor(COLUMNS)
private val _CREATE_TIME = createTimeColumnBuildFor(COLUMNS)
private val _MODIFY_TIME = modifyTimeColumnBuildFor(COLUMNS)
private val _NAME = builder("name", Types.TEXT).notNull().inSecondaryKey().unique().buildFor(COLUMNS)

object FoodCategoryTable: TableImpl<FoodCategory>(TABLE_NAME, FoodCategory.factory, COLUMNS) {
    val ID: Column<FoodCategory, Long>
        get() = _ID
    val CREATE_TIME: Column<FoodCategory, Long>
        get() = _CREATE_TIME
    val MODIFY_TIME: Column<FoodCategory, Long>
        get() = _MODIFY_TIME
    val NAME: Column<FoodCategory, String>
        get() = _NAME
}
