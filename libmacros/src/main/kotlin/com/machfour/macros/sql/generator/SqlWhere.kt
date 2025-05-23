package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.datatype.SqlType
import com.machfour.macros.sql.datatype.Types
import com.machfour.macros.sql.makeSqlWhereLikeString
import com.machfour.macros.sql.makeSqlWhereNullString
import com.machfour.macros.sql.makeSqlWhereString

// TODO allow combining multiple SqlWhere statements, each one with a single column
//  make into sealed class with subclasses for each kind of where (multivalue, single value, LIKE, etc.)
class SqlWhereExpr<M, J: Any> private constructor(
    private val whereClause: String = "",
    private val whereColumnExpr: ColumnExpr<M, J>? = null,
    private val whereColumnType: SqlType<J>? = null,
    private val whereValues: Collection<J>? = null,
    val isIterated: Boolean = false,
    private var suffix: String = ""
) {

    internal companion object {
        fun <M> whereAny(): SqlWhereExpr<M, Nothing> {
            return SqlWhereExpr()
        }

        fun <M, J: Any> where(whereColumnExpr: ColumnExpr<M, J>, whereValue: J): SqlWhereExpr<M, J> {
            return SqlWhereExpr(
                whereColumnExpr = whereColumnExpr,
                whereColumnType = whereColumnExpr.type,
                whereClause = makeSqlWhereString(whereColumnExpr, nValues = 1),
                whereValues = listOf(whereValue)
            )
        }

        // can specify iterated = true so that a separate query will be carried out for each where value
        // useful when the number of where values is large
        fun <M, J: Any> where(whereColumnExpr: ColumnExpr<M, J>, whereValues: Collection<J>, iterated: Boolean): SqlWhereExpr<M, J> {
            require(whereValues.isNotEmpty()) { "whereValues cannot be empty" }
            val nValues = if (iterated) 1 else whereValues.size
            return SqlWhereExpr(
                whereColumnExpr = whereColumnExpr,
                whereColumnType = whereColumnExpr.type,
                whereValues = whereValues,
                whereClause = makeSqlWhereString(whereColumnExpr, nValues),
                isIterated = iterated
            )
        }

        fun <M, J: Any> whereNull(whereColumnExpr: ColumnExpr<M, J>, negate: Boolean): SqlWhereExpr<M, J> {
            return SqlWhereExpr(
                whereColumnExpr = whereColumnExpr,
                whereColumnType = whereColumnExpr.type,
                whereClause = makeSqlWhereNullString(whereColumnExpr, negate)
            )
        }

        fun <M> where(whereString: String): SqlWhereExpr<M, *> {
            return SqlWhereExpr<M, Nothing>(
                whereClause = whereString
            )
        }

        // TODO clean up this interface (see note at top)
        // " WHERE (likeColumn[0] LIKE ?) <Conjunction> (likeColumn[1] LIKE ?) <Conjunction> ..."
        fun <M> whereLike(
            likeColumns: Collection<Column<M, String>>,
            whereLikeValues: Collection<String>,
            conjunction: Conjunction = Conjunction.OR
        ): SqlWhereExpr<M, String> {
            return SqlWhereExpr(
                whereColumnType = Types.TEXT,
                whereClause = makeSqlWhereLikeString(likeColumns, conjunction),
                whereValues = whereLikeValues
            )
        }
    }

    fun alsoWhere(conjunction: Conjunction, sql: String) {
        check(whereClause.isNotBlank()) { "Where clause is blank; use a primary condition instead"}
        suffix = " $conjunction ($sql)"
    }

    fun toSql(): String {
        return whereClause + suffix
    }

    fun getRawBindObjects(): List<Any> {
        return whereValues?.map { bindArgumentType?.toRaw(it) ?: it } ?: emptyList()
    }

    // For Android
    fun getRawBindStrings(): List<String> {
        return whereValues?.map { bindArgumentType?.toSqlString(it) ?: it.toString() } ?: emptyList()
    }

    val numArgs: Int
        get() = whereValues?.size ?: 0

    val hasBindObjects: Boolean
        get() = numArgs > 0

    // for Android
    val bindArgumentType: SqlType<J>?
        get() = whereColumnType

}