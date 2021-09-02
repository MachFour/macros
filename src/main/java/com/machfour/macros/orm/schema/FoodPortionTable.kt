package com.machfour.macros.orm.schema

import com.machfour.macros.entities.*
import com.machfour.macros.entities.Unit
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types

private const val tableName = "FoodPortion"

// iteration order of columns is the order in which columns are defined below
private val columns = ArrayList<Column<FoodPortion, *>>()

private val id = idColumnBuildFor(columns)
private val createTime = createTimeColumnBuildFor(columns)
private val modifyTime = modifyTimeColumnBuildFor(columns)
private val notes = notesColumnBuildAndAdd(columns)

private val quantity = foodQuantityQuantityCol(columns)
private val quantityUnit = foodQuantityQuantityUnitCol(columns)
private val foodId = foodQuantityFoodIdCol(columns)
private val servingId = foodQuantityServingIdCol(columns)
private val nutrientMaxVersion = foodQuantityNutrientMaxVersionCol(columns)

private val mealId =
    builder("meal_id", Types.ID).notEditable().notNull().buildFkFor(MealTable, MealTable.ID, columns)
private val recipeMaxVersion =
    builder("recipe_max_version", Types.INTEGER).notEditable().notNull().defaultsTo(1).buildFor(columns)

// needs to come after FoodTable, ServingTable, MealTable
object FoodPortionTable: TableImpl<FoodPortion>(tableName, Factories.foodPortion, columns) {
    val ID: Column<FoodPortion, Long>
        get() = id
    val CREATE_TIME: Column<FoodPortion, Long>
        get() = createTime
    val MODIFY_TIME: Column<FoodPortion, Long>
        get() = modifyTime
    val QUANTITY: Column<FoodPortion, Double>
        get() = quantity
    val QUANTITY_UNIT: Column.Fk<FoodPortion, String, Unit>
        get() = quantityUnit
    val FOOD_ID: Column.Fk<FoodPortion, Long, Food>
        get() = foodId
    val SERVING_ID: Column.Fk<FoodPortion, Long, Serving>
        get() = servingId
    val NOTES: Column<FoodPortion, String>
        get() = notes
    val NUTRIENT_MAX_VERSION: Column<FoodPortion, Int>
        get() = nutrientMaxVersion
    val MEAL_ID: Column.Fk<FoodPortion, Long, Meal>
        get() = mealId
    val RECIPE_MAX_VERSION: Column<FoodPortion, Int>
        get() = recipeMaxVersion
}

