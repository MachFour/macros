package com.machfour.macros.schema

import com.machfour.datestamp.DateStamp
import com.machfour.datestamp.currentDateStamp
import com.machfour.datestamp.currentEpochSeconds
import com.machfour.macros.entities.Meal
import com.machfour.macros.entities.NutrientGoal
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types

private const val tableName = "Meal"

// iteration order of columns is the order in which columns are defined below
private val columns = ArrayList<Column<Meal, out Any>>()

private val id = idColumnBuildFor(columns)
private val createTime = createTimeColumnBuildFor(columns)
private val modifyTime = modifyTimeColumnBuildFor(columns)
private val notes = notesColumnBuildAndAdd(columns)

private val name =
    builder("name", Types.TEXT).notNull().buildFor(columns)
private val day =
    builder("day", Types.DATESTAMP).notNull().default { currentDateStamp() }.buildFor(columns)
private val startTime =
    builder("start_time", Types.TIMESTAMP).notNull().default { currentEpochSeconds() }.buildFor(columns)
private val duration =
    builder("duration", Types.INTEGER).notNull().defaultsTo(0).buildFor(columns)
private val goalId =
    builder("goal_id", Types.ID).defaultsTo(null).notEditable().unique()
        .buildFkFor(NutrientGoalTable, NutrientGoalTable.ID, columns)

object MealTable: TableImpl<Meal>(tableName, Factories.meal, columns) {
    val ID: Column<Meal, Long>
        get() = id
    val CREATE_TIME: Column<Meal, Long>
        get() = createTime
    val MODIFY_TIME: Column<Meal, Long>
        get() = modifyTime
    val NAME: Column<Meal, String>
        get() = com.machfour.macros.schema.name
    val DAY: Column<Meal, DateStamp>
        get() = day
    val START_TIME: Column<Meal, Long>
        get() = startTime
    val DURATION: Column<Meal, Int>
        get() = duration
    val NOTES: Column<Meal, String>
        get() = notes
    val GOAL_ID: Column.Fk<Meal, Long, NutrientGoal>
        get() = goalId

}