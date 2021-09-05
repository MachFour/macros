package com.machfour.macros.core

import com.machfour.macros.orm.Factory
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table

/**
 * Defines common methods for each object to be persisted
 */
interface MacrosEntity<M : MacrosEntity<M>> {
    companion object {
        const val NO_ID: Long = -100

        // special ID for the 'null' serving of just grams / mL
        const val UNIT_SERVING: Long = -101
        const val NO_DATE: Long = -99
        const val UNSET = Double.NaN

        fun <M: MacrosEntity<M>> Factory<M>.cloneWithoutMetadata(obj: MacrosEntity<M>): M {
            return construct(obj.dataCopy(withMetadata = false), ObjectSource.COMPUTED)
        }
    }

    val id: Long
    val hasId: Boolean
        get() = (id != NO_ID)

    val createTime: Long
    val modifyTime: Long
    val source: ObjectSource

    // Used to get data by column objects
    fun <J> getData(col: Column<M, J>): J?
    fun hasData(col: Column<M, *>): Boolean

    val data: RowData<M>
    val table: Table<M>
    val factory: Factory<M>

    fun dataCopy(withMetadata: Boolean): RowData<M>
    fun dataFullCopy(): RowData<M> = dataCopy(withMetadata = true)

    // ... Alternative methods that can be used with unique columns
    fun <N : MacrosEntity<N>, J> setFkParentNaturalKey(fkCol: Column.Fk<M, *, N>, parentNaturalKey: Column<N, J>, parent: N)
    fun <N, J> setFkParentNaturalKey(fkCol: Column.Fk<M, *, N>, parentNaturalKey: Column<N, J>, data: J)
    fun <N> getFkParentNaturalKey(fkCol: Column.Fk<M, *, N>): RowData<N>
    val fkNaturalKeyMap: Map<Column.Fk<M, *, *>, *>
    fun copyFkNaturalKeyMap(from: MacrosEntity<M>)

}