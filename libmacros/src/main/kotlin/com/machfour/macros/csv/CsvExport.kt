package com.machfour.macros.csv

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.FoodType
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.Serving
import com.machfour.macros.nutrients.AllNutrients
import com.machfour.macros.nutrients.BasicNutrientData
import com.machfour.macros.nutrients.ENERGY
import com.machfour.macros.queries.getFoodsByType
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.ServingTable
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.datatype.TypeCastException
import com.machfour.macros.sql.datatype.Types
import com.machfour.macros.units.StandardNutrientUnits


@Throws(SqlException::class)
private fun getFoodsForExport(db: SqlDatabase) = getFoodsByType(db, FoodType.PRIMARY)

@Throws(SqlException::class)
private fun getRecipesForExport(db: SqlDatabase) = getFoodsByType(db, FoodType.COMPOSITE)

private val foodColumnsForExport by lazy {
    val foodColumnsToExclude = setOf(
        FoodTable.ID,
        FoodTable.CREATE_TIME,
        FoodTable.MODIFY_TIME,
        FoodTable.SEARCH_RELEVANCE
    )

    FoodTable.columns.filterNot { it in foodColumnsToExclude }
}

private val servingColumnsForExport by lazy {
    val servingColumnsToExclude = setOf(
        ServingTable.ID,
        ServingTable.CREATE_TIME,
        ServingTable.MODIFY_TIME,
        ServingTable.FOOD_ID,
    )

    ServingTable.columns.filterNot { it in servingColumnsToExclude }
}

private val nutrientDataColumnNames = listOf("quantity_unit", "energy_unit") + AllNutrients.map { it.name }


private fun prepareFoodDataForExport(f: Food): Map<String, String> {
    return buildMap {
        // null data gets mapped to empty string
        foodColumnsForExport.forEach { col -> put(col.sqlName, f.data.getAsRawString(col)) }
    }
}


private fun prepareNutrientDataForExport(nd: BasicNutrientData<*>): Map<String, String> {
    return buildMap {
        put("quantity_unit", nd.perQuantity.unit.abbr)
        put("energy_unit", nd.getUnit(ENERGY, StandardNutrientUnits).abbr)
        AllNutrients.forEach {
            put(it.name, Types.REAL.toRawString(nd.amountOf(it)))
        }
        // TODO constraint spec
    }
}

private fun <J: Any> prepareServingDataForExport(s: Serving, foodKeyCol: Column<Food, J>, foodIdToKey: Map<EntityId, J>): Map<String, String> {
    return buildMap {
        put(foodKeyCol.sqlName, foodKeyCol.type.toRawString(foodIdToKey.getValue(s.foodId)))
        // null data gets mapped to empty string
        servingColumnsForExport.forEach { col -> put(col.sqlName, s.data.getAsRawString(col)) }
    }
}


@Throws(SqlException::class)
fun <J: Any> exportFoodData(
    db: SqlDatabase,
    foodKeyCol: Column<Food, J>,
): String {
    require(foodKeyCol.isUnique)

    val dataForExport = getFoodsForExport(db).values.map {
        val foodData = prepareFoodDataForExport(it)
        val nutrientData = prepareNutrientDataForExport(it.nutrientData)
        foodData + nutrientData
    }

    val header = foodColumnsForExport.map { it.sqlName } + nutrientDataColumnNames

    val rows = buildList {
        add(header)
        dataForExport.forEach { add(header.map { column -> it[column] ?: "" }) }
    }

    return getCsvWriter().write(rows)
}

@Throws(SqlException::class, TypeCastException::class)
fun <J: Any> exportServings(
    db: SqlDatabase,
    foodKeyCol: Column<Food, J>,
    ignoreFoodIds: Set<EntityId>,
): String {
    require(foodKeyCol.isUnique && !foodKeyCol.isNullable) { "food key col ($foodKeyCol) must be unique and not nullable" }

    // list of (food, serving) for each serving of a primary food
    val foodsForExport = getFoodsForExport(db)
        .filterNot { ignoreFoodIds.contains(it.key) }

    val foodIdToKey = foodsForExport.mapValues { it.value.getData(foodKeyCol)!! }

    val header = servingColumnsForExport.map { it.sqlName }

    val rows = buildList {
        add(header)
        for (f in foodsForExport.values) {
            for (s in f.servings) {
                val data = prepareServingDataForExport(s, foodKeyCol, foodIdToKey)
                add(header.map { data[it] ?: "" })
            }
        }
    }

    return getCsvWriter().write(rows)
}

@Throws(SqlException::class)
fun exportRecipes(
    db: SqlDatabase,
): Pair<String, String> {
    val recipesForExport = getRecipesForExport(db)
    TODO()
}