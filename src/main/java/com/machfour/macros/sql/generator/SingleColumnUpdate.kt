package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.Table

class SingleColumnUpdate<M, J> private constructor(
    val updateColumn: Column<M, J>,
    private val statement: UpdateStatement<M>,
): UpdateStatement<M> by statement {

    companion object {
        fun <M, J> build(
            table: Table<M>,
            column: Column<M, J>,
            queryOptions: UpdateStatement.Builder<M>.() -> Unit
        ) : SingleColumnUpdate<M, J> {
            return Builder(table, column).run {
                queryOptions()
                build()
            }
        }
    }

    private class Builder<M, J>(table: Table<M>, private val updateColumn: Column<M, J>)
        : UpdateStatementImpl.Builder<M>(table, listOf(updateColumn)) {
        fun build(): SingleColumnUpdate<M, J> {
            return SingleColumnUpdate(updateColumn, buildQuery())
        }
    }
}