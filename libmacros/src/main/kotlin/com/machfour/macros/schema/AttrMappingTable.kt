package com.machfour.macros.schema

import com.machfour.macros.sql.TableImpl
import com.machfour.macros.entities.AttrMapping
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodAttribute
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.datatype.Types


private const val tableName = "AttributeMapping"

// iteration order of columns is the order in which columns are defined below
private val columns = ArrayList<Column<AttrMapping, out Any>>()

private val id = idColumnBuildFor(columns)
private val create_time = createTimeColumnBuildFor(columns)
private val modify_time = modifyTimeColumnBuildFor(columns)
private val food_id =
    builder("food_id", Types.ID).notEditable().notNull()
        .buildFkFor(FoodTable.ID, columns)
private val attribute_id =
    builder("attribute_id", Types.ID).notEditable().notNull()
        .buildFkFor(FoodAttributeTable.ID, columns)

object AttrMappingTable : TableImpl<AttrMapping, AttrMapping>(tableName, AttrMapping.factory, columns) {
    val ID: Column<AttrMapping, Long>
        get() = id
    val CREATE_TIME: Column<AttrMapping, Long>
        get() = create_time
    val MODIFY_TIME: Column<AttrMapping, Long>
        get() = modify_time
    val FOOD_ID: Column.Fk<AttrMapping, Long, Food>
        get() = food_id
    val ATTRIBUTE_ID: Column.Fk<AttrMapping, Long, FoodAttribute>
        get() = attribute_id
}
