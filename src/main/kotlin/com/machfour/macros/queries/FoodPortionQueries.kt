package com.machfour.macros.queries

import com.machfour.macros.entities.Unit
import com.machfour.macros.schema.FoodPortionTable
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.generator.ColumnMax.Companion.max
import com.machfour.macros.sql.generator.OrderByDirection

//fun getServingId(dataSource: MacrosDataSource, fpId: Long) : Long? {
//    return CoreQueries.selectSingleColumn(dataSource, FoodPortionTable, FoodPortionTable.SERVING_ID) {
//        where(FoodPortionTable.ID, listOf(fpId))
//    }.getOrNull(0)
//}

//fun getQuantityUnit(dataSource: MacrosDataSource, fpId: Long) : Unit? {
//    val abbr = CoreQueries.selectSingleColumn(dataSource, FoodPortionTable, FoodPortionTable.QUANTITY_UNIT) {
//            where(FoodPortionTable.ID, listOf(fpId))
//        }.getOrNull(0)
//    return abbr?.let {
//        Units.fromAbbreviationNoThrow(abbr)
//    }
//}

//fun getFoodForFoodPortionId(ds: MacrosDataSource, fpId: Long): Food? {
//    val foodIds = CoreQueries.selectSingleColumn(ds, FoodPortionTable, FoodPortionTable.FOOD_ID) {
//        where(FoodPortionTable.ID, listOf(fpId))
//    }
//    assert(foodIds.size <= 1) { "Returned multiple food ids for one foodportion id" }
//    return foodIds.getOrNull(0)?.let {
//        FoodQueries.getFoodById(ds, it)
//    }
//}

@Throws(SqlException::class)
internal fun recentFoodIds(db: SqlDatabase, howMany: Int, distinct: Boolean): List<Long> {
    // NOTE this can't actually give the create time.
    // Need to SELECT MAX(create_time) for that (not just ORDER BY)
    val query = selectTwoColumns(
        db = db,
        select1 = FoodPortionTable.FOOD_ID,
        select2 = FoodPortionTable.CREATE_TIME
    ) {
        if (distinct) {
            orderBy(FoodPortionTable.CREATE_TIME.max(), OrderByDirection.DESCENDING)
            groupBy(FoodPortionTable.FOOD_ID)
            //distinct()
        } else {
            orderBy(FoodPortionTable.CREATE_TIME, OrderByDirection.DESCENDING)
        }
        limit(howMany)
    }
    return query.mapNotNull { it.first }
}

internal fun getCommonQuantities(db: SqlDatabase, foodId: Long): List<Pair<Double, Unit>> {
    val quantity = FoodPortionTable.QUANTITY
    val quantityUnit = FoodPortionTable.QUANTITY_UNIT
    val query = selectTwoColumns(
        db = db,
        select1 = FoodPortionTable.QUANTITY,
        select2 = FoodPortionTable.QUANTITY_UNIT,
    ) {
        fromSuffix("COUNT (*) as count")
        groupBy("$quantity, $quantityUnit")
        orderBy("count DESC")
        where(FoodPortionTable.FOOD_ID, foodId)
    }
    return query.filterIsInstance<Pair<Double, Unit>>()
}