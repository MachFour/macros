package com.machfour.macros.schema

import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.NutrientGoal
import com.machfour.macros.entities.NutrientGoalValue
import com.machfour.macros.entities.Unit
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types

private const val tableName = "NutrientGoalValue"

// iteration order of columns is the order in which columns are defined below
private val columns = ArrayList<Column<NutrientGoalValue, out Any>>()

private val id = idColumnBuildFor(columns)
private val createTime = createTimeColumnBuildFor(columns)
private val modifyTime = modifyTimeColumnBuildFor(columns)

private val nutrientId = nutrientValueNutrientColumn(columns)
private val unitId = nutrientValueUnitColumn(columns)
private val value = nutrientValueValueColumn(columns)
private val constraintSpec = nutrientValueConstraintColumn(columns)

private val goalId =
    builder("goal_id", Types.ID).notEditable().notNull()
        .buildFkFor(NutrientGoalTable, NutrientGoalTable.ID, columns)

object NutrientGoalValueTable: TableImpl<NutrientGoalValue>(tableName, Factories.nutrientGoalValue, columns) {
    val ID: Column<NutrientGoalValue, Long>
        get() = id
    val CREATE_TIME: Column<NutrientGoalValue, Long>
        get() = createTime
    val MODIFY_TIME: Column<NutrientGoalValue, Long>
        get() = modifyTime
    val NUTRIENT_ID: Column.Fk<NutrientGoalValue, Long, Nutrient>
        get() = nutrientId
    val VALUE: Column<NutrientGoalValue, Double>
        get() = value
    val CONSTRAINT_SPEC: Column<NutrientGoalValue, Int>
        get() = constraintSpec
    val UNIT_ID: Column.Fk<NutrientGoalValue, Long, Unit>
        get() = unitId
    val GOAL_ID: Column.Fk<NutrientGoalValue, Long, NutrientGoal>
        get() = goalId
}
