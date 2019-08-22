package com.machfour.macros.ingredients;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class CompositeFoodAdapter extends TypeAdapter<CompositeFoodSpec> {
    // for reading the arrays of ingredients
    static final IngredientsAdapter ingredientsReader = new IngredientsAdapter();

    @Override
    public void write(JsonWriter writer, CompositeFoodSpec spec) throws IOException {
        if (spec == null) {
            writer.nullValue();
            return;
        }
        // TODO
        return;
    }

    @Override
    public CompositeFoodSpec read(JsonReader reader) throws IOException {
        String indexName = null;
        String variety = null;
        String name = null;
        String notes = null;
        List<IngredientSpec> ingredients = new ArrayList<>();
        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            switch (key) {
                case "index_name":
                    indexName = reader.nextString();
                    break;
                case "name":
                    name = reader.nextString();
                    break;
                case "variety":
                    variety = reader.nextString();
                    break;
                case "notes":
                    notes = reader.nextString();
                    break;
                case "ingredients":
                    reader.beginArray();
                    while (reader.hasNext()) {
                        ingredients.add(ingredientsReader.read(reader));
                    }
                    reader.endArray();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        CompositeFoodSpec spec = new CompositeFoodSpec(indexName, name, variety, notes);
        spec.addIngredients(ingredients);
        return spec;
    }
}
