package com.machfour.macros.queries

import com.machfour.macros.core.schema.FoodPortionTable
import com.machfour.macros.entities.*
import com.machfour.macros.entities.Unit
import com.machfour.macros.entities.inbuilt.Units
import com.machfour.macros.sql.OrderByDirection
import com.machfour.macros.persistence.MacrosDataSource
import com.machfour.macros.sql.ColumnMax.Companion.max
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

    /* SELECT DISTINCT ... ORDER BY doesn't work because when food IDs are repeated, it needs
     * to take the one corresponding to the most recent create time.
     * This is what aggregate functions are for: https://stackoverflow.com/q/5391564/9901599
     * Ideally:
     *     SELECT (DISTINCT) food_id, MAX(create_time) FROM FoodPortion
     *     GROUP BY food_id
     *     ORDER BY MAX(create_time) DESC
     * (nb DISTINCT is implied by the aggregation)
     *
     * For now we can manually do the order by operation
     */
    fun recentFoodIds(ds: MacrosDataSource, howMany: Int) : List<Long> {
        val (foodId, createTime) = Pair(FoodPortionTable.FOOD_ID, FoodPortionTable.CREATE_TIME)
        // we don't technically need to select MAX(create_time) but it would be nice
        val query = Queries.selectTwoColumns(ds, FoodPortion.table, foodId, createTime) {
            //distinct()
            orderBy(FoodPortionTable.CREATE_TIME.max(), OrderByDirection.DESCENDING)
            groupBy(FoodPortionTable.FOOD_ID)
            limit(howMany)
        }
        return query.mapNotNull { it.first }
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
