package com.machfour.macros.schema

import com.machfour.macros.sql.TableImpl
import com.machfour.macros.entities.Meal
import com.machfour.macros.entities.RegularMeal
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.datatype.Types

private const val tableName = "RegularMeal"

// iteration order of columns is the order in which columns are defined below
private val columns = ArrayList<Column<RegularMeal, out Any>>()

private val id = idColumnBuildFor(columns)
private val createTime = createTimeColumnBuildFor(columns)
private val modifyTime = modifyTimeColumnBuildFor(columns)
private val name =
    builder("name", Types.TEXT).notNull().buildFor(columns)
private val mealId =
    builder("meal_id", Types.ID).notEditable().notNull().unique()
        .buildFkFor(MealTable.ID, columns)

object RegularMealTable: TableImpl<RegularMeal, RegularMeal>(tableName, RegularMeal.factory, columns) {
    val ID: Column<RegularMeal, Long>
        get() = id
    val CREATE_TIME: Column<RegularMeal, Long>
        get() = createTime
    val MODIFY_TIME: Column<RegularMeal, Long>
        get() = modifyTime
    val NAME: Column<RegularMeal, String>
        get() = com.machfour.macros.schema.name
    val MEAL_ID: Column.Fk<RegularMeal, Long, Meal>
        get() = mealId
}
