package com.machfour.macros.orm.schema

import com.machfour.macros.entities.Meal
import com.machfour.macros.entities.NutrientGoal
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types
import com.machfour.macros.util.DateStamp
import com.machfour.macros.util.DateStamp.Companion.currentDate
import java.time.Instant

private const val TABLE_NAME = "Meal"

// iteration order of COLUMNS is the order in which columns are defined below
private val COLUMNS = ArrayList<Column<Meal, *>>()

private val _ID = idColumnBuildFor(COLUMNS)
private val _CREATE_TIME = createTimeColumnBuildFor(COLUMNS)
private val _MODIFY_TIME = modifyTimeColumnBuildFor(COLUMNS)
private val _NOTES = notesColumnBuildAndAdd(COLUMNS)

private val _NAME =
    builder("name", Types.TEXT).notNull().buildFor(COLUMNS)
private val _DAY =
    builder("day", Types.DATESTAMP).notNull().default { currentDate() }.buildFor(COLUMNS)
private val _START_TIME =
    builder("start_time", Types.TIMESTAMP).notNull().default{ Instant.now().epochSecond }.buildFor(COLUMNS)
private val _DURATION =
    builder("duration", Types.INTEGER).notNull().defaultsTo(0).buildFor(COLUMNS)
private val _GOAL_ID =
    builder("goal_id", Types.ID).defaultsTo(null).notEditable().unique()
        .buildFkFor(NutrientGoalTable, NutrientGoalTable.ID, COLUMNS)

object MealTable: TableImpl<Meal>(TABLE_NAME, Factories.meal, COLUMNS) {
    val ID: Column<Meal, Long>
        get() = _ID
    val CREATE_TIME: Column<Meal, Long>
        get() = _CREATE_TIME
    val MODIFY_TIME: Column<Meal, Long>
        get() = _MODIFY_TIME
    val NAME: Column<Meal, String>
        get() = _NAME
    val DAY: Column<Meal, DateStamp>
        get() = _DAY
    val START_TIME: Column<Meal, Long>
        get() = _START_TIME
    val DURATION: Column<Meal, Int>
        get() = _DURATION
    val NOTES: Column<Meal, String>
        get() = _NOTES
    val GOAL_ID: Column.Fk<Meal, Long, NutrientGoal>
        get() = _GOAL_ID

}