package com.machfour.macros.core.schema

import com.machfour.macros.core.BaseTable
import com.machfour.macros.core.Column
import com.machfour.macros.core.datatype.Types
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.entities.NutrientGoal

class NutrientGoalTable private constructor() : BaseTable<NutrientGoal>(TABLE_NAME, Factories.nutrientGoal, COLUMNS) {
    companion object {
        private const val TABLE_NAME = "NutrientGoal"

        // holds the following columns in the order initialised in the static block
        private val COLUMNS = ArrayList<Column<NutrientGoal, *>>()

        val ID: Column<NutrientGoal, Long>
        val CREATE_TIME: Column<NutrientGoal, Long>
        val MODIFY_TIME: Column<NutrientGoal, Long>
        val NAME: Column<NutrientGoal, String>

        init {
            ID = SchemaHelpers.idColumnBuildAndAdd(COLUMNS)
            CREATE_TIME = SchemaHelpers.createTimeColumnBuildAndAdd(COLUMNS)
            MODIFY_TIME = SchemaHelpers.modifyTimeColumnBuildAndAdd(COLUMNS)

            NAME = SchemaHelpers.builder("name", Types.TEXT).notNull().buildAndAdd(COLUMNS)
        }

        val instance = NutrientGoalTable()
    }
}
