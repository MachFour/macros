package com.machfour.macros.sql

import com.machfour.macros.core.Column
import com.machfour.macros.core.Table
import com.machfour.macros.storage.DatabaseUtils
import com.machfour.macros.util.StringJoiner

open class SelectQuery<M>(
    table: Table<M>,
    val columns: List<Column<M, *>>
): SqlQuery<M>(table, SqlQueryMode.SELECT) {
    private var distinct: Boolean = false
    private var ordering: OrderByClause? = null
    private var whereExpr: SqlWhereExpr = SqlWhereExpr.whereAny()
    private var limit: Int? = null
    private var offset: Int? = null
    private var suffix: String = ""

    companion object {
    }

    private inner class OrderByClause(
        val column: Column<M, *>,
        val direction: OrderByDirection?,
        val nulls: OrderByNullPrecedence?
    )

    fun orderBy(
        column: Column<M, *>,
        order: OrderByDirection? = null,
        nullPrecedence: OrderByNullPrecedence? = null
    ) {
        ordering = OrderByClause(column, order, nullPrecedence)
    }

    fun distinct(selectDistinct: Boolean = true) {
        distinct = selectDistinct
    }

    fun <J> where(whereColumn: Column<M, J>, whereValue: J) {
        whereExpr = SqlWhereExpr.where(whereColumn, whereValue)
    }

    fun <J> where(whereColumn: Column<M, J>, whereValues: Collection<J>) {
        whereExpr = SqlWhereExpr.where(whereColumn, whereValues)
    }

    fun where(whereColumn: Column<M, *>, isNotNull: Boolean) {
        whereExpr = SqlWhereExpr.where(whereColumn, isNotNull)
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

    val hasBindArguments: Boolean
        get() = whereExpr.numArgs > 0

    internal fun getBindArguments(): Collection<*> {
        return whereExpr.getBindObjects() ?: emptyList<Nothing>()
    }

    internal fun toSql(): String {
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

        query.add(whereExpr.toTemplate())

        ordering?.let {
            query.add("ORDER BY")
            query.add(it.column.sqlName)
            it.direction?.let { dir ->
                query.add(dir.sql)
            }
            it.nulls?.let { nulls ->
                query.add(nulls.sql)
            }
        }

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

