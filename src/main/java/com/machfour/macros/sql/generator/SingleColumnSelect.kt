package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.Table

class SingleColumnSelect<M, J> private constructor(
    val selectColumn: Column<M, J>,
    private val query: SelectQuery<M>
): SelectQuery<M> by query {

    companion object {
        fun <M, J> build(
            table: Table<M>,
            selectColumn: Column<M, J>,
            queryOptions: SelectQuery.Builder<M>.() -> Unit
        ) : SingleColumnSelect<M, J> {
            val query = SelectQueryImpl.Builder(table, listOf(selectColumn)).run {
                queryOptions()
                buildQuery()
            }
            return SingleColumnSelect(selectColumn, query)
        }
    }
}

