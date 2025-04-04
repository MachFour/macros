package com.machfour.macros.json

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.Instant
import com.machfour.macros.entities.Unit
import com.machfour.macros.units.UnitType
import kotlinx.serialization.Serializable

@Serializable
class JsonUnit(
    override val id: EntityId,
    override val created: Instant,
    override val modified: Instant,
    val name: String,
    val abbr: String,
    val type: UnitType,
    val metricEquivalent: Double,
    val inbuilt: Boolean
): JsonEntity() {
    constructor(u: Unit): this(
        id = u.id,
        created = u.createTime,
        modified = u.modifyTime,
        name = u.name,
        abbr = u.abbr,
        type = u.type,
        metricEquivalent = u.metricEquivalent,
        inbuilt = u.isInbuilt

    )
}