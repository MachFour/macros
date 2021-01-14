package com.machfour.macros.core.schema

import com.machfour.macros.core.BaseTable
import com.machfour.macros.core.Column
import com.machfour.macros.core.datatype.Types
import com.machfour.macros.objects.Food
import com.machfour.macros.objects.Ingredient
import com.machfour.macros.objects.Serving
import com.machfour.macros.objects.Unit
import com.machfour.macros.objects.helpers.Factories

// needs to come after FoodTable, ServingTable, MealTable
class IngredientTable private constructor() : BaseTable<Ingredient>(TABLE_NAME, Factories.ingredient, COLUMNS) {
    companion object {
        private const val TABLE_NAME = "Ingredient"

        // holds the following columns in the order initialised in the static block
        private val COLUMNS = ArrayList<Column<Ingredient, *>>()

        val ID: Column<Ingredient, Long>
        val CREATE_TIME: Column<Ingredient, Long>
        val MODIFY_TIME: Column<Ingredient, Long>

        val QUANTITY: Column<Ingredient, Double>
        val QUANTITY_UNIT: Column.Fk<Ingredient, String, Unit>
        val FOOD_ID: Column.Fk<Ingredient, Long, Food>
        val SERVING_ID: Column.Fk<Ingredient, Long, Serving>
        val NOTES: Column<Ingredient, String>
        val NUTRIENT_MAX_VERSION: Column<Ingredient, Int>

        val PARENT_FOOD_ID: Column.Fk<Ingredient, Long, Food>
        val RECIPE_VERSION: Column<Ingredient, Int>

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

            RECIPE_VERSION = SchemaHelpers.builder("recipe_version", Types.INTEGER).notEditable().notNull().defaultsTo(1)
                .buildAndAdd(COLUMNS)
            PARENT_FOOD_ID = SchemaHelpers.builder("parent_food_id", Types.ID).notEditable().notNull()
                .buildAndAddFk(FoodTable.ID, FoodTable.instance, COLUMNS)
        }

        val instance = IngredientTable()
    }
}
