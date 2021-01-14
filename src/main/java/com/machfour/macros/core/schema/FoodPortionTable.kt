package com.machfour.macros.core.schema

import com.machfour.macros.core.BaseTable
import com.machfour.macros.core.Column
import com.machfour.macros.core.datatype.Types
import com.machfour.macros.objects.*
import com.machfour.macros.objects.Unit
import com.machfour.macros.objects.helpers.Factories

// needs to come after FoodTable, ServingTable, MealTable
class FoodPortionTable private constructor() : BaseTable<FoodPortion>(TABLE_NAME, Factories.foodPortion, COLUMNS) {
    companion object {
        private const val TABLE_NAME = "FoodPortion"

        // holds the following columns in the order initialised in the static block
        private val COLUMNS = ArrayList<Column<FoodPortion, *>>()

        val ID: Column<FoodPortion, Long>
        val CREATE_TIME: Column<FoodPortion, Long>
        val MODIFY_TIME: Column<FoodPortion, Long>
        val QUANTITY: Column<FoodPortion, Double>
        val QUANTITY_UNIT: Column.Fk<FoodPortion, String, Unit>
        val FOOD_ID: Column.Fk<FoodPortion, Long, Food>
        val SERVING_ID: Column.Fk<FoodPortion, Long, Serving>
        val NOTES: Column<FoodPortion, String>
        val NUTRIENT_MAX_VERSION: Column<FoodPortion, Int>

        val MEAL_ID: Column.Fk<FoodPortion, Long, Meal>
        val RECIPE_MAX_VERSION: Column<FoodPortion, Int>

        init {
            ID = SchemaHelpers.idColumnBuildAndAdd(COLUMNS)
            CREATE_TIME = SchemaHelpers.createTimeColumnBuildAndAdd(COLUMNS)
            MODIFY_TIME = SchemaHelpers.modifyTimeColumnBuildAndAdd(COLUMNS)
            NOTES = SchemaHelpers.notesColumnBuildAndAdd(COLUMNS)

            QUANTITY = SchemaHelpers.foodQuantityQuantityCol(COLUMNS)
            QUANTITY_UNIT = SchemaHelpers.foodQuantityQuantityUnitCol(COLUMNS)
            FOOD_ID = SchemaHelpers.foodQuantityFoodIdCol(COLUMNS)
            SERVING_ID = SchemaHelpers.foodQuantityServingIdCol(COLUMNS)
            NUTRIENT_MAX_VERSION = SchemaHelpers.foodQuantityNutrientMaxVersionCol(COLUMNS)

            MEAL_ID = SchemaHelpers.builder("meal_id", Types.ID).notEditable().notNull()
                .buildAndAddFk(MealTable.ID, MealTable.instance, COLUMNS)
            RECIPE_MAX_VERSION = SchemaHelpers.builder("recipe_max_version", Types.INTEGER).notEditable().notNull().defaultsTo(1)
                .buildAndAdd(COLUMNS)
        }

        val instance = FoodPortionTable()
    }
}

