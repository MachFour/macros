package com.machfour.macros.core

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

    // whether this object was created from a database instance or whether it was created by the
    // application (e.g. by a 'new object' action initiated by the user)
    val source: ObjectSource

    // Used to get data by column objects
    fun <J: Any> getData(col: Column<M, J>): J?
    fun hasData(col: Column<M, *>): Boolean

    val data: RowData<M>
    val table: Table<M>
    val factory: Factory<M>

    fun dataCopy(withMetadata: Boolean): RowData<M>
    fun dataFullCopy(): RowData<M> = dataCopy(withMetadata = true)

}