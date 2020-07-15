package com.machfour.macros.ingredients

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException

internal class IngredientsAdapter : TypeAdapter<IngredientSpec?>() {
    @Throws(IOException::class)
    override fun write(writer: JsonWriter, spec: IngredientSpec?) {
        writer.apply {
            if (spec != null) {
                beginObject()
                name("index_name")
                value(spec.indexName)
                name("quantity")
                value(spec.quantity)
                name("quantity_unit")
                value(spec.unit)
                if (spec.notes != null) {
                    name("notes")
                    value(spec.notes)
                } else if (serializeNulls) {
                    name("notes")
                    nullValue()
                }
                endObject()
            } else if (serializeNulls) {
                nullValue()
            }
        }
    }

    @Throws(IOException::class)
    override fun read(reader: JsonReader): IngredientSpec? {
        var indexName: String? = null
        var quantity: Double? = null
        var quantityUnit: String? = null
        var notes: String? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "index_name" -> indexName = reader.nextString()
                "quantity" -> quantity = reader.nextDouble()
                "quantity_unit" -> quantityUnit = reader.nextString()
                "notes" -> notes = reader.nextString()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        require(indexName != null) { "Index name cannot be null" }
        require(quantity != null) { "quantity cannot be null" }
        require(quantityUnit != null) { "Quantity unit cannot be null" }
        return IngredientSpec(indexName, quantity, quantityUnit, notes)
    }
}