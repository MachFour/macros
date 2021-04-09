package com.machfour.macros.ingredients

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException

internal class CompositeFoodAdapter : TypeAdapter<CompositeFoodSpec>() {
    @Throws(IOException::class)
    override fun write(writer: JsonWriter, spec: CompositeFoodSpec) {
        // TODO
        return
    }

    @Throws(IOException::class)
    override fun read(reader: JsonReader): CompositeFoodSpec {
        var indexName: String? = null
        var variety: String? = null
        var name: String? = null
        var notes: String? = null
        val ingredients: MutableList<IngredientSpec> = ArrayList()
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "index_name" -> indexName = reader.nextString()
                "name" -> name = reader.nextString()
                "variety" -> variety = reader.nextString()
                "notes" -> notes = reader.nextString()
                "ingredients" -> {
                    reader.beginArray()
                    while (reader.hasNext()) {
                        ingredients.add(ingredientsReader.read(reader))
                    }
                    reader.endArray()
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        requireNotNull(indexName) { "Index name cannot be null" }
        requireNotNull(name) { "name cannot be null" }
        val spec = CompositeFoodSpec(indexName, name, variety, notes)
        spec.addIngredients(ingredients)
        return spec
    }

    companion object {
        // for reading the arrays of ingredients
        val ingredientsReader = IngredientsAdapter()
    }
}