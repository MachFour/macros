package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Column

class SingleColumnSelect<M, J> private constructor(
    val selectColumn: Column<M, J>,
    private val query: SelectQuery<M>
): SelectQuery<M> by query {

    companion object {
        fun <M, J> build(
            selectColumn: Column<M, J>,
            queryOptions: SelectQuery.Builder<M>.() -> Unit
        ) : SingleColumnSelect<M, J> {
            val query = SelectQueryImpl.Builder(selectColumn.table, listOf(selectColumn)).run {
                queryOptions()
                buildQuery()
            }
            return SingleColumnSelect(selectColumn, query)
        }
    }
}

