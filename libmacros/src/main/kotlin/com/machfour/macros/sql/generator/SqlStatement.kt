package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.Table

const val DEFAULT_ITERATE_THRESHOLD = 200

sealed interface SqlStatement<M> {
    val table: Table<*, M>
    val mode: SqlQueryMode

    val whereExpression: SqlWhereExpr<M, *> // XXX needed for Android binding but should remove

    // TODO situation when there are a lot of arguments (e.g. in a WHERE clause),
    //  currently iteration is done over all args one by one.
    //  However, there are cases where some arguments which must be in every query
    //  e.g. WHERE col1 = X AND col2 IN (Y1, Y2, Y3, Y4, ...)
    val shouldIterateBindArguments: Boolean

    val hasBindArguments: Boolean

    fun getBindArguments(): Collection<*>

    fun toSql(): String


    interface Builder<M> {
        fun <J: Any> where(whereColumnExpr: ColumnExpr<M, J>, whereValue: J)

        // iterate threshold exists because if number of the parameters in a query gets too large,
        // Android SQlite will not execute the query
        fun <J: Any> where(whereColumnExpr: ColumnExpr<M, J>, whereValues: Collection<J>, iterateThreshold: Int = DEFAULT_ITERATE_THRESHOLD)

        fun whereNull(whereColumnExpr: ColumnExpr<M, *>, negate: Boolean)

        fun where(whereString: String)

        fun whereLike(
            likeColumns: Collection<Column<M, String>>,
            whereLikeValues: Collection<String>,
            conjunction: Conjuction = Conjuction.OR
        )

        // TODO XXX these are mad hacks
        fun andWhere(expr: String)
        fun orWhere(expr: String)

    }
}