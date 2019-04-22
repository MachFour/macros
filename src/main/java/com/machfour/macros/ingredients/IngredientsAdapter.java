package com.machfour.macros.ingredients;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

class IngredientsAdapter extends TypeAdapter<IngredientSpec> {
    @Override
    public void write(JsonWriter writer, IngredientSpec spec) throws IOException {
        if (spec == null) {
            if (writer.getSerializeNulls()) {
                writer.nullValue();
            }
            return;
        }
        writer.beginObject();
        writer.name("index_name");
        writer.value(spec.indexName);
        writer.name("quantity");
        writer.value(spec.quantity);
        writer.name("quantity_unit");
        writer.value(spec.quantityUnit);
        if (spec.notes != null) {
            writer.name("notes");
            writer.value(spec.notes);
        } else if (writer.getSerializeNulls()) {
            writer.name("notes");
            writer.nullValue();
        }
        writer.endObject();
    }

    @Override
    public IngredientSpec read(JsonReader reader) throws IOException {
        String indexName = null;
        Double quantity = null;
        String quantityUnit = null;
        String notes = null;
        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            switch (key) {
                case "index_name":
                    indexName = reader.nextString();
                    break;
                case "quantity":
                    quantity = reader.nextDouble();
                    break;
                case "quantity_unit":
                    quantityUnit = reader.nextString();
                    break;
                case "notes":
                    notes = reader.nextString();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return new IngredientSpec(indexName, quantity, quantityUnit, notes);
    }

}
