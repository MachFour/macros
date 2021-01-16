package com.machfour.macros.sql

import com.machfour.macros.core.Table

class AllColumnSelect<M> private constructor(
    table: Table<M>,
) : SelectQuery<M>(
    table,
    listOf()
) {
    companion object {
        fun <M> build(
            table: Table<M>,
            queryOptions: AllColumnSelect<M>.() -> Unit
        ) : AllColumnSelect<M> {
            return AllColumnSelect(table).apply {
                queryOptions()
            }
        }
    }
}
