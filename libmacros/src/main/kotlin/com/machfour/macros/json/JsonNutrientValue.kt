package com.machfour.macros.json

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.Instant
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.INutrient
import com.machfour.macros.entities.Unit
import com.machfour.macros.nutrients.INutrientValue
import com.machfour.macros.nutrients.nutrientWithName
import com.machfour.macros.units.unitWithAbbr
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class JsonNutrientValue(
    @Transient
    override val id: EntityId = MacrosEntity.NO_ID,
    @Transient
    override val created: Instant = 0,
    @Transient
    override val modified: Instant = 0,
    @Transient
    override val source: ObjectSource = ObjectSource.JSON,

    @SerialName("nutrient")
    val nutrientName: String,

    override val amount: Double,
    @SerialName("unit")
    val unitAbbr: String,
    @SerialName("constraint_spec")
    override val constraintSpec: Int = 0,
): JsonEntity(), INutrientValue {

    private constructor(fnv: INutrientValue) : this(
        id = fnv.id,
        created = fnv.createTime,
        modified = fnv.modifyTime,
        source = fnv.source,
        amount = fnv.amount,
        nutrientName = fnv.nutrient.name,
        unitAbbr = fnv.unit.abbr,
        constraintSpec = fnv.constraintSpec,
    )


    companion object {
        fun INutrientValue.toJsonNutrientValue(): JsonNutrientValue {
            return JsonNutrientValue(this)
        }

        fun JsonQuantity.toJsonNutrientValue(nutrientName: String): JsonNutrientValue {
            return JsonNutrientValue(
                id = id,
                created = created,
                modified = modified,
                source = source,
                nutrientName = nutrientName,
                amount = amount,
                unitAbbr = unitAbbr,
                constraintSpec = constraintSpec,
            )
        }
    }

    fun scale(ratio: Double): INutrientValue {
        return copy(amount = amount * ratio)
    }

    override val nutrient: INutrient
        get() = nutrientWithName(nutrientName)

    @Transient
    override val unit: Unit = unitWithAbbr(unitAbbr)

    override val createTime: Instant
        get() = created

    override val modifyTime: Instant
        get() = modified

}