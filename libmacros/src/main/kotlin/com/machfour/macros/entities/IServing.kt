package com.machfour.macros.entities

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.PortionMeasurement
import com.machfour.macros.nutrients.IQuantity

interface IServing: MacrosEntity, PortionMeasurement {
    override val name: String

    override val amount: Double
        get() = quantity.amount

    override val unit: Unit
        get() = quantity.unit

    val quantity: IQuantity

    val isDefault: Boolean

    val foodId: EntityId

    val notes: String?

}