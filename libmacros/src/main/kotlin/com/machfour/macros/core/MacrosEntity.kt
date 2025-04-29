package com.machfour.macros.core

/**
 * Defines common methods for each object to be persisted
 */
interface MacrosEntity {
    companion object {
        val NO_ID = EntityId(-100)
    }

    val id: EntityId
    val hasId: Boolean
        get() = (id != NO_ID)

    val createTime: Instant
    val modifyTime: Instant

    // e.g. whether this object was constructed from a database instance or user input
    val source: ObjectSource
}