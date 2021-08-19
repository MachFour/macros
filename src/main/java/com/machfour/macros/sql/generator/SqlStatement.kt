package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.Table

sealed interface SqlStatement<M> {
    val table: Table<M>
    val mode: SqlQueryMode


    // TODO situation when there are a lot of arguments (e.g. in a WHERE clause),
    //  currently iteration is done over all args one by one.
    //  However, there are cases where some arguments which must be in every query
    //  e.g. WHERE col1 = X AND col2 IN (Y1, Y2, Y3, Y4, ...)
    val shouldIterateBindArguments: Boolean

    val hasBindArguments: Boolean

    fun getBindArguments(): Collection<*>

    fun toSql(): String

    interface Builder<M> {
        fun <J> where(whereColumnExpr: ColumnExpr<M, J>, whereValue: J)

        fun <J> where(whereColumnExpr: ColumnExpr<M, J>, whereValues: Collection<J>, iterate: Boolean = false)

        fun where(whereColumnExpr: ColumnExpr<M, *>, isNotNull: Boolean)

        fun where(whereString: String)

        fun whereLike(
            likeColumns: Collection<Column<M, String>>,
            whereLikeValues: Collection<String>,
            conjunction: Conjuction = Conjuction.OR
        )
    }
}