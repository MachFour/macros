package com.machfour.macros.json

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.Instant
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.INutrient
import com.machfour.macros.entities.nutrientEquals
import com.machfour.macros.entities.nutrientHashCode
import com.machfour.macros.entities.nutrientToString
import com.machfour.macros.units.UnitType
import kotlinx.serialization.Serializable

@Serializable
data class JsonNutrient(
    @kotlinx.serialization.Transient
    override val id: EntityId = MacrosEntity.NO_ID,
    @kotlinx.serialization.Transient
    override val created: Instant = 0L,
    @kotlinx.serialization.Transient
    override val modified: Instant = 0L,
    @kotlinx.serialization.Transient
    override val source: ObjectSource = ObjectSource.JSON,
    override val name: String,
    @kotlinx.serialization.Transient
    override val isInbuilt: Boolean = true,
    @kotlinx.serialization.Transient
    override val unitTypes: Set<UnitType> = emptySet()
): JsonEntity(), INutrient {

    companion object {
        fun INutrient.toJsonNutrient(): JsonNutrient {
            return JsonNutrient(this)
        }
    }

    private constructor(n: INutrient): this(
        id = n.id,
        created = n.createTime,
        modified = n.modifyTime,
        source = n.source,
        name = n.name,
        isInbuilt = n.isInbuilt,
        unitTypes = n.unitTypes,
    )

    override val createTime: Instant
        get() = created

    override val modifyTime: Instant
        get() = modified

    override fun toString(): String {
        return nutrientToString(this)
    }

    override fun equals(other: Any?): Boolean {
        return nutrientEquals(this, other)
    }

    override fun hashCode(): Int {
        return nutrientHashCode(this)
    }
}