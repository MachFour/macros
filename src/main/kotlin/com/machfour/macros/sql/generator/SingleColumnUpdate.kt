package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.Table

class SingleColumnUpdate<M, J: Any> private constructor(
    val updateColumn: Column<M, J>,
    private val statement: UpdateStatement<M>,
): UpdateStatement<M> by statement {

    companion object {
        fun <M, J: Any> build(
            table: Table<M>,
            updateColumn: Column<M, J>,
            statementOptions: UpdateStatement.Builder<M>.() -> Unit
        ) : SingleColumnUpdate<M, J> {
            val statement = UpdateStatementImpl.Builder(table, listOf(updateColumn)).run {
                statementOptions()
                buildQuery()
            }
            return SingleColumnUpdate(updateColumn, statement)
        }
    }
}