package com.machfour.macros.util

import com.machfour.macros.objects.FoodPortion
import com.machfour.macros.objects.QtyUnit

// records intermediate data during parsing of a food portion spec
class FoodPortionSpec {
    @JvmField
    var foodIndexName: String? = null
    @JvmField
    var isServingMode = false

    // for non-serving mode
    @JvmField
    var quantity = 0.0
    @JvmField
    var unit: QtyUnit? = null

    // for serving mode. servingName = "" means default serving
    @JvmField
    var servingName: String? = null
    @JvmField
    var servingCount = 0.0

    // context data
    @JvmField
    var lineIdx = 0

    // holds the completed food portion object, only created if no other errors were encountered
    @JvmField
    var createdObject: FoodPortion? = null

    // records an error at any stage of parsing
    @JvmField
    var error: String? = null

    override fun equals(other: Any?): Boolean {
        return when (other is FoodPortionSpec) {
            true -> {
                foodIndexName == other.foodIndexName
                    && isServingMode == other.isServingMode
                    && quantity == other.quantity
                    && unit == other.unit
                    && servingName == other.servingName
                    && servingCount == other.servingCount
                // other fields are excluded as they are able to be derived from the fields above
                // (except lineIdx, which is metadata)
            }
            false -> false
        }
    }

    override fun hashCode(): Int {
        return arrayOf(foodIndexName, isServingMode, quantity, unit, servingName, servingCount).contentHashCode()
    }
}