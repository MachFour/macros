package com.machfour.macros.core.schema

import com.machfour.macros.core.BaseTable
import com.machfour.macros.core.Column
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.datatype.Types
import com.machfour.macros.objects.*
import com.machfour.macros.objects.Unit
import com.machfour.macros.objects.helpers.Factories

class MealNutrientGoalValueTable private constructor() : BaseTable<MealNutrientGoalValue>(
    TABLE_NAME, Factories.mealNutrientGoalValue, COLUMNS
) {
    companion object {
        private const val TABLE_NAME = "MealNutrientGoalValue"

        // holds the following columns in the order initialised in the static block
        private val COLUMNS = ArrayList<Column<MealNutrientGoalValue, *>>()

        val ID: Column<MealNutrientGoalValue, Long>
        val CREATE_TIME: Column<MealNutrientGoalValue, Long>
        val MODIFY_TIME: Column<MealNutrientGoalValue, Long>
        val NUTRIENT_ID: Column.Fk<MealNutrientGoalValue, Long, Nutrient>
        val VALUE: Column<MealNutrientGoalValue, Double>
        val CONSTRAINT_SPEC: Column<MealNutrientGoalValue, Int>
        val UNIT_ID: Column.Fk<MealNutrientGoalValue, Long, Unit>

        val MEAL_ID: Column.Fk<MealNutrientGoalValue, Long, Meal>

        init {
            ID = SchemaHelpers.idColumnBuildAndAdd(COLUMNS)
            CREATE_TIME = SchemaHelpers.createTimeColumnBuildAndAdd(COLUMNS)
            MODIFY_TIME = SchemaHelpers.modifyTimeColumnBuildAndAdd(COLUMNS)

            NUTRIENT_ID = SchemaHelpers.nutrientValueNutrientColumn(COLUMNS)
            UNIT_ID = SchemaHelpers.nutrientValueUnitColumn(COLUMNS)
            VALUE = SchemaHelpers.nutrientValueValueColumn(COLUMNS)
            CONSTRAINT_SPEC = SchemaHelpers.nutrientValueConstraintColumn(COLUMNS)

            MEAL_ID = SchemaHelpers.builder("meal_id", Types.ID)
                .notNull().notEditable().defaultsTo(MacrosEntity.NO_ID).inSecondaryKey()
                .buildAndAddFk(MealTable.ID, MealTable.instance, COLUMNS)
        }

        // this part has to be last (static initialisation order)
        val instance = MealNutrientGoalValueTable()
    }

}
