package com.machfour.macros.core.schema

import com.machfour.macros.core.BaseTable
import com.machfour.macros.core.Column
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.datatype.Types
import com.machfour.macros.objects.Food
import com.machfour.macros.objects.FoodNutrientValue
import com.machfour.macros.objects.Nutrient
import com.machfour.macros.objects.Unit
import com.machfour.macros.objects.helpers.Factories

class FoodNutrientValueTable private constructor() : BaseTable<FoodNutrientValue>(TABLE_NAME, Factories.foodNutrientValue, COLUMNS) {
    companion object {
        private const val TABLE_NAME = "FoodNutrientValue"

        // holds the following columns in the order initialised in the static block
        private val COLUMNS = ArrayList<Column<FoodNutrientValue, *>>()

        val ID: Column<FoodNutrientValue, Long>
        val CREATE_TIME: Column<FoodNutrientValue, Long>
        val MODIFY_TIME: Column<FoodNutrientValue, Long>

        val NUTRIENT_ID: Column.Fk<FoodNutrientValue, Long, Nutrient>
        val VALUE: Column<FoodNutrientValue, Double>
        val CONSTRAINT_SPEC: Column<FoodNutrientValue, Int>
        val UNIT_ID: Column.Fk<FoodNutrientValue, Long, Unit>

        val FOOD_ID: Column.Fk<FoodNutrientValue, Long, Food>
        val VERSION: Column<FoodNutrientValue, Int>

        init {
            ID = SchemaHelpers.idColumnBuildAndAdd(COLUMNS)
            CREATE_TIME = SchemaHelpers.createTimeColumnBuildAndAdd(COLUMNS)
            MODIFY_TIME = SchemaHelpers.modifyTimeColumnBuildAndAdd(COLUMNS)

            NUTRIENT_ID = SchemaHelpers.nutrientValueNutrientColumn(COLUMNS)
            UNIT_ID = SchemaHelpers.nutrientValueUnitColumn(COLUMNS)
            VALUE = SchemaHelpers.nutrientValueValueColumn(COLUMNS)
            CONSTRAINT_SPEC = SchemaHelpers.nutrientValueConstraintColumn(COLUMNS)

            FOOD_ID = SchemaHelpers.builder("food_id", Types.ID)
                .notNull().notEditable().defaultsTo(MacrosEntity.NO_ID).inSecondaryKey()
                .buildAndAddFk(FoodTable.ID, FoodTable.instance, COLUMNS)
            VERSION = SchemaHelpers.builder("version", Types.INTEGER).notNull().defaultsTo(1).buildAndAdd(COLUMNS)
        }

        // this part has to be last (static initialisation order)
        val instance = FoodNutrientValueTable()
    }

}
