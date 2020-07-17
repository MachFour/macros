package com.machfour.macros.util

import com.machfour.macros.objects.FoodPortion
import com.machfour.macros.objects.QtyUnit

// records intermediate data during parsing of a food portion spec
class FoodPortionSpec(
        val foodIndexName: String,
        // for non-serving mode
        val quantity: Double,
        val unit: QtyUnit?,
        // for serving mode. servingName = "" means default serving
        val servingCount: Double,
        val servingName: String?,
        var isServingMode: Boolean,
        // records an error at any stage of parsing
        var error: String?
) {

    // context data
    @JvmField
    var lineIdx = 0

    // holds the completed food portion object, only created if no other errors were encountered
    @JvmField
    var createdObject: FoodPortion? = null

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