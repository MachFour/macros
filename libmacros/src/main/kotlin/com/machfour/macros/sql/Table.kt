package com.machfour.macros.sql

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.sql.entities.Deconstructor
import com.machfour.macros.sql.entities.Factory
import com.machfour.macros.sql.rowdata.RowData

interface Table<I: MacrosEntity, M : I>: Factory<I, M> {
    val sqlName: String
    val columns: List<Column<M, out Any>>

    val columnsByName: Map<String, Column<M, *>>

    val idColumn: Column<M, EntityId>
    val createTimeColumn: Column<M, EntityId>
    val modifyTimeColumn: Column<M, EntityId>
}