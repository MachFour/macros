package com.machfour.macros.schema

import com.machfour.macros.core.EntityId
import com.machfour.macros.entities.*
import com.machfour.macros.entities.Unit
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types
import com.machfour.macros.sql.entities.Factories

private const val tableName = "FoodPortion"

// iteration order of columns is the order in which columns are defined below
private val columns = ArrayList<Column<FoodPortion, out Any>>()

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
    builder("meal_id", Types.ID).notEditable().notNull().buildFkFor(MealTable.ID, columns)
private val recipeMaxVersion =
    builder("recipe_max_version", Types.INTEGER).notEditable().notNull().default { 1 }.buildFor(columns)

// needs to come after FoodTable, ServingTable, MealTable
object FoodPortionTable: TableImpl<IFoodPortion<*>, FoodPortion>(tableName, Factories.foodPortion, columns) {
    val ID: Column<FoodPortion, EntityId>
        get() = id
    val CREATE_TIME: Column<FoodPortion, Long>
        get() = createTime
    val MODIFY_TIME: Column<FoodPortion, Long>
        get() = modifyTime
    val QUANTITY: Column<FoodPortion, Double>
        get() = quantity
    val QUANTITY_UNIT: Column.Fk<FoodPortion, String, Unit>
        get() = quantityUnit
    val FOOD_ID: Column.Fk<FoodPortion, EntityId, Food>
        get() = foodId
    val SERVING_ID: Column.Fk<FoodPortion, EntityId, Serving>
        get() = servingId
    val NOTES: Column<FoodPortion, String>
        get() = notes
    val NUTRIENT_MAX_VERSION: Column<FoodPortion, Int>
        get() = nutrientMaxVersion
    val MEAL_ID: Column.Fk<FoodPortion, EntityId, Meal>
        get() = mealId
    val RECIPE_MAX_VERSION: Column<FoodPortion, Int>
        get() = recipeMaxVersion
}

