package com.machfour.macros.sql

import com.machfour.macros.orm.Column
import com.machfour.macros.orm.Table

class SingleColumnSelect<M, J> private constructor(
    val selectColumn: Column<M, J>,
    private val query: SelectQuery<M>
): SelectQuery<M> by query {

    companion object {
        fun <M, J> build(
            table: Table<M>,
            column: Column<M, J>,
            queryOptions: SelectQuery.Builder<M>.() -> Unit
        ) : SingleColumnSelect<M, J> {
            return Builder(table, column).run {
                queryOptions()
                build()
            }
        }
    }

    private class Builder<M, J>(table: Table<M>, private val selectColumn: Column<M, J>)
        : SelectQueryImpl.Builder<M>(table, listOf(selectColumn)) {
        fun build(): SingleColumnSelect<M, J> {
            return SingleColumnSelect(selectColumn, buildQuery())
        }
    }
}

