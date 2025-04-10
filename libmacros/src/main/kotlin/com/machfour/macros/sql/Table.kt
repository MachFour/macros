package com.machfour.macros.sql

import com.machfour.macros.core.ObjectSource

interface Table<M> {
    val sqlName: String
    val columns: List<Column<M, out Any>>

    // return all FK columns
    val fkColumns: List<Column.Fk<M, *, *>>
    val columnsByName: Map<String, Column<M, *>>

    val idColumn: Column<M, Long>
    val createTimeColumn: Column<M, Long>
    val modifyTimeColumn: Column<M, Long>

    // returns a list of columns that can be used to identify an individual row,
    // if such a list exists for this table. If not, an empty list is returned.
    val secondaryKeyCols: List<Column<M, *>>

    fun construct(data: RowData<M>, source: ObjectSource): M
}