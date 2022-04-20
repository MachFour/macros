package com.machfour.macros.ingredients

data class IngredientSpec(val indexName: String, val quantity: Double, val unit: String, val notes: String?) {
    override fun toString(): String {
        val quantityString = "%.2f".format(quantity)
        return "{ indexName: $indexName, quantity: ${quantityString}, quantityUnit: $unit, notes: $notes }"
    }
}