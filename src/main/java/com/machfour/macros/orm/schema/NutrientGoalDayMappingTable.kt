package com.machfour.macros.orm.schema

import com.machfour.macros.entities.NutrientGoal
import com.machfour.macros.entities.NutrientGoalDayMapping
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types
import com.machfour.macros.util.DateStamp

private const val tableName = "NutrientGoalDayMapping"
// holds the following columns in the order initialised in the static block
// iteration order of columns is the order in which columns are defined below
private val columns = ArrayList<Column<NutrientGoalDayMapping, *>>()

private val id = idColumnBuildFor(columns)
private val createTime = createTimeColumnBuildFor(columns)
private val modifyTime = modifyTimeColumnBuildFor(columns)
private val day =
    builder("day", Types.DATESTAMP).notNull().buildFor(columns)
private val goalId =
    builder("goal_id", Types.ID).notNull().notEditable().unique()
        .buildFkFor(NutrientGoalTable, NutrientGoalTable.ID, columns)

object NutrientGoalDayMappingTable: TableImpl<NutrientGoalDayMapping>(tableName, NutrientGoalDayMapping.factory, columns) {
    val ID: Column<NutrientGoalDayMapping, Long>
        get() = id
    val CREATE_TIME: Column<NutrientGoalDayMapping, Long>
        get() = createTime
    val MODIFY_TIME: Column<NutrientGoalDayMapping, Long>
        get() = modifyTime
    val GOAL_ID: Column.Fk<NutrientGoalDayMapping, Long, NutrientGoal>
        get() = goalId
    val DAY: Column<NutrientGoalDayMapping, DateStamp>
        get() = day
}
