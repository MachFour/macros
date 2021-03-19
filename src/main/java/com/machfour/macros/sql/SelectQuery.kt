package com.machfour.macros.sql

import com.machfour.macros.core.Column
import com.machfour.macros.core.Table
import com.machfour.macros.persistence.DatabaseUtils
import com.machfour.macros.util.StringJoiner

open class SelectQuery<M>(
    table: Table<M>,
    val columns: List<Column<M, *>>
): SqlQuery<M>(table, SqlQueryMode.SELECT) {
    private var distinct: Boolean = false
    private var ordering: OrderByClause<M>? = null
    private var grouping: GroupByClause<M>? = null
    private var whereExpr: SqlWhereExpr<M, *> = SqlWhereExpr.whereAny()
    private var limit: Int? = null
    private var offset: Int? = null
    private var suffix: String = ""


    fun orderBy(
        columnExpr: ColumnExpr<M, *>,
        order: OrderByDirection? = null,
        nullPrecedence: OrderByNullPrecedence? = null
    ) {
        ordering = OrderByClause(columnExpr, order, nullPrecedence)
    }

    fun orderBy(clauseBody: String) {
        ordering = OrderByClause(clauseBody)
    }

    fun groupBy(columnExpr: ColumnExpr<M, *>) {
        grouping = GroupByClause(columnExpr)
    }
    fun groupBy(clauseBody: String) {
        grouping = GroupByClause(clauseBody)
    }

    fun distinct(selectDistinct: Boolean = true) {
        distinct = selectDistinct
    }

    fun <J> where(whereColumnExpr: ColumnExpr<M, J>, whereValue: J) {
        whereExpr = SqlWhereExpr.where(whereColumnExpr, whereValue)
    }

    fun <J> where(whereColumnExpr: ColumnExpr<M, J>, whereValues: Collection<J>, iterate: Boolean = false) {
        whereExpr = SqlWhereExpr.where(whereColumnExpr, whereValues, iterate)
    }

    fun where(whereColumnExpr: ColumnExpr<M, *>, isNotNull: Boolean) {
        whereExpr = SqlWhereExpr.where(whereColumnExpr, isNotNull)
    }

    fun where(whereString: String) {
        whereExpr = SqlWhereExpr.where(whereString)
    }

    fun whereLike(
        likeColumns: Collection<Column<M, String>>,
        whereLikeValues: Collection<String>,
        conjunction: Conjuction = Conjuction.OR
    ) {
        whereExpr = SqlWhereExpr.whereLike(likeColumns, whereLikeValues, conjunction)
    }

    fun limit(limit: Int, offset: Int? = null) {
        this.limit = limit
        this.offset = offset
    }

    fun rawSuffix(sql: String) {
        suffix = sql
    }

    val isOrdered: Boolean
        get() = ordering != null

    // TODO situation when there are a lot of arguments (e.g. in a WHERE clause), we iterate over them one by one
    //  however there may be some other arguments which must be in every query?
    val shouldIterateBindArguments: Boolean
        get() = whereExpr.isIterated

    val hasBindArguments: Boolean
        get() = whereExpr.numArgs > 0

    fun getBindArguments(): Collection<*> {
        return whereExpr.getBindObjects()
    }

    val whereExpression: SqlWhereExpr<M, *>
        get() = whereExpr


    fun toSql(): String {
        val query = ArrayList<String>()
        query.add(mode.sql)
        if (distinct) {
            query.add("DISTINCT")
        }
        if (columns.isEmpty()) {
            query.add("*")
        } else {
            query.add(DatabaseUtils.joinColumns(columns))
        }
        query.add("FROM")
        query.add(table.name)

        query.add(whereExpr.toSql())

        grouping?.let { query.add(it.sql) }
        ordering?.let { query.add(it.sql) }

        limit?.let { l ->
            query.add("LIMIT $l")
            offset?.let { o ->
                query.add("OFFSET $o")
            }
        }
        query.add(suffix)

        return StringJoiner.of(query).sep(" ").join()
    }

}

