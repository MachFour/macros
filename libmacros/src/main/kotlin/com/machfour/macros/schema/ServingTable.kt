package com.machfour.macros.schema

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.IServing
import com.machfour.macros.entities.Serving
import com.machfour.macros.entities.Unit
import com.machfour.macros.sql.entities.Factories
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.datatype.Types

private const val tableName = "Serving"

// iteration order of columns is the order in which columns are defined below
private val columns = ArrayList<Column<Serving, out Any>>()

private val id = idColumnBuildFor(columns)
private val createTime = createTimeColumnBuildFor(columns)
private val modifyTime = modifyTimeColumnBuildFor(columns)
private val name =
    builder("name", Types.TEXT).notNull().buildFor(columns)
private val notes =
    builder("notes", Types.TEXT).buildFor(columns)
private val quantity =
    builder("quantity", Types.REAL).notNull().buildFor(columns)
private val quantityUnit =
    builder("quantity_unit", Types.TEXT).notNull().buildFkFor(UnitTable.ABBREVIATION, columns)
private val isDefault =
    builder("is_default", Types.NULL_BOOLEAN).notNull().default { false }.buildFor(columns)
private val foodId =
    builder("food_id", Types.ID).notEditable().notNull().default { MacrosEntity.NO_ID }
        .buildFkFor(FoodTable.ID, columns)

object ServingTable : TableImpl<IServing, Serving>(tableName, Factories.serving, columns) {
    val ID: Column<Serving, Long>
        get() = id
    val CREATE_TIME: Column<Serving, Long>
        get() = createTime
    val MODIFY_TIME: Column<Serving, Long>
        get() = modifyTime
    val NAME: Column<Serving, String>
        get() = name
    val NOTES: Column<Serving, String>
        get() = notes
    val QUANTITY: Column<Serving, Double>
        get() = quantity
    val IS_DEFAULT: Column<Serving, Boolean>
        get() = isDefault
    val FOOD_ID: Column.Fk<Serving, Long, Food>
        get() = foodId
    val QUANTITY_UNIT: Column.Fk<Serving, String, Unit>
        get() = quantityUnit

}
