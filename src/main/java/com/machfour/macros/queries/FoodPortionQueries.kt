package com.machfour.macros.queries

import com.machfour.macros.core.schema.FoodPortionTable
import com.machfour.macros.entities.*
import com.machfour.macros.sql.OrderByDirection
import com.machfour.macros.persistence.MacrosDatabase
import com.machfour.macros.sql.ColumnMax.Companion.max
import java.sql.SQLException

object FoodPortionQueries {
    //fun getServingId(dataSource: MacrosDataSource, fpId: Long) : Long? {
    //    return CoreQueries.selectSingleColumn(dataSource, FoodPortion.table, FoodPortionTable.SERVING_ID) {
    //        where(FoodPortionTable.ID, listOf(fpId))
    //    }.getOrNull(0)
    //}

    //fun getQuantityUnit(dataSource: MacrosDataSource, fpId: Long) : Unit? {
    //    val abbr = CoreQueries.selectSingleColumn(dataSource, FoodPortion.table, FoodPortionTable.QUANTITY_UNIT) {
    //            where(FoodPortionTable.ID, listOf(fpId))
    //        }.getOrNull(0)
    //    return abbr?.let {
    //        Units.fromAbbreviationNoThrow(abbr)
    //    }
    //}

    //fun getFoodForFoodPortionId(ds: MacrosDataSource, fpId: Long): Food? {
    //    val foodIds = CoreQueries.selectSingleColumn(ds, FoodPortion.table, FoodPortionTable.FOOD_ID) {
    //        where(FoodPortionTable.ID, listOf(fpId))
    //    }
    //    assert(foodIds.size <= 1) { "Returned multiple food ids for one foodportion id" }
    //    return foodIds.getOrNull(0)?.let {
    //        FoodQueries.getFoodById(ds, it)
    //    }
    //}

    @Throws(SQLException::class)
    fun recentFoodIds(ds: MacrosDatabase, howMany: Int) : List<Long> {
        val (foodId, createTime) = Pair(FoodPortionTable.FOOD_ID, FoodPortionTable.CREATE_TIME)
        // NOTE this can't actually give the create time - need to select MAX(create_time) for that
        val query = CoreQueries.selectTwoColumns(ds, FoodPortion.table, foodId, createTime) {
            orderBy(FoodPortionTable.CREATE_TIME.max(), OrderByDirection.DESCENDING)
            groupBy(FoodPortionTable.FOOD_ID)
            limit(howMany)
            //distinct()
        }
        return query.mapNotNull { it.first }
    }

}
