package com.machfour.macros.sql

import com.machfour.macros.core.ObjectSource

interface Table<M> {
    val sqlName: String
    val columns: List<Column<M, out Any>>

    val columnsByName: Map<String, Column<M, *>>

    val idColumn: Column<M, Long>
    val createTimeColumn: Column<M, Long>
    val modifyTimeColumn: Column<M, Long>

    fun construct(data: RowData<M>, source: ObjectSource): M
}