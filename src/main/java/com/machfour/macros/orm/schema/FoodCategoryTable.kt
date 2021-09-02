package com.machfour.macros.orm.schema

import com.machfour.macros.entities.FoodCategory
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types

private const val tableName = "FoodCategory"

// iteration order of columns is the order in which columns are defined below
private val columns = ArrayList<Column<FoodCategory, *>>()

private val id = idColumnBuildFor(columns)
private val createTime = createTimeColumnBuildFor(columns)
private val modifyTime = modifyTimeColumnBuildFor(columns)
private val name = builder("name", Types.TEXT).notNull().inSecondaryKey().unique().buildFor(columns)

object FoodCategoryTable: TableImpl<FoodCategory>(tableName, FoodCategory.factory, columns) {
    val ID: Column<FoodCategory, Long>
        get() = id
    val CREATE_TIME: Column<FoodCategory, Long>
        get() = createTime
    val MODIFY_TIME: Column<FoodCategory, Long>
        get() = modifyTime
    val NAME: Column<FoodCategory, String>
        get() = com.machfour.macros.orm.schema.name
}
