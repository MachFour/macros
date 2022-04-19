package com.machfour.macros.ingredients

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.IngredientTable
import java.io.IOException

internal class IngredientsAdapter : TypeAdapter<IngredientSpec>() {
    @Throws(IOException::class)
    override fun write(writer: JsonWriter, spec: IngredientSpec) {
        writer.apply {
            beginObject()
            name(FoodTable.INDEX_NAME.sqlName)
            value(spec.indexName)
            name(IngredientTable.QUANTITY.sqlName)
            value(spec.quantity)
            name(IngredientTable.QUANTITY_UNIT.sqlName)
            value(spec.unit)
            if (spec.notes != null) {
                name(IngredientTable.NOTES.sqlName)
                value(spec.notes)
            } else if (serializeNulls) {
                name(IngredientTable.NOTES.sqlName)
                nullValue()
            }
            endObject()
        }
    }

    @Throws(IOException::class)
    override fun read(reader: JsonReader): IngredientSpec {
        var indexName: String? = null
        var quantity: Double? = null
        var quantityUnit: String? = null
        var notes: String? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                FoodTable.INDEX_NAME.sqlName -> indexName = reader.nextString()
                IngredientTable.QUANTITY.sqlName -> quantity = reader.nextDouble()
                IngredientTable.QUANTITY_UNIT.sqlName -> quantityUnit = reader.nextString()
                IngredientTable.NOTES.sqlName -> notes = reader.nextString()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        requireNotNull(indexName) { "Index name cannot be null" }
        requireNotNull(quantity) { "quantity cannot be null" }
        requireNotNull(quantityUnit) { "Quantity unit cannot be null" }
        return IngredientSpec(indexName, quantity, quantityUnit, notes)
    }
}