package com.machfour.macros.json

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.Instant
import com.machfour.macros.units.UnitType
import kotlinx.serialization.Serializable

@Serializable
class JsonNutrient(
    override val id: EntityId,
    override val created: Instant,
    override val modified: Instant,
    val name: String,
    val isInbuilt: Boolean,
    val unitTypes: Set<UnitType>
): JsonEntity()