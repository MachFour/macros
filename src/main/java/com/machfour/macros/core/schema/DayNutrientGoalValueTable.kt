package com.machfour.macros.core.schema

import com.machfour.macros.core.BaseTable
import com.machfour.macros.core.Column
import com.machfour.macros.core.datatype.Types
import com.machfour.macros.objects.DayNutrientGoalValue
import com.machfour.macros.objects.Nutrient
import com.machfour.macros.objects.Unit
import com.machfour.macros.objects.helpers.Factories
import com.machfour.macros.util.DateStamp
import com.machfour.macros.util.DateStamp.Companion.currentDate

class DayNutrientGoalValueTable private constructor() : BaseTable<DayNutrientGoalValue>(
    TABLE_NAME, Factories.dayNutrientGoalValue, COLUMNS
) {
    companion object {
        private const val TABLE_NAME = "DayNutrientGoalValue"

        // holds the following columns in the order initialised in the static block
        private val COLUMNS = ArrayList<Column<DayNutrientGoalValue, *>>()

        val ID: Column<DayNutrientGoalValue, Long>
        val CREATE_TIME: Column<DayNutrientGoalValue, Long>
        val MODIFY_TIME: Column<DayNutrientGoalValue, Long>
        val NUTRIENT_ID: Column.Fk<DayNutrientGoalValue, Long, Nutrient>
        val VALUE: Column<DayNutrientGoalValue, Double>
        val CONSTRAINT_SPEC: Column<DayNutrientGoalValue, Int>
        val UNIT_ID: Column.Fk<DayNutrientGoalValue, Long, Unit>

        val DAY: Column<DayNutrientGoalValue, DateStamp>

        init {
            ID = SchemaHelpers.idColumnBuildAndAdd(COLUMNS)
            CREATE_TIME = SchemaHelpers.createTimeColumnBuildAndAdd(COLUMNS)
            MODIFY_TIME = SchemaHelpers.modifyTimeColumnBuildAndAdd(COLUMNS)

            NUTRIENT_ID = SchemaHelpers.nutrientValueNutrientColumn(COLUMNS)
            UNIT_ID = SchemaHelpers.nutrientValueUnitColumn(COLUMNS)
            VALUE = SchemaHelpers.nutrientValueValueColumn(COLUMNS)
            CONSTRAINT_SPEC = SchemaHelpers.nutrientValueConstraintColumn(COLUMNS)

            DAY = SchemaHelpers.builder("day", Types.DATESTAMP)
                .notNull().notEditable().defaultsTo(currentDate()).inSecondaryKey()
                .buildAndAdd(COLUMNS)
        }

        // this part has to be last (static initialisation order)
        val instance = DayNutrientGoalValueTable()
    }

}
