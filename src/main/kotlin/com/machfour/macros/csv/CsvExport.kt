package com.machfour.macros.csv

import com.machfour.macros.core.FoodType
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.Serving
import com.machfour.macros.nutrients.AllNutrients
import com.machfour.macros.nutrients.ENERGY
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.queries.getFoodsByType
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.ServingTable
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.datatype.TypeCastException
import com.machfour.macros.sql.datatype.Types
import com.machfour.macros.units.StandardNutrientUnits
import java.io.IOException
import java.io.Reader
import java.io.Writer
import java.sql.SQLException


@Throws(IOException::class, SQLException::class)
private fun getFoodsForExport(db: SqlDatabase) = getFoodsByType(db, FoodType.PRIMARY)

@Throws(IOException::class, SQLException::class)
private fun getRecipesForExport(db: SqlDatabase) = getFoodsByType(db, FoodType.COMPOSITE)

private val foodColumnsToExclude = setOf(
    FoodTable.ID,
    FoodTable.CREATE_TIME,
    FoodTable.MODIFY_TIME,
    FoodTable.SEARCH_RELEVANCE
)

private val servingColumnsToExclude = setOf(
    ServingTable.ID,
    ServingTable.CREATE_TIME,
    ServingTable.MODIFY_TIME,
    ServingTable.FOOD_ID,
)

private fun prepareFoodDataForExport(f: Food): Map<String, String> {
    return buildMap {
        FoodTable.columns
            .filterNot { foodColumnsToExclude.contains(it) }
            // null data gets mapped to empty string
            .forEach { col -> put(col.sqlName, f.data.getAsRawString(col)) }
    }
}


private fun prepareNutrientDataForExport(nd: FoodNutrientData): Map<String, String> {
    return buildMap {
        put("quantity_unit", nd.qtyUnitAbbr)
        put("energy_unit", nd.getUnit(ENERGY, StandardNutrientUnits).abbr)
        AllNutrients.forEach {
            put(it.csvName, Types.REAL.toRawString(nd[it]?.value))
        }
        // TODO constraint spec
    }
}

private fun <J> prepareServingDataForExport(s: Serving, foodKeyCol: Column<Food, J>): Map<String, String> {
    return buildMap {
        put(foodKeyCol.sqlName, s.food.data.getAsRawString(foodKeyCol))
        ServingTable.columns
            .filterNot { servingColumnsToExclude.contains(it) }
            // null data gets mapped to empty string
            .forEach { col -> put(col.sqlName, s.data.getAsRawString(col)) }
    }
}


@Throws(IOException::class, SQLException::class)
fun <J> exportFoodData(
    db: SqlDatabase,
    foodCsv: Writer,
    foodKeyCol: Column<Food, J>,
) {
    require(foodKeyCol.isUnique)

    val dataForExport = getFoodsForExport(db).values.map {
        val foodData = prepareFoodDataForExport(it)
        val nutrientData = prepareNutrientDataForExport(it.nutrientData)
        foodData + nutrientData
    }

    val header = dataForExport.firstOrNull()?.keys?.toTypedArray() ?: return

    with(getMapWriter(foodCsv)) {
        writeHeader(*header)
        dataForExport.forEach { write(it, *header) }
        flush()
    }
}

@Throws(IOException::class, SQLException::class, TypeCastException::class)
fun <J> exportServings(
    db: SqlDatabase,
    servingCsv: Writer,
    foodKeyCol: Column<Food, J>,
    ignoreFoodIds: Set<Long>,
) {

    // list of (food, serving) for each serving of a primary food
    val servingsForExport = getFoodsForExport(db)
        .filterNot { ignoreFoodIds.contains(it.key) }
        .flatMap { (_, food) -> food.servings }

    val dataForExport = servingsForExport
        .map { prepareServingDataForExport(it, foodKeyCol) }

    val header = dataForExport.firstOrNull()?.keys?.toTypedArray() ?: return

    with(getMapWriter(servingCsv)) {
        writeHeader(*header)
        dataForExport.forEach { write(it, *header) }
        flush()
    }
}

@Throws(IOException::class, SQLException::class)
fun exportRecipes(
    db: SqlDatabase,
    recipeCsv: Reader,
    ingredientCsv: Reader
) {

    val recipesForExport = getRecipesForExport(db)
    // TODO
}