package com.machfour.macros.core

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class EntityId(val value: Long): java.io.Serializable {
    override fun toString(): String {
        return "@$value"
    }
}

val Int.id: EntityId
    get() = EntityId(this.toLong())

val Long.id: EntityId
    get() = EntityId(this)

typealias Instant = Long