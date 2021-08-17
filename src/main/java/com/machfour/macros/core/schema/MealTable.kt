package com.machfour.macros.core.schema

import com.machfour.macros.orm.BaseTable
import com.machfour.macros.orm.Column
import com.machfour.macros.orm.datatype.Types
import com.machfour.macros.entities.Meal
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.entities.NutrientGoal
import com.machfour.macros.util.DateStamp
import com.machfour.macros.util.DateStamp.Companion.currentDate
import java.time.Instant

class MealTable private constructor() : BaseTable<Meal>(TABLE_NAME, Factories.meal, COLUMNS) {
    companion object {
        private const val TABLE_NAME = "Meal"

        // holds the following columns in the order initialised in the static block
        private val COLUMNS = ArrayList<Column<Meal, *>>()

        val ID: Column<Meal, Long>
        val CREATE_TIME: Column<Meal, Long>
        val MODIFY_TIME: Column<Meal, Long>
        val NAME: Column<Meal, String>
        val DAY: Column<Meal, DateStamp>
        val START_TIME: Column<Meal, Long>
        val DURATION: Column<Meal, Int>
        val NOTES: Column<Meal, String>
        val GOAL_ID: Column.Fk<Meal, Long, NutrientGoal>

        init {
            ID = SchemaHelpers.idColumnBuildAndAdd(COLUMNS)
            CREATE_TIME = SchemaHelpers.createTimeColumnBuildAndAdd(COLUMNS)
            MODIFY_TIME = SchemaHelpers.modifyTimeColumnBuildAndAdd(COLUMNS)
            NOTES = SchemaHelpers.notesColumnBuildAndAdd(COLUMNS)

            NAME = SchemaHelpers.builder("name", Types.TEXT).notNull().buildAndAdd(COLUMNS)
            DAY = SchemaHelpers.builder("day", Types.DATESTAMP).notNull().default { currentDate() }.buildAndAdd(COLUMNS)
            START_TIME = SchemaHelpers.builder("start_time", Types.TIMESTAMP).notNull().default{ Instant.now().epochSecond }.buildAndAdd(COLUMNS)
            DURATION = SchemaHelpers.builder("duration", Types.INTEGER).notNull().defaultsTo(0).buildAndAdd(COLUMNS)
            GOAL_ID = SchemaHelpers.builder("goal_id", Types.ID).defaultsTo(null).notEditable().unique()
                .buildAndAddFk(NutrientGoalTable.ID, NutrientGoalTable.instance, COLUMNS)
        }

        val instance = MealTable()
    }
}
