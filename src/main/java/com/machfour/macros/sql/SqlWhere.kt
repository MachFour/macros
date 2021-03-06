package com.machfour.macros.sql

import com.machfour.macros.core.Column
import com.machfour.macros.core.datatype.MacrosType
import com.machfour.macros.core.datatype.Types
import com.machfour.macros.persistence.DatabaseUtils

// TODO allow combining multiple SqlWhere statements, each one with a single column
//  make into sealed class with subclasses for each kind of where (multivalue, single value, LIKE, etc.)
class SqlWhereExpr<M, J> private constructor(
    private val whereClause: String = "",
    private val whereColumn: Column<M, J>? = null,
    private val whereColumnType: MacrosType<J>? = null,
    private val whereValues: Collection<J>? = null,
) {

    companion object {
        fun <M> whereAny(): SqlWhereExpr<M, Nothing> {
            return SqlWhereExpr()
        }

        fun <M, J> where(whereColumn: Column<M, J>, whereValue: J): SqlWhereExpr<M, J> {
            return SqlWhereExpr(
                whereColumn = whereColumn,
                whereColumnType = whereColumn.type,
                whereClause = DatabaseUtils.makeWhereString(whereColumn, nValues = 1),
                whereValues = listOf(whereValue)
            )
        }

        fun <M, J> where(whereColumn: Column<M, J>, whereValues: Collection<J>): SqlWhereExpr<M, J> {
            return SqlWhereExpr(
                whereColumn = whereColumn,
                whereColumnType = whereColumn.type,
                whereClause = DatabaseUtils.makeWhereString(whereColumn, nValues = whereValues.size),
                whereValues = whereValues
            )
        }

        fun <M, J> where(whereColumn: Column<M, J>, isNotNull: Boolean): SqlWhereExpr<M, J> {
            return SqlWhereExpr(
                whereColumn = whereColumn,
                whereColumnType = whereColumn.type,
                whereClause = DatabaseUtils.makeWhereString(whereColumn, isNotNull = isNotNull)
            )
        }

        fun <M> where(whereString: String): SqlWhereExpr<M, *> {
            return SqlWhereExpr<M, Nothing>(
                whereClause = whereString
            )
        }

        // TODO clean up this interface (see note at top)
        // " WHERE (likeColumn[0] LIKE ?) <Conjuction> (likeColumn[1] LIKE ?) <Conjunction> ..."
        fun <M> whereLike(
            likeColumns: Collection<Column<M, String>>,
            whereLikeValues: Collection<String>,
            conjunction: Conjuction = Conjuction.OR
        ): SqlWhereExpr<M, String> {
            return SqlWhereExpr(
                whereColumnType = Types.TEXT,
                whereClause = DatabaseUtils.makeWhereLikeString(likeColumns, conjunction),
                whereValues = whereLikeValues
            )
        }
    }

    fun toSql(): String {
        return whereClause
    }

    fun getBindObjects(): Collection<J> {
        return whereValues ?: emptyList<Nothing>()
    }
    val numArgs: Int
        get() = whereValues?.size ?: 0

    val hasBindObjects: Boolean
        get() = numArgs > 0

    // for Android
    val bindArgumentType: MacrosType<J>?
        get() = whereColumnType
}