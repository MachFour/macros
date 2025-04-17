package com.machfour.macros.json

import com.machfour.macros.core.*
import com.machfour.macros.entities.IServing
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JsonServing(
    override val id: EntityId = MacrosEntity.NO_ID,
    override val created: Instant = 0,
    override val modified: Instant = 0,
    override val name: String,
    override val quantity: JsonQuantity,
    @SerialName("is_default")
    override val isDefault: Boolean = false,
    override val notes: String? = null,
    @kotlinx.serialization.Transient
    override val foodId: EntityId = MacrosEntity.NO_ID,
    override val source: ObjectSource = ObjectSource.JSON,
): JsonEntity(), IServing, PortionMeasurement {

    constructor(s: IServing): this(
        id = s.id,
        created = s.createTime,
        modified = s.modifyTime,
        name = s.name,
        quantity = JsonQuantity(amount = s.amount, unitAbbr = s.quantity.unit.abbr),
        isDefault = s.isDefault,
        notes = s.notes
    )

    override val createTime: Instant
        get() = created

    override val modifyTime: Instant
        get() = modified

}