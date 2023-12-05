package com.machfour.macros.json

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.Instant
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.entities.FoodNutrientValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JsonNutrientValue(
    @kotlinx.serialization.Transient
    override val id: EntityId = MacrosEntity.NO_ID,
    @kotlinx.serialization.Transient
    override val created: Instant = 0,
    @kotlinx.serialization.Transient
    override val modified: Instant = 0,
    val value: Double,
    val unit: String,
    @SerialName("constraint_spec")
    val constraintSpec: Int = 0,
    ): JsonEntity() {
        constructor(nv: FoodNutrientValue): this(
            id = nv.id,
            created = nv.createTime,
            modified = nv.modifyTime,
            unit = nv.unit.abbr,
            value = nv.value,
            constraintSpec = nv.constraintSpec,
        )

}