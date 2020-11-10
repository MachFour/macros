package com.machfour.macros.queries

import com.machfour.macros.core.*
import com.machfour.macros.objects.Food
import com.machfour.macros.objects.FoodQuantity
import com.machfour.macros.objects.Unit
import com.machfour.macros.objects.inbuilt.Units
import com.machfour.macros.storage.MacrosDataSource
import java.sql.SQLException

object FoodQuantityQueries {
    fun getServingId(dataSource: MacrosDataSource, fpId: Long) : Long? {
        return Queries.selectColumn(
            dataSource,
            FoodQuantity.table,
            Schema.FoodQuantityTable.SERVING_ID,
            Schema.FoodQuantityTable.ID,
            fpId
        ).getOrNull(0)
    }

    fun getQuantityUnit(dataSource: MacrosDataSource, fpId: Long) : Unit? {
        val abbr = Queries.selectColumn(
            dataSource,
            FoodQuantity.table,
            Schema.FoodQuantityTable.QUANTITY_UNIT,
            Schema.FoodQuantityTable.ID,
            fpId
        ).getOrNull(0)
        return abbr?.let {
            Units.fromAbbreviationNoThrow(abbr)
        }
    }

    fun getFoodForFoodPortionId(ds: MacrosDataSource, fpId: Long): Food? {
        val foodIds = Queries.selectColumn(ds, FoodQuantity.table,
            Schema.FoodQuantityTable.FOOD_ID,
            Schema.FoodQuantityTable.ID,
            fpId)
        assert(foodIds.size <= 1) { "Returned multiple food ids for one foodportion id" }
        return foodIds.getOrNull(0)?.let {
            FoodQueries.getFoodById(ds, it)
        }
    }


    @Throws(SQLException::class)
    fun deleteAllIngredients(ds: MacrosDataSource) : Int {
        return ds.deleteByNullStatus(FoodQuantity.table, Schema.FoodQuantityTable.PARENT_FOOD_ID, true)
    }

    @Throws(SQLException::class)
    fun deleteAllFoodPortions(ds: MacrosDataSource) : Int {
        return ds.deleteByNullStatus(FoodQuantity.table, Schema.FoodQuantityTable.MEAL_ID, true)
    }

}
