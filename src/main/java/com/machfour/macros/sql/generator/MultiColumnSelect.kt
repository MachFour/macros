package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.Table

class MultiColumnSelect<M> private constructor(
    private val query: SelectQuery<M>
) : SelectQuery<M> by query {

    companion object {
        fun <M> build(
            table: Table<M>,
            orderedColumns: Collection<Column<M, *>>,
            queryOptions: SelectQuery.Builder<M>.() -> Unit
        ) : MultiColumnSelect<M> {
            return Builder(table, orderedColumns).run {
                queryOptions()
                build()
            }
        }
    }

    private class Builder<M>(
        table: Table<M>,
        orderedColumns: Collection<Column<M, *>>
    ): SelectQueryImpl.Builder<M>(table, orderedColumns.toList()) {
        fun build(): MultiColumnSelect<M> {
            return MultiColumnSelect(buildQuery())
        }
    }
}

