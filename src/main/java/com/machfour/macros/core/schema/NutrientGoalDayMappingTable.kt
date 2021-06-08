package com.machfour.macros.core.schema

import com.machfour.macros.core.BaseTable
import com.machfour.macros.core.Column
import com.machfour.macros.core.datatype.Types
import com.machfour.macros.entities.*
import com.machfour.macros.util.DateStamp

class NutrientGoalDayMappingTable private constructor()
    : BaseTable<NutrientGoalDayMapping>(TABLE_NAME, NutrientGoalDayMapping.factory, COLUMNS) {

    companion object {
        private const val TABLE_NAME = "NutrientGoalDayMapping"

        // holds the following columns in the order initialised in the static block
        private val COLUMNS = ArrayList<Column<NutrientGoalDayMapping, *>>()

        val ID: Column<NutrientGoalDayMapping, Long>
        val CREATE_TIME: Column<NutrientGoalDayMapping, Long>
        val MODIFY_TIME: Column<NutrientGoalDayMapping, Long>
        val GOAL_ID: Column.Fk<NutrientGoalDayMapping, Long, NutrientGoal>
        val DAY: Column<NutrientGoalDayMapping, DateStamp>

        init {
            ID = SchemaHelpers.idColumnBuildAndAdd(COLUMNS)
            CREATE_TIME = SchemaHelpers.createTimeColumnBuildAndAdd(COLUMNS)
            MODIFY_TIME = SchemaHelpers.modifyTimeColumnBuildAndAdd(COLUMNS)
            DAY = SchemaHelpers.builder("day", Types.DATESTAMP).notNull().buildAndAdd(COLUMNS)
            GOAL_ID = SchemaHelpers.builder("goal_id", Types.ID).notNull().notEditable().unique()
                .buildAndAddFk(NutrientGoalTable.ID, NutrientGoalTable.instance, COLUMNS)
        }

        // this declaration has to be last (static initialisation order)
        val instance = NutrientGoalDayMappingTable()
    }
}
