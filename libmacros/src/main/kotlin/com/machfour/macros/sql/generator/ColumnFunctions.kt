package com.machfour.macros.sql.generator

import com.machfour.macros.sql.Column

class ColumnMax<M, J: Any> private constructor(col: Column<M, J>): ColumnExpr<M, J> {
    override val sql = "MAX(${col.sqlName})"
    override val table = col.table
    override val type = col.type

    companion object {
        fun <M, J: Any> Column<M, J>.max() = ColumnMax(this)
    }
}

class ColumnMin<M, J: Any> private constructor(col: Column<M, J>): ColumnExpr<M, J> {
    override val sql = "MIN(${col.sqlName})"
    override val table = col.table
    override val type = col.type

    companion object {
        fun <M, J: Any> Column<M, J>.min() = ColumnMin(this)
    }
}

class ColumnCount<M, J: Any> private constructor(col: Column<M, J>): ColumnExpr<M, J> {
    override val sql = "COUNT(${col.sqlName})"
    override val table = col.table
    override val type = col.type

    companion object {
        fun <M, J: Any> Column<M, J>.count() = ColumnCount(this)
    }
}