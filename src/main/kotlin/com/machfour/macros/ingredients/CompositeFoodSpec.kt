package com.machfour.macros.ingredients

import com.machfour.macros.schema.FoodTable
import com.machfour.macros.sql.datatype.TypeCastException
import com.machfour.macros.sql.datatype.Types

class CompositeFoodSpec(val indexName: String, val name: String, variety: String?, notes: String?) {
    val variety: String?
    val notes: String?
    private val ingredients = ArrayList<IngredientSpec>()

    init {
        this.variety = emptyStringAsNull(variety)
        this.notes = emptyStringAsNull(notes)
    }


    private fun emptyStringAsNull(input: String?): String? {
        return when (input) {
            null -> null
            else -> try {
                Types.TEXT.fromRawString(input) // empty string -> null data
            } catch (ignored: TypeCastException) {
                null
            }
        }
    }

    fun addIngredients(ingredientSpecs: Collection<IngredientSpec>) {
        ingredients.addAll(ingredientSpecs)
    }

    fun getIngredients(): List<IngredientSpec> = ingredients.toList()

    fun prettyPrint(withIngredients: Boolean): String {
        return buildString {
            append("{ ")
            append("${FoodTable.INDEX_NAME.sqlName}: $indexName, ")
            append("${FoodTable.NAME.sqlName}: $name, ")
            append("${FoodTable.VARIETY.sqlName}: $variety, ")
            append("${FoodTable.NOTES.sqlName}: $notes")
            if (withIngredients) {
                append(", ingredients: [${ingredients.joinToString(separator = " ")}]" )
            }
            append(" }")
        }

    }

    override fun toString(): String {
        return prettyPrint(false)
    }

}