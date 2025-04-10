package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.Table

internal class SelectQueryImpl<M> private constructor(
    table: Table<M>,
    override val columns: List<Column<M, *>>,
    override val whereExpression: SqlWhereExpr<M, *>,
    private val fromSuffix: String?,
    private val distinct: Boolean,
    private val ordering: OrderByClause<M>?,
    private val grouping: GroupByClause<M>?,
    private val limit: Int?,
    private val offset: Int?,
    private val suffix: String,
): SqlStatementImpl<M>(table, SqlQueryMode.SELECT), SelectQuery<M> {
    override val isOrdered: Boolean
        get() = ordering != null

    override fun toSql(): String {
        val query = ArrayList<String>()
        query.add(mode.sql)
        if (distinct) {
            query.add("DISTINCT")
        }
        if (columns.isEmpty()) {
            query.add("*")
        } else {
            query.add(columns.joinToString(separator = ", ") { it.sqlName })
        }
        if (fromSuffix != null) {
            query.add(", $fromSuffix")
        }

        query.add("FROM")
        query.add(table.sqlName)

        query.add(whereExpression.toSql())

        grouping?.let { query.add(it.sql) }
        ordering?.let { query.add(it.sql) }

        limit?.let { l ->
            query.add("LIMIT $l")
            offset?.let { o ->
                query.add("OFFSET $o")
            }
        }
        query.add(suffix)

        return query.joinToString(separator = " ")
            //.also { println("Generated SQL: $it") }
    }

    internal class Builder<M>(
        table: Table<M>,
        val columns: List<Column<M, *>>
    ): SqlStatementImpl.Builder<M>(table), SelectQuery.Builder<M> {
        private var distinct: Boolean = false
        private var fromSuffix: String? = null
        private var ordering: OrderByClause<M>? = null
        private var grouping: GroupByClause<M>? = null
        private var limit: Int? = null
        private var offset: Int? = null
        private var suffix: String = ""

        override fun fromSuffix(suffix: String) {
            fromSuffix = suffix
        }

        override fun orderBy(
            columnExpr: ColumnExpr<M, *>,
            order: OrderByDirection?,
            nullPrecedence: OrderByNullPrecedence?
        ) {
            ordering = OrderByClause(columnExpr, order, nullPrecedence)
        }

        override fun orderBy(clauseBody: String) {
            ordering = OrderByClause(clauseBody)
        }

        override fun groupBy(columnExpr: ColumnExpr<M, *>) {
            grouping = GroupByClause(columnExpr)
        }
        override fun groupBy(clauseBody: String) {
            grouping = GroupByClause(clauseBody)
        }

        override fun distinct() {
            distinct = true
        }

        override fun notDistinct() {
            distinct = false
        }

        override fun limit(limit: Int, offset: Int?) {
            this.limit = limit
            this.offset = offset
        }

        @Deprecated("Avoid using raw suffix")
        override fun rawSuffix(sql: String) {
            suffix = sql
        }

        fun buildQuery(): SelectQueryImpl<M> {
            return SelectQueryImpl(
                table = table,
                whereExpression = whereExpression,
                columns = columns,
                fromSuffix = fromSuffix,
                distinct = distinct,
                ordering = ordering,
                grouping = grouping,
                limit = limit,
                offset = offset,
                suffix = suffix,
            )
        }
    }

}

