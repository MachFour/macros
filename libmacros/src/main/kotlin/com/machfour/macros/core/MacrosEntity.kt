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
    }

    val id: EntityId
    val hasId: Boolean

    val createTime: Instant
    val modifyTime: Instant

    // e.g. whether this object was constructed from a database instance or user input
    val source: ObjectSource

    // Used to get data by column objects
    fun <J: Any> getData(col: Column<M, J>): J?
    fun hasData(col: Column<M, *>): Boolean

    val data: RowData<M>
    val table: Table<M>

    // Data copy without ID or create/modify times
    fun dataCopyWithoutMetadata(): RowData<M>
    fun dataFullCopy(): RowData<M>
}