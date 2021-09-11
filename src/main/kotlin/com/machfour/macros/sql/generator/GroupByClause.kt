package com.machfour.macros.sql.generator

class GroupByClause<M>(clauseBody: String) {
    constructor(columnExpr: ColumnExpr<M, *>): this(makeClauseBody(columnExpr))

    val sql = "GROUP BY $clauseBody"

    companion object {
        private fun <M> makeClauseBody(columnExpr: ColumnExpr<M, *>) : String {
            return columnExpr.sql
        }
    }
}