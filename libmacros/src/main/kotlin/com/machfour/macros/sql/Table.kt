package com.machfour.macros.sql

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.sql.entities.Factory

interface Table<I: MacrosEntity, M : I>: Factory<I, M> {
    val sqlName: String
    val columns: List<Column<M, out Any>>

    val columnsByName: Map<String, Column<M, *>>

    val idColumn: Column<M, EntityId>
    val createTimeColumn: Column<M, Long>
    val modifyTimeColumn: Column<M, Long>
}