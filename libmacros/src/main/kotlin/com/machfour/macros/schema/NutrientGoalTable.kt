package com.machfour.macros.schema

import com.machfour.macros.core.EntityId
import com.machfour.macros.entities.NutrientGoal
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types
import com.machfour.macros.sql.entities.Factories

private const val tableName = "NutrientGoal"

// iteration order of columns is the order in which columns are defined below
private val columns = ArrayList<Column<NutrientGoal, out Any>>()

private val id = idColumnBuildFor(columns)
private val createTime = createTimeColumnBuildFor(columns)
private val modifyTime = modifyTimeColumnBuildFor(columns)
private val name = builder("name", Types.TEXT).notNull().buildFor(columns)

object NutrientGoalTable: TableImpl<NutrientGoal, NutrientGoal>(tableName, Factories.nutrientGoal, columns) {
    val ID: Column<NutrientGoal, EntityId>
        get() = id
    val CREATE_TIME: Column<NutrientGoal, Long>
        get() = createTime
    val MODIFY_TIME: Column<NutrientGoal, Long>
        get() = modifyTime
    val NAME: Column<NutrientGoal, String>
        get() = com.machfour.macros.schema.name
}
