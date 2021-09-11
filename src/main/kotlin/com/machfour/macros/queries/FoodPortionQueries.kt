package com.machfour.macros.queries

import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.schema.FoodPortionTable
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.generator.ColumnMax.Companion.max
import com.machfour.macros.sql.generator.OrderByDirection
import java.sql.SQLException

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
internal fun recentFoodIds(db: SqlDatabase, howMany: Int): List<Long> {
    // NOTE this can't actually give the create time.
    // Need to SELECT MAX(create_time) for that (not just ORDER BY)
    val query = selectTwoColumns(
        db = db,
        table = FoodPortion.table,
        select1 = FoodPortionTable.FOOD_ID,
        select2 = FoodPortionTable.CREATE_TIME
    ) {
        orderBy(FoodPortionTable.CREATE_TIME.max(), OrderByDirection.DESCENDING)
        groupBy(FoodPortionTable.FOOD_ID)
        limit(howMany)
        //distinct()
    }
    return query.mapNotNull { it.first }
}