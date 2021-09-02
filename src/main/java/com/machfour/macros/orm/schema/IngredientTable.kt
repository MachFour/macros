package com.machfour.macros.orm.schema

import com.machfour.macros.entities.Food
import com.machfour.macros.entities.Ingredient
import com.machfour.macros.entities.Serving
import com.machfour.macros.entities.Unit
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types

private const val TABLE_NAME = "Ingredient"

// iteration order of COLUMNS is the order in which columns are defined below
private val COLUMNS = ArrayList<Column<Ingredient, *>>()

private val _ID = idColumnBuildFor(COLUMNS)
private val _CREATE_TIME = createTimeColumnBuildFor(COLUMNS)
private val _MODIFY_TIME = modifyTimeColumnBuildFor(COLUMNS)
private val _NOTES = notesColumnBuildAndAdd(COLUMNS)

private val _QUANTITY = foodQuantityQuantityCol(COLUMNS)
private val _QUANTITY_UNIT = foodQuantityQuantityUnitCol(COLUMNS)
private val _FOOD_ID = foodQuantityFoodIdCol(COLUMNS)
private val _SERVING_ID = foodQuantityServingIdCol(COLUMNS)
private val _NUTRIENT_MAX_VERSION = foodQuantityNutrientMaxVersionCol(COLUMNS)

private val _RECIPE_VERSION =
    builder("recipe_version", Types.INTEGER).notEditable().notNull().defaultsTo(1).buildFor(COLUMNS)
private val _PARENT_FOOD_ID =
    builder("parent_food_id", Types.ID).notEditable().notNull().buildFkFor(FoodTable, FoodTable.ID, COLUMNS)


// needs to come after FoodTable, ServingTable, MealTable
object IngredientTable: TableImpl<Ingredient>(TABLE_NAME, Factories.ingredient, COLUMNS) {
    val ID: Column<Ingredient, Long>
        get() = _ID
    val CREATE_TIME: Column<Ingredient, Long>
        get() = _CREATE_TIME
    val MODIFY_TIME: Column<Ingredient, Long>
        get() = _MODIFY_TIME

    val QUANTITY: Column<Ingredient, Double>
        get() = _QUANTITY
    val QUANTITY_UNIT: Column.Fk<Ingredient, String, Unit>
        get() = _QUANTITY_UNIT
    val FOOD_ID: Column.Fk<Ingredient, Long, Food>
        get() = _FOOD_ID
    val SERVING_ID: Column.Fk<Ingredient, Long, Serving>
        get() = _SERVING_ID
    val NOTES: Column<Ingredient, String>
        get() = _NOTES
    val NUTRIENT_MAX_VERSION: Column<Ingredient, Int>
        get() = _NUTRIENT_MAX_VERSION

    val PARENT_FOOD_ID: Column.Fk<Ingredient, Long, Food>
        get() = _PARENT_FOOD_ID
    val RECIPE_VERSION: Column<Ingredient, Int>
        get() = _RECIPE_VERSION
}
