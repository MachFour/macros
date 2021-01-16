package com.machfour.macros.sql

import com.machfour.macros.core.Column
import com.machfour.macros.core.Table

class TwoColumnSelect<M, I, J> private constructor(
    table: Table<M>,
    val column1: Column<M, I>,
    val column2: Column<M, J>,
) : SelectQuery<M>(
    table,
    listOf(column1, column2)
) {
    companion object {
        fun <M, I, J> build(
            table: Table<M>,
            column1: Column<M, I>,
            column2: Column<M, J>,
            queryOptions: TwoColumnSelect<M, I, J>.() -> Unit
        ) : TwoColumnSelect<M, I, J> {
            return TwoColumnSelect(table, column1, column2).apply {
                queryOptions()
            }
        }
    }
}

