package com.machfour.macros.orm.schema

import com.machfour.macros.entities.*
import com.machfour.macros.entities.Unit
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types

private const val TABLE_NAME = "FoodPortion"

// iteration order of COLUMNS is the order in which columns are defined below
private val COLUMNS = ArrayList<Column<FoodPortion, *>>()

private val _ID = idColumnBuildFor(COLUMNS)
private val _CREATE_TIME = createTimeColumnBuildFor(COLUMNS)
private val _MODIFY_TIME = modifyTimeColumnBuildFor(COLUMNS)
private val _NOTES = notesColumnBuildAndAdd(COLUMNS)

private val _QUANTITY = foodQuantityQuantityCol(COLUMNS)
private val _QUANTITY_UNIT = foodQuantityQuantityUnitCol(COLUMNS)
private val _FOOD_ID = foodQuantityFoodIdCol(COLUMNS)
private val _SERVING_ID = foodQuantityServingIdCol(COLUMNS)
private val _NUTRIENT_MAX_VERSION = foodQuantityNutrientMaxVersionCol(COLUMNS)

private val _MEAL_ID =
    builder("meal_id", Types.ID).notEditable().notNull().buildFkFor(MealTable, MealTable.ID, COLUMNS)
private val _RECIPE_MAX_VERSION =
    builder("recipe_max_version", Types.INTEGER).notEditable().notNull().defaultsTo(1).buildFor(COLUMNS)

// needs to come after FoodTable, ServingTable, MealTable
object FoodPortionTable: TableImpl<FoodPortion>(TABLE_NAME, Factories.foodPortion, COLUMNS) {
    val ID: Column<FoodPortion, Long>
        get() = _ID
    val CREATE_TIME: Column<FoodPortion, Long>
        get() = _CREATE_TIME
    val MODIFY_TIME: Column<FoodPortion, Long>
        get() = _MODIFY_TIME
    val QUANTITY: Column<FoodPortion, Double>
        get() = _QUANTITY
    val QUANTITY_UNIT: Column.Fk<FoodPortion, String, Unit>
        get() = _QUANTITY_UNIT
    val FOOD_ID: Column.Fk<FoodPortion, Long, Food>
        get() = _FOOD_ID
    val SERVING_ID: Column.Fk<FoodPortion, Long, Serving>
        get() = _SERVING_ID
    val NOTES: Column<FoodPortion, String>
        get() = _NOTES
    val NUTRIENT_MAX_VERSION: Column<FoodPortion, Int>
        get() = _NUTRIENT_MAX_VERSION

    val MEAL_ID: Column.Fk<FoodPortion, Long, Meal>
        get() = _MEAL_ID
    val RECIPE_MAX_VERSION: Column<FoodPortion, Int>
        get() = _RECIPE_MAX_VERSION
}

