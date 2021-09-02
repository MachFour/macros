package com.machfour.macros.orm.schema

import com.machfour.macros.entities.NutrientGoal
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types

private const val TABLE_NAME = "NutrientGoal"

// iteration order of COLUMNS is the order in which columns are defined below
private val COLUMNS = ArrayList<Column<NutrientGoal, *>>()

private val _ID = idColumnBuildFor(COLUMNS)
private val _CREATE_TIME = createTimeColumnBuildFor(COLUMNS)
private val _MODIFY_TIME = modifyTimeColumnBuildFor(COLUMNS)
private val _NAME = builder("name", Types.TEXT).notNull().buildFor(COLUMNS)

object NutrientGoalTable: TableImpl<NutrientGoal>(TABLE_NAME, Factories.nutrientGoal, COLUMNS) {
    val ID: Column<NutrientGoal, Long>
        get() = _ID
    val CREATE_TIME: Column<NutrientGoal, Long>
        get() = _CREATE_TIME
    val MODIFY_TIME: Column<NutrientGoal, Long>
        get() = _MODIFY_TIME
    val NAME: Column<NutrientGoal, String>
        get() = _NAME
}
