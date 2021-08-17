package com.machfour.macros.sql

import com.machfour.macros.orm.Table

class AllColumnSelect<M> private constructor(
    private val query: SelectQuery<M>
) : SelectQuery<M> by query {

    companion object {
        fun <M> build(
            table: Table<M>,
            queryOptions: SelectQuery.Builder<M>.() -> Unit
        ) : AllColumnSelect<M> {
            return Builder(table).run {
                queryOptions()
                build()
            }
        }
    }

    private class Builder<M>(table: Table<M>): SelectQueryImpl.Builder<M>(table, emptyList()) {
        fun build(): AllColumnSelect<M> {
            return AllColumnSelect(buildQuery())
        }
    }
}
