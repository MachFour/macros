package com.machfour.macros.sql.generator

import com.machfour.macros.util.stringJoin

class OrderByClause<M>(
    clauseBody: String
) {
    val sql = "ORDER BY $clauseBody"

    constructor(
        columnExpr: ColumnExpr<M, *>,
        direction: OrderByDirection?,
        nulls: OrderByNullPrecedence?,
    ): this(makeClauseBody(columnExpr, direction, nulls))

    // can specify a number - this will sort by the corresponding output column
    // numbers are 1 indexed
    constructor(
        columnNumber: Int,
        direction: OrderByDirection?,
        nulls: OrderByNullPrecedence?,
    ): this(makeClauseBody(columnNumber.toString(), direction, nulls))

    companion object {
        private fun makeClauseBody(
            columnSpec: String,
            direction: OrderByDirection?,
            nulls: OrderByNullPrecedence?,
        ): String {
            val words = ArrayList<String>().also {
                it.add(columnSpec)
                if (direction != null) {
                    it.add(direction.sql)
                }
                if (nulls != null) {
                    it.add(nulls.sql)
                }
            }
            return stringJoin(words, sep = " ")
        }
        private fun <M> makeClauseBody(
            columnExpr: ColumnExpr<M, *>,
            direction: OrderByDirection?,
            nulls: OrderByNullPrecedence?,
        ): String {
            return makeClauseBody(columnExpr.sql, direction, nulls)
        }
    }
}
