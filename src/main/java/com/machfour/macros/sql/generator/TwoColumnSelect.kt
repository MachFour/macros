package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.Table

class TwoColumnSelect<M, I, J> private constructor(
    val column1: Column<M, I>,
    val column2: Column<M, J>,
    private val query: SelectQuery<M>
) : SelectQuery<M> by query {

    companion object {
        fun <M, I, J> build(
            table: Table<M>,
            column1: Column<M, I>,
            column2: Column<M, J>,
            queryOptions: SelectQuery.Builder<M>.() -> Unit
        ) : TwoColumnSelect<M, I, J> {
            return Builder(table, column1, column2).run {
                queryOptions()
                build()
            }
        }
    }

    private class Builder<M, I, J>(
        table: Table<M>,
        val column1: Column<M, I>,
        val column2: Column<M, J>,
    ) : SelectQueryImpl.Builder<M>(table, listOf(column1, column2)) {
        fun build(): TwoColumnSelect<M, I, J> {
            return TwoColumnSelect(column1, column2, buildQuery())
        }
    }
}

