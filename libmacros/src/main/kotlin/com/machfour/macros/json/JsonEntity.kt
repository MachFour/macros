package com.machfour.macros.json

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.Instant
import kotlinx.serialization.Serializable

@Serializable
sealed class JsonEntity {
    abstract val id: EntityId
    abstract val created: Instant
    abstract val modified: Instant
}