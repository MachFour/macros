package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Table

class AllColumnSelect<M> private constructor(
    private val query: SelectQuery<M>
) : SelectQuery<M> by query {

    companion object {
        fun <M> build(
            table: Table<M>,
            queryOptions: SelectQuery.Builder<M>.() -> Unit
        ): AllColumnSelect<M> {
            val query = SelectQueryImpl.Builder(table, emptyList()).run {
                queryOptions()
                buildQuery()
            }
            return AllColumnSelect(query)
        }
    }
}
