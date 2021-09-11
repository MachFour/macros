package com.machfour.macros.core

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.RowData

// These methods can be used to associate objects together before they're inserted into the database
internal interface FkEntity<M: MacrosEntity<M>>: MacrosEntity<M> {

    // prefer the first version if possible
    fun <N : MacrosEntity<N>, J> setFkParentKey(fkCol: Column.Fk<M, *, N>, parentKeyCol: Column<N, J>, parent: N)
    fun <N, J> setFkParentKey(fkCol: Column.Fk<M, *, N>, parentKeyCol: Column<N, J>, data: J)

    fun <N> getFkParentKey(fkCol: Column.Fk<M, *, N>): RowData<N>
    //fun copyFkNaturalKeyMap(from: FkEntity<M>)

    // maps Fk to RowData associated with the parent key columns...
    // note this includes the data as well as the columns themselves
    val fkParentKeyData: Map<Column.Fk<M, *, *>, RowData<*>>
}