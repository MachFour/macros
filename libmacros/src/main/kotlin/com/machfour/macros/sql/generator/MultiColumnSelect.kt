package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.Table

class MultiColumnSelect<M> private constructor(
    private val query: SelectQuery<M>
) : SelectQuery<M> by query {

    companion object {
        fun <M> build(
            table: Table<*, M>,
            orderedColumns: Collection<Column<M, *>>,
            queryOptions: SelectQuery.Builder<M>.() -> Unit
        ) : MultiColumnSelect<M> {
            val query = SelectQueryImpl.Builder(table, orderedColumns.toList()).run {
                queryOptions()
                buildQuery()
            }
            return MultiColumnSelect(query)
        }
    }
}

