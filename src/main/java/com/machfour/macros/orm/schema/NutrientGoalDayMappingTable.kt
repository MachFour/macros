package com.machfour.macros.orm.schema

import com.machfour.macros.entities.NutrientGoal
import com.machfour.macros.entities.NutrientGoalDayMapping
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types
import com.machfour.macros.util.DateStamp

private const val TABLE_NAME = "NutrientGoalDayMapping"
// holds the following columns in the order initialised in the static block
// iteration order of COLUMNS is the order in which columns are defined below
private val COLUMNS = ArrayList<Column<NutrientGoalDayMapping, *>>()

private val _ID = idColumnBuildFor(COLUMNS)
private val _CREATE_TIME = createTimeColumnBuildFor(COLUMNS)
private val _MODIFY_TIME = modifyTimeColumnBuildFor(COLUMNS)
private val _DAY =
    builder("day", Types.DATESTAMP).notNull().buildFor(COLUMNS)
private val _GOAL_ID =
    builder("goal_id", Types.ID).notNull().notEditable().unique()
        .buildFkFor(NutrientGoalTable, NutrientGoalTable.ID, COLUMNS)

object NutrientGoalDayMappingTable: TableImpl<NutrientGoalDayMapping>(TABLE_NAME, NutrientGoalDayMapping.factory, COLUMNS) {
    val ID: Column<NutrientGoalDayMapping, Long>
        get() = _ID
    val CREATE_TIME: Column<NutrientGoalDayMapping, Long>
        get() = _CREATE_TIME
    val MODIFY_TIME: Column<NutrientGoalDayMapping, Long>
        get() = _MODIFY_TIME
    val GOAL_ID: Column.Fk<NutrientGoalDayMapping, Long, NutrientGoal>
        get() = _GOAL_ID
    val DAY: Column<NutrientGoalDayMapping, DateStamp>
        get() = _DAY
}
