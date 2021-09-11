package com.machfour.macros.schema

import com.machfour.macros.entities.Food
import com.machfour.macros.entities.Ingredient
import com.machfour.macros.entities.Serving
import com.machfour.macros.entities.Unit
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.TableImpl
import com.machfour.macros.sql.datatype.Types

private const val tableName = "Ingredient"

// iteration order of columns is the order in which columns are defined below
private val columns = ArrayList<Column<Ingredient, out Any>>()

private val id = idColumnBuildFor(columns)
private val createTime = createTimeColumnBuildFor(columns)
private val modifyTime = modifyTimeColumnBuildFor(columns)
private val notes = notesColumnBuildAndAdd(columns)

private val quantity = foodQuantityQuantityCol(columns)
private val quantityUnit = foodQuantityQuantityUnitCol(columns)
private val foodId = foodQuantityFoodIdCol(columns)
private val servingId = foodQuantityServingIdCol(columns)
private val nutrientMaxVersion = foodQuantityNutrientMaxVersionCol(columns)

private val recipeVersion =
    builder("recipe_version", Types.INTEGER).notEditable().notNull().defaultsTo(1).buildFor(columns)
private val parentFoodId =
    builder("parent_food_id", Types.ID).notEditable().notNull().buildFkFor(FoodTable, FoodTable.ID, columns)


// needs to come after FoodTable, ServingTable, MealTable
object IngredientTable: TableImpl<Ingredient>(tableName, Factories.ingredient, columns) {
    val ID: Column<Ingredient, Long>
        get() = id
    val CREATE_TIME: Column<Ingredient, Long>
        get() = createTime
    val MODIFY_TIME: Column<Ingredient, Long>
        get() = modifyTime

    val QUANTITY: Column<Ingredient, Double>
        get() = quantity
    val QUANTITY_UNIT: Column.Fk<Ingredient, String, Unit>
        get() = quantityUnit
    val FOOD_ID: Column.Fk<Ingredient, Long, Food>
        get() = foodId
    val SERVING_ID: Column.Fk<Ingredient, Long, Serving>
        get() = servingId
    val NOTES: Column<Ingredient, String>
        get() = notes
    val NUTRIENT_MAX_VERSION: Column<Ingredient, Int>
        get() = nutrientMaxVersion

    val PARENT_FOOD_ID: Column.Fk<Ingredient, Long, Food>
        get() = parentFoodId
    val RECIPE_VERSION: Column<Ingredient, Int>
        get() = recipeVersion
}
