package com.machfour.macros.orm.schema

import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.NutrientGoal
import com.machfour.macros.entities.NutrientGoalValue
import com.machfour.macros.entities.Unit
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types

private const val TABLE_NAME = "NutrientGoalValue"

// iteration order of COLUMNS is the order in which columns are defined below
private val COLUMNS = ArrayList<Column<NutrientGoalValue, *>>()

private val _ID = idColumnBuildFor(COLUMNS)
private val _CREATE_TIME = createTimeColumnBuildFor(COLUMNS)
private val _MODIFY_TIME = modifyTimeColumnBuildFor(COLUMNS)

private val _NUTRIENT_ID = nutrientValueNutrientColumn(COLUMNS)
private val _UNIT_ID = nutrientValueUnitColumn(COLUMNS)
private val _VALUE = nutrientValueValueColumn(COLUMNS)
private val _CONSTRAINT_SPEC = nutrientValueConstraintColumn(COLUMNS)

private val _GOAL_ID =
    builder("goal_id", Types.ID).notEditable().notNull()
        .buildFkFor(NutrientGoalTable, NutrientGoalTable.ID, COLUMNS)

object NutrientGoalValueTable: TableImpl<NutrientGoalValue>(TABLE_NAME, Factories.nutrientGoalValue, COLUMNS) {
    val ID: Column<NutrientGoalValue, Long>
        get() = _ID
    val CREATE_TIME: Column<NutrientGoalValue, Long>
        get() = _CREATE_TIME
    val MODIFY_TIME: Column<NutrientGoalValue, Long>
        get() = _MODIFY_TIME
    val NUTRIENT_ID: Column.Fk<NutrientGoalValue, Long, Nutrient>
        get() = _NUTRIENT_ID
    val VALUE: Column<NutrientGoalValue, Double>
        get() = _VALUE
    val CONSTRAINT_SPEC: Column<NutrientGoalValue, Int>
        get() = _CONSTRAINT_SPEC
    val UNIT_ID: Column.Fk<NutrientGoalValue, Long, Unit>
        get() = _UNIT_ID
    val GOAL_ID: Column.Fk<NutrientGoalValue, Long, NutrientGoal>
        get() = _GOAL_ID
}
