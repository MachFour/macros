package com.machfour.macros.json

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.Instant
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.entities.Serving
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JsonServing(
    override val id: EntityId = MacrosEntity.NO_ID,
    override val created: Instant = 0,
    override val modified: Instant = 0,
    val name: String,
    val quantity: JsonQuantity,
    @SerialName("is_default")
    val isDefault: Boolean = false,
    val notes: String? = null,
): JsonEntity() {
    constructor(s: Serving): this(
        id = s.id,
        created = s.createTime,
        modified = s.modifyTime,
        name = s.name,
        quantity = JsonQuantity(value = s.quantity, unit = s.qtyUnitAbbr),
        isDefault = s.isDefault,
        notes = s.notes
    )
}