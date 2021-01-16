package com.machfour.macros.sql

import com.machfour.macros.core.Column
import com.machfour.macros.core.Table

class MultiColumnSelect<M> private constructor(
    table: Table<M>,
    orderedColumns: Collection<Column<M, *>>
) : SelectQuery<M>(
    table,
    orderedColumns.toList()
) {
    companion object {
        fun <M> build(
            table: Table<M>,
            columns: Collection<Column<M, *>>,
            queryOptions: MultiColumnSelect<M>.() -> Unit
        ) : MultiColumnSelect<M> {
            return MultiColumnSelect(table, columns).apply {
                queryOptions()
            }
        }
    }
}

