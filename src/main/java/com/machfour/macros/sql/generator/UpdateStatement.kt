package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Column

interface UpdateStatement<M>: SqlStatement<M> {
    val columns: List<Column<M, *>>

    interface Builder<M>: SqlStatement.Builder<M> {
    }
}