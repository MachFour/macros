package com.machfour.macros.json

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.Instant
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.nutrients.IQuantity
import com.machfour.macros.units.unitWithAbbr
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class JsonQuantity(
    @Transient
    override val id: EntityId = MacrosEntity.NO_ID,
    @Transient
    override val created: Instant = 0,
    @Transient
    override val modified: Instant = 0,
    @Transient
    override val source: ObjectSource = ObjectSource.JSON,

    override val amount: Double,
    @SerialName("unit")
    val unitAbbr: String,
    @SerialName("constraint_spec")
    override val constraintSpec: Int = 0,
): JsonEntity(), IQuantity {

    constructor(q: IQuantity): this(
        id = q.id,
        created = q.createTime,
        modified = q.modifyTime,
        amount = q.amount,
        unitAbbr = q.unit.abbr,
        constraintSpec = q.constraintSpec,
    )

    override val createTime: Instant
        get() = created

    override val modifyTime: Instant
        get() = modified

    // TODO is it okay if this throws an exception
    @Transient
    override val unit = unitWithAbbr(unitAbbr)

}