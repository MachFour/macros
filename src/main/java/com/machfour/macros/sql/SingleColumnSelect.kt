package com.machfour.macros.sql

import com.machfour.macros.core.Column
import com.machfour.macros.core.Table

class SingleColumnSelect<M, J> private constructor(
    table: Table<M>,
    val selectColumn: Column<M, J>
) : SelectQuery<M>(
    table,
    listOf(selectColumn)
) {
    companion object {
        fun <M, J> build(
            table: Table<M>,
            column: Column<M, J>,
            queryOptions: SingleColumnSelect<M, J>.() -> Unit
        ) : SingleColumnSelect<M, J> {
            return SingleColumnSelect(table, column).apply {
                queryOptions()
            }
        }
    }
}

