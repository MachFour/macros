package com.machfour.macros.nutrients

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.Instant
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.Nutrient
import com.machfour.macros.units.GRAMS

data class Quantity(
    override val id: EntityId = MacrosEntity.NO_ID,
    override val createTime: Instant = 0L,
    override val modifyTime: Instant = 0L,
    override val source: ObjectSource = ObjectSource.JSON,
    override val amount: Double,
    override val unit: com.machfour.macros.entities.Unit
): INutrientValue, IQuantity {
    companion object {
        fun INutrientValue.toQuantity(): Quantity {
            return Quantity(this)
        }

        val NullQuantity = Quantity(amount = 0.0, unit = GRAMS)
    }

    private constructor(nv: INutrientValue) : this(nv.id, nv.createTime, nv.modifyTime, nv.source, nv.amount, nv.unit) {
        require(nv.nutrient === QUANTITY) { "nutrient must be QUANTITY but was ${nv.nutrient}" }
    }

    init {
        require(QUANTITY.compatibleWith(unit)) { "Invalid quantity unit: $unit" }
    }

    override val nutrient: Nutrient
        get() = QUANTITY
}
