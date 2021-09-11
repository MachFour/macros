package com.machfour.macros.ingredients

import com.machfour.macros.sql.datatype.TypeCastException
import com.machfour.macros.sql.datatype.Types
import com.machfour.macros.util.stringJoin

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
        val pretty = StringBuilder("{ indexName: $indexName, name: $name, variety: $variety, notes: $notes")
        if (withIngredients) {
            val ingredientsString = stringJoin(ingredients, sep = ", ")
            pretty.append(", ingredients: [$ingredientsString]" )
        }
        pretty.append(" }")
        return pretty.toString()
    }

    override fun toString(): String {
        return prettyPrint(false)
    }

}