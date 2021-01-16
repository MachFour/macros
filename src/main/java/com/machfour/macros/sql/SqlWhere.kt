package com.machfour.macros.sql

import com.machfour.macros.core.Column
import com.machfour.macros.storage.DatabaseUtils

internal class SqlWhereExpr private constructor(
    private val whereClause: String = "",
    private val whereValues: Collection<*>? = null,
    private val whereLikeValues: Collection<String>? = null,
) {

    companion object {
        fun whereAny(): SqlWhereExpr {
            return SqlWhereExpr()
        }

        fun <M, J> where(whereColumn: Column<M, J>, whereValue: J): SqlWhereExpr {
            return SqlWhereExpr(
                whereClause = DatabaseUtils.makeWhereString(whereColumn, nValues = 1),
                whereValues = listOf(whereValue)
            )
        }

        fun <M, J> where(whereColumn: Column<M, J>, whereValues: Collection<J>): SqlWhereExpr {
            return SqlWhereExpr(
                whereClause = DatabaseUtils.makeWhereString(whereColumn, nValues = whereValues.size),
                whereValues = whereValues
            )
        }

        fun <M, J> where(whereColumn: Column<M, J>, isNotNull: Boolean): SqlWhereExpr {
            return SqlWhereExpr(
                whereClause = DatabaseUtils.makeWhereString(whereColumn, isNotNull = isNotNull)
            )
        }

        fun where(whereString: String): SqlWhereExpr {
            return SqlWhereExpr(
                whereClause = whereString
            )
        }

        // " WHERE (likeColumn[0] LIKE ?) <Conjuction> (likeColumn[1] LIKE ?) <Conjunction> ..."
        fun <M> whereLike(
            likeColumns: Collection<Column<M, String>>,
            whereLikeValues: Collection<String>,
            conjunction: Conjuction = Conjuction.OR
        ): SqlWhereExpr {
            return SqlWhereExpr(
                whereClause = DatabaseUtils.makeWhereLikeString(likeColumns, conjunction),
                whereLikeValues = whereLikeValues
            )
        }
    }

    fun toTemplate(): String {
        return whereClause
    }
    fun getBindObjects(): Collection<*>? {
        return whereValues ?: whereLikeValues
    }
    val numArgs: Int
        get() = whereValues?.size ?: whereLikeValues?.size ?: 0
}