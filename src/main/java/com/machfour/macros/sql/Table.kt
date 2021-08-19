package com.machfour.macros.sql

import com.machfour.macros.orm.Factory
import com.machfour.macros.orm.ObjectSource

interface Table<M> {
    val name: String
    val columns: List<Column<M, *>>

    // return all FK columns
    val fkColumns: List<Column.Fk<M, *, *>>
    val columnsByName: Map<String, Column<M, *>>

    val idColumn: Column<M, Long>
    val createTimeColumn: Column<M, Long>
    val modifyTimeColumn: Column<M, Long>

    // returns a list of columns that can be used to identify an individual row,
    // if such a list exists for this table. If not, an empty list is returned.
    val secondaryKeyCols: List<Column<M, *>>

    // special case when secondary key has a single column.
    val naturalKeyColumn: Column<M, *>?
    val factory: Factory<M>

    fun construct(dataMap: RowData<M>, objectSource: ObjectSource): M = factory.construct(dataMap, objectSource)
}