package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Table

class SimpleDelete<M> private constructor(
    private val statement: DeleteStatement<M>
): DeleteStatement<M> by statement {

    companion object {
        fun <M> build(
            table: Table<M>,
            statementOptions: DeleteStatement.Builder<M>.() -> Unit
        ): SimpleDelete<M> {
            val statement = DeleteStatementImpl.Builder(table).run {
                statementOptions()
                buildQuery()
            }
            return SimpleDelete(statement)
        }
    }
}