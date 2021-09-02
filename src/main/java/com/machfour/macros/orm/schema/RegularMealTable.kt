package com.machfour.macros.orm.schema

import com.machfour.macros.entities.Meal
import com.machfour.macros.entities.RegularMeal
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types

private const val TABLE_NAME = "RegularMeal"

// iteration order of COLUMNS is the order in which columns are defined below
private val COLUMNS = ArrayList<Column<RegularMeal, *>>()

private val _ID = idColumnBuildFor(COLUMNS)
private val _CREATE_TIME = createTimeColumnBuildFor(COLUMNS)
private val _MODIFY_TIME = modifyTimeColumnBuildFor(COLUMNS)
private val _NAME =
    builder("name", Types.TEXT).notNull().buildFor(COLUMNS)
private val _MEAL_ID =
    builder("meal_id", Types.ID).notEditable().notNull().inSecondaryKey().unique()
        .buildFkFor(MealTable, MealTable.ID, COLUMNS)

object RegularMealTable: TableImpl<RegularMeal>(TABLE_NAME, RegularMeal.factory, COLUMNS) {
    val ID: Column<RegularMeal, Long>
        get() = _ID
    val CREATE_TIME: Column<RegularMeal, Long>
        get() = _CREATE_TIME
    val MODIFY_TIME: Column<RegularMeal, Long>
        get() = _MODIFY_TIME
    val NAME: Column<RegularMeal, String>
        get() = _NAME
    val MEAL_ID: Column.Fk<RegularMeal, Long, Meal>
        get() = _MEAL_ID
}
