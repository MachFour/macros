package com.machfour.macros.schema

import com.machfour.macros.core.EntityId
import com.machfour.macros.entities.FoodAttribute
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types

private const val tableName = "FoodAttribute"

// iteration order of columns is the order in which columns are defined below
private val columns = ArrayList<Column<FoodAttribute, out Any>>()

private val id = idColumnBuildFor(columns)
private val createTime = createTimeColumnBuildFor(columns)
private val modifyTime = modifyTimeColumnBuildFor(columns)
private val name = builder("name", Types.TEXT).notNull().unique().buildFor(columns)

object FoodAttributeTable: TableImpl<FoodAttribute, FoodAttribute>(tableName, FoodAttribute.factory, columns) {
    val ID: Column<FoodAttribute, EntityId>
        get() = id
    val CREATE_TIME: Column<FoodAttribute, Long>
        get() = createTime
    val MODIFY_TIME: Column<FoodAttribute, Long>
        get() = modifyTime
    val NAME: Column<FoodAttribute, String>
        get() = name
}

