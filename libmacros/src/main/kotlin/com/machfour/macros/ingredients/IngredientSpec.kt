package com.machfour.macros.ingredients

import com.machfour.macros.formatting.toString

data class IngredientSpec(val indexName: String, val quantity: Double, val unit: String, val notes: String?) {
    override fun toString(): String {
        return "{ indexName: $indexName, quantity: ${quantity.toString(2)}, quantityUnit: $unit, notes: $notes }"
    }
}