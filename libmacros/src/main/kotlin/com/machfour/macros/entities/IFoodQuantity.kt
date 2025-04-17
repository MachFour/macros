package com.machfour.macros.entities

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.formatting.toStringWithRounding
import com.machfour.macros.nutrients.INutrientValue
import com.machfour.macros.nutrients.NutrientData

interface IFoodQuantity<E: INutrientValue>: MacrosEntity {
    val food: IFood<E>

    val qtyUnit: Unit

    val serving: IServing?

    val foodId: EntityId

    val quantity: Double

    val notes: String?

    val maxNutrientVersion: Int

    val nutrientData: NutrientData<E>

    val servingId: EntityId?

    //fun prettyFormat(withNotes: Boolean): String

    val servingString: String?
        get() {
            val s = serving
            return if (s != null) "$servingCountString ${s.name}" else null
        }

    // returns a string containing the serving count. If the serving count is close to an integer,
    // it is formatted as an integer.
    private val servingCountString: String?
        get() = servingCount?.toStringWithRounding()

    val servingCount: Double?
        get() {
            val s = serving
            return if (s != null) quantity / s.amount else 0.0
        }

}