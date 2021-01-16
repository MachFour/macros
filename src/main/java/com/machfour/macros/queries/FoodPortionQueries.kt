package com.machfour.macros.queries

import com.machfour.macros.core.schema.FoodPortionTable
import com.machfour.macros.objects.*
import com.machfour.macros.objects.Unit
import com.machfour.macros.objects.inbuilt.Units
import com.machfour.macros.sql.OrderByDirection
import com.machfour.macros.persistence.MacrosDataSource
import java.sql.SQLException

object FoodPortionQueries {
    fun getServingId(dataSource: MacrosDataSource, fpId: Long) : Long? {
        return Queries.selectSingleColumn(dataSource, FoodPortion.table, FoodPortionTable.SERVING_ID) {
            where(FoodPortionTable.ID, listOf(fpId))
        }.getOrNull(0)
    }

    fun getQuantityUnit(dataSource: MacrosDataSource, fpId: Long) : Unit? {
        val abbr = Queries.selectSingleColumn(dataSource, FoodPortion.table, FoodPortionTable.QUANTITY_UNIT) {
                where(FoodPortionTable.ID, listOf(fpId))
            }.getOrNull(0)
        return abbr?.let {
            Units.fromAbbreviationNoThrow(abbr)
        }
    }

    fun getRecentlyEnteredFoodIds(ds: MacrosDataSource, howMany: Int) : List<Long> {
        return Queries.selectSingleColumn(ds, FoodPortion.table, FoodPortionTable.FOOD_ID) {
            orderBy(FoodPortionTable.CREATE_TIME, OrderByDirection.DESCENDING)
            limit(howMany)
        }.filterNotNull()
    }

    fun getFoodForFoodPortionId(ds: MacrosDataSource, fpId: Long): Food? {
        val foodIds = Queries.selectSingleColumn(ds, FoodPortion.table, FoodPortionTable.FOOD_ID) {
            where(FoodPortionTable.ID, listOf(fpId))
        }
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
