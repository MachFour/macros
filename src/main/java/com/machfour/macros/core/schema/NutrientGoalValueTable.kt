package com.machfour.macros.core.schema

import com.machfour.macros.core.BaseTable
import com.machfour.macros.core.Column
import com.machfour.macros.core.datatype.Types
import com.machfour.macros.entities.*
import com.machfour.macros.entities.Unit
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.entities.NutrientGoal

class NutrientGoalValueTable private constructor() : BaseTable<NutrientGoalValue>(
    TABLE_NAME, Factories.nutrientGoalValue, COLUMNS
) {
    companion object {
        private const val TABLE_NAME = "NutrientGoalValue"

        // holds the following columns in the order initialised in the static block
        private val COLUMNS = ArrayList<Column<NutrientGoalValue, *>>()

        val ID: Column<NutrientGoalValue, Long>
        val CREATE_TIME: Column<NutrientGoalValue, Long>
        val MODIFY_TIME: Column<NutrientGoalValue, Long>
        val NUTRIENT_ID: Column.Fk<NutrientGoalValue, Long, Nutrient>
        val VALUE: Column<NutrientGoalValue, Double>
        val CONSTRAINT_SPEC: Column<NutrientGoalValue, Int>
        val UNIT_ID: Column.Fk<NutrientGoalValue, Long, Unit>

        val GOAL_ID: Column.Fk<NutrientGoalValue, Long, NutrientGoal>

        init {
            ID = SchemaHelpers.idColumnBuildAndAdd(COLUMNS)
            CREATE_TIME = SchemaHelpers.createTimeColumnBuildAndAdd(COLUMNS)
            MODIFY_TIME = SchemaHelpers.modifyTimeColumnBuildAndAdd(COLUMNS)

            NUTRIENT_ID = SchemaHelpers.nutrientValueNutrientColumn(COLUMNS)
            UNIT_ID = SchemaHelpers.nutrientValueUnitColumn(COLUMNS)
            VALUE = SchemaHelpers.nutrientValueValueColumn(COLUMNS)
            CONSTRAINT_SPEC = SchemaHelpers.nutrientValueConstraintColumn(COLUMNS)

            GOAL_ID = SchemaHelpers.builder("goal_id", Types.ID).notEditable().notNull()
                .buildAndAddFk(NutrientGoalTable.ID, NutrientGoalTable.instance, COLUMNS)
        }

        // this part has to be last (static initialisation order)
        val instance = NutrientGoalValueTable()
    }

}
