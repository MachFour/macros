package com.machfour.macros.queries

import com.machfour.macros.core.EntityId
import com.machfour.macros.nutrients.IQuantity
import com.machfour.macros.nutrients.Quantity
import com.machfour.macros.schema.FoodPortionTable
import com.machfour.macros.schema.MealTable
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.generator.ColumnMax.Companion.max
import com.machfour.macros.sql.generator.OrderByDirection
import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.unitWithAbbrOrNull

//fun getServingId(dataSource: MacrosDataSource, fpId: EntityId) : EntityId? {
//    return CoreQueries.selectSingleColumn(dataSource, FoodPortionTable, FoodPortionTable.SERVING_ID) {
//        where(FoodPortionTable.ID, listOf(fpId))
//    }.getOrNull(0)
//}

//fun getQuantityUnit(dataSource: MacrosDataSource, fpId: EntityId) : Unit? {
//    val abbr = CoreQueries.selectSingleColumn(dataSource, FoodPortionTable, FoodPortionTable.QUANTITY_UNIT) {
//            where(FoodPortionTable.ID, listOf(fpId))
//        }.getOrNull(0)
//    return abbr?.let {
//        Units.fromAbbreviationNoThrow(abbr)
//    }
//}

//fun getFoodForFoodPortionId(ds: MacrosDataSource, fpId: EntityId): Food? {
//    val foodIds = CoreQueries.selectSingleColumn(ds, FoodPortionTable, FoodPortionTable.FOOD_ID) {
//        where(FoodPortionTable.ID, listOf(fpId))
//    }
//    check(foodIds.size <= 1) { "Returned multiple food ids for one foodportion id" }
//    return foodIds.getOrNull(0)?.let {
//        FoodQueries.getFoodById(ds, it)
//    }
//}

@Throws(SqlException::class)
fun recentFoodIds(db: SqlDatabase, howMany: Int, distinct: Boolean): List<EntityId> {
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

@Throws(SqlException::class)
fun recentMealIds(db: SqlDatabase, howMany: Int, nameFilter: Collection<String>): List<EntityId> {
    val query = selectTwoColumns(
        db = db,
        select1 = MealTable.ID,
        select2 = MealTable.MODIFY_TIME,
    ) {
        if (nameFilter.isNotEmpty()) {
            where(MealTable.NAME, nameFilter)
        }
        orderBy(MealTable.MODIFY_TIME, OrderByDirection.DESCENDING)
        limit(howMany)
    }
    return query.mapNotNull { it.first }
}

// returns pairs of <Quantity, ServingId>
@Throws(SqlException::class)
fun getCommonQuantities(db: SqlDatabase, foodId: EntityId, limit: Int = -1): List<Pair<IQuantity, EntityId?>> {
    val quantity = FoodPortionTable.QUANTITY
    val quantityUnit = FoodPortionTable.QUANTITY_UNIT
    val servingID = FoodPortionTable.SERVING_ID
    val query = selectThreeColumns(
        db = db,
        select1 = quantity,
        select2 = quantityUnit,
        select3 = servingID,
    ) {
        fromSuffix("COUNT (*) as count")
        where(FoodPortionTable.FOOD_ID, foodId)
        groupBy("$quantity, $quantityUnit, $servingID")
        orderBy("count DESC")
        if (limit >= 0) {
            limit(limit)
        }
    }
    return query.map { queryToQuantity(it.first, it.second) to it.third }
}

// XXX mad hack
private fun queryToQuantity(amount: Double?, abbr: String?): IQuantity {
    return Quantity(
        amount = amount ?: 0.0,
        unit = abbr?.let { unitWithAbbrOrNull(it) } ?: GRAMS
    )
}