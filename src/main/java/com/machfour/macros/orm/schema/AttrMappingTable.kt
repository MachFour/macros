package com.machfour.macros.orm.schema

import com.machfour.macros.entities.AttrMapping
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodAttribute
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types


private const val TABLE_NAME = "AttributeMapping"

// iteration order of COLUMNS is the order in which columns are defined below
private val COLUMNS = ArrayList<Column<AttrMapping, *>>()

private val _ID = idColumnBuildFor(COLUMNS)
private val _CREATE_TIME = createTimeColumnBuildFor(COLUMNS)
private val _MODIFY_TIME = modifyTimeColumnBuildFor(COLUMNS)
private val _FOOD_ID =
    builder("food_id", Types.ID).notEditable().notNull().inSecondaryKey()
        .buildFkFor(FoodTable, FoodTable.ID, COLUMNS )
private val _ATTRIBUTE_ID =
    builder("attribute_id", Types.ID).notEditable().notNull().inSecondaryKey()
        .buildFkFor(FoodAttributeTable, FoodAttributeTable.ID, COLUMNS)

object AttrMappingTable: TableImpl<AttrMapping>(TABLE_NAME, AttrMapping.factory, COLUMNS) {
    val ID: Column<AttrMapping, Long>
        get() = _ID
    val CREATE_TIME: Column<AttrMapping, Long>
        get() = _CREATE_TIME
    val MODIFY_TIME: Column<AttrMapping, Long>
        get() = _MODIFY_TIME
    val FOOD_ID: Column.Fk<AttrMapping, Long, Food>
        get() = _FOOD_ID
    val ATTRIBUTE_ID: Column.Fk<AttrMapping, Long, FoodAttribute>
        get() = _ATTRIBUTE_ID
}
