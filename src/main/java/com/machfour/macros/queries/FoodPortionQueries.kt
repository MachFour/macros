package com.machfour.macros.queries

import com.machfour.macros.core.*
import com.machfour.macros.objects.*
import com.machfour.macros.objects.Unit
import com.machfour.macros.objects.inbuilt.Units
import com.machfour.macros.storage.MacrosDataSource
import java.sql.SQLException

object FoodPortionQueries {
    fun getServingId(dataSource: MacrosDataSource, fpId: Long) : Long? {
        return Queries.selectColumn(
            dataSource,
            FoodPortion.table,
            Schema.FoodPortionTable.SERVING_ID,
            Schema.FoodPortionTable.ID,
            fpId
        ).getOrNull(0)
    }

    fun getQuantityUnit(dataSource: MacrosDataSource, fpId: Long) : Unit? {
        val abbr = Queries.selectColumn(
            dataSource,
            FoodPortion.table,
            Schema.FoodPortionTable.QUANTITY_UNIT,
            Schema.FoodPortionTable.ID,
            fpId
        ).getOrNull(0)
        return abbr?.let {
            Units.fromAbbreviationNoThrow(abbr)
        }
    }

    fun getFoodForFoodPortionId(ds: MacrosDataSource, fpId: Long): Food? {
        val foodIds = Queries.selectColumn(ds, FoodPortion.table,
            Schema.FoodPortionTable.FOOD_ID,
            Schema.FoodPortionTable.ID,
            fpId)
        assert(foodIds.size <= 1) { "Returned multiple food ids for one foodportion id" }
        return foodIds.getOrNull(0)?.let {
            FoodQueries.getFoodById(ds, it)
        }
    }


    @Throws(SQLException::class)
    fun deleteAllIngredients(ds: MacrosDataSource) {
        ds.clearTable(Ingredient.table)
    }

    @Throws(SQLException::class)
    fun deleteAllFoodPortions(ds: MacrosDataSource) {
        ds.clearTable(FoodPortion.table)
    }

}
