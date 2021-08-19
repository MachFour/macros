package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.datatype.MacrosType
import com.machfour.macros.sql.datatype.Types
import com.machfour.macros.persistence.DatabaseUtils

// TODO allow combining multiple SqlWhere statements, each one with a single column
//  make into sealed class with subclasses for each kind of where (multivalue, single value, LIKE, etc.)
class SqlWhereExpr<M, J> private constructor(
    private val whereClause: String = "",
    private val whereColumnExpr: ColumnExpr<M, J>? = null,
    private val whereColumnType: MacrosType<J>? = null,
    private val whereValues: Collection<J>? = null,
    val isIterated: Boolean = false,
) {

    internal companion object {
        fun <M> whereAny(): SqlWhereExpr<M, Nothing> {
            return SqlWhereExpr()
        }

        fun <M, J> where(whereColumnExpr: ColumnExpr<M, J>, whereValue: J): SqlWhereExpr<M, J> {
            return SqlWhereExpr(
                whereColumnExpr = whereColumnExpr,
                whereColumnType = whereColumnExpr.type,
                whereClause = DatabaseUtils.makeWhereString(whereColumnExpr, nValues = 1),
                whereValues = listOf(whereValue)
            )
        }

        // can specify iterated = true so that a separate query will be carried out for each where value
        // useful when the number of where values is large
        fun <M, J> where(whereColumnExpr: ColumnExpr<M, J>, whereValues: Collection<J>, iterated: Boolean): SqlWhereExpr<M, J> {
            val nValues = if (iterated) 1 else whereValues.size
            return SqlWhereExpr(
                whereColumnExpr = whereColumnExpr,
                whereColumnType = whereColumnExpr.type,
                whereValues = whereValues,
                whereClause = DatabaseUtils.makeWhereString(whereColumnExpr, nValues = nValues),
                isIterated = iterated
            )
        }

        fun <M, J> where(whereColumnExpr: ColumnExpr<M, J>, isNotNull: Boolean): SqlWhereExpr<M, J> {
            return SqlWhereExpr(
                whereColumnExpr = whereColumnExpr,
                whereColumnType = whereColumnExpr.type,
                whereClause = DatabaseUtils.makeWhereString(whereColumnExpr, isNotNull = isNotNull)
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