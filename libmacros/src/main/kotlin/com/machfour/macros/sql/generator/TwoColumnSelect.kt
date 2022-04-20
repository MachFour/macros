package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.Table

class TwoColumnSelect<M, I: Any, J: Any> private constructor(
    val column1: Column<M, I>,
    val column2: Column<M, J>,
    private val query: SelectQuery<M>
) : SelectQuery<M> by query {

    companion object {
        fun <M, I: Any, J: Any> build(
            table: Table<M>,
            column1: Column<M, I>,
            column2: Column<M, J>,
            queryOptions: SelectQuery.Builder<M>.() -> Unit
        ) : TwoColumnSelect<M, I, J> {
            val query = SelectQueryImpl.Builder(table, listOf(column1, column2)).run {
                queryOptions()
                buildQuery()
            }
            return TwoColumnSelect(column1, column2, query)
        }
    }
}

