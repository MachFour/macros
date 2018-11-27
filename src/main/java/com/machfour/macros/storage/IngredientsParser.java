package com.machfour.macros.storage;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IngredientsParser {
    /*
     * Ingredient file format:
     * [
     * {
     *      // first composite food
     *      "name": "<name>",
     *      "index_name": "<index name>",
     *      "variety": "<variety>",
     *      "ingredients": [
     *          "<portion spec 1>",
     *          "<portion spec 2>",
     *       ]
     * },
     * {
     *      // second composite food
     *      ...
     * }
     * ]
     *
     */

    static Collection<CompositeFoodSpec> deserialise(String json) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(CompositeFoodSpec.class, new CompositeFoodAdapter().nullSafe());
        // if PointAdapter didn't check for nulls in its read/write methods, you should instead use
        // builder.registerTypeAdapter(Point.class, new PointAdapter().nullSafe());
        Gson gson = builder.create();
        // this creates an anonymous subclass of typetoken?
        Type collectionType = new TypeToken<Collection<CompositeFoodSpec>>(){}.getType();
        return gson.fromJson(json, collectionType);
    }

    static class CompositeFoodAdapter extends TypeAdapter<CompositeFoodSpec> {

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
            List<String> ingredientSpecs = new ArrayList<>();
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
                    case "ingredients":
                        if (reader.peek() == JsonToken.BEGIN_ARRAY) {
                            reader.beginArray();
                            while (reader.hasNext()) {
                                ingredientSpecs.add(reader.nextString());
                            }
                            reader.endArray();
                        }
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            CompositeFoodSpec spec = new CompositeFoodSpec(name, variety, indexName);
            for (String ingredientSpec : ingredientSpecs) {
                spec.addIngredientSpec(ingredientSpec);
            }

            return spec;
        }
    }

    static class CompositeFoodSpec {
        private final String indexName;
        private final String name;
        private final String variety;
        private final List<String> ingredientSpecs;

        CompositeFoodSpec(String indexName, String name, String variety) {
            this.indexName = indexName;
            this.name = name;
            this.variety = variety;
            ingredientSpecs = new ArrayList<>();
        }

        void addIngredientSpec(@NotNull String spec) {
            assert !ingredientSpecs.contains(spec);
            ingredientSpecs.add(spec);
        }
    }
}
