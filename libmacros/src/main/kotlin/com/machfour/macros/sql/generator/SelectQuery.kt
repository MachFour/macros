package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Column

interface SelectQuery<M>: SqlStatement<M> {
    val columns: List<Column<M, *>>

    // true if ORDER BY is part of this query
    val isOrdered: Boolean

    interface Builder<M>: SqlStatement.Builder<M> {
        fun fromSuffix(suffix: String)

        fun orderBy(columnExpr: ColumnExpr<M, *>, order: OrderByDirection? = null, nullPrecedence: OrderByNullPrecedence? = null)

        fun orderBy(clauseBody: String)

        fun groupBy(columnExpr: ColumnExpr<M, *>)
        fun groupBy(clauseBody: String)

        fun distinct()

        fun notDistinct()

        fun limit(limit: Int, offset: Int? = null)

        @Deprecated("Avoid using raw suffix")
        fun rawSuffix(sql: String)
    }
}