package com.machfour.macros.ingredients;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.machfour.macros.core.Column;
import com.machfour.macros.core.ColumnData;
import com.machfour.macros.core.MacrosBuilder;
import com.machfour.macros.core.Schema;
import com.machfour.macros.objects.CompositeFood;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.FoodType;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.validation.SchemaViolation;
import com.machfour.macros.validation.ValidationError;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.*;

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

    static Collection<CompositeFoodSpec> deserialiseIngredientsJson(String json) {
        GsonBuilder builder = new GsonBuilder();
        // if PointAdapter didn't check for nulls in its read/write methods, you should instead use
        // builder.registerTypeAdapter(Point.class, new PointAdapter().nullSafe());
        builder.registerTypeAdapter(CompositeFoodSpec.class, new CompositeFoodAdapter().nullSafe());
        Gson gson = builder.create();
        // this creates an anonymous subclass of typetoken?
        Type collectionType = new TypeToken<Collection<CompositeFoodSpec>>(){}.getType();
        return gson.fromJson(json, collectionType);
    }

    static Food processIngredientSpec(IngredientSpec spec, Food composite, Map<String, Long> ingredientMap) {
        // get food from index name
        // get quantity
        // get quantity unit
        // get notes
        // create Ingredients Object

        if (ingredientMap.containsKey(spec.indexName)) {
            // throw some exception
        }

        // TODO
        Long ingredientId = ingredientMap.get(spec.indexName);
        return null;
    }

    private static Set<String> extractAllIndexNames(Collection<CompositeFoodSpec> allSpecs) {
        // say there are an average of 4 ingredients per composite food
        Set<String> indexNames = new HashSet<>(allSpecs.size()*4);
        for (CompositeFoodSpec cSpec : allSpecs) {
            for (IngredientSpec iSpec : cSpec.ingredients) {
                indexNames.add(iSpec.indexName);
            }
        }
        return indexNames;
    }

    static Food processCompositeFoodSpec(CompositeFoodSpec spec, MacrosDataSource dataSource) throws SQLException {
        MacrosBuilder<Food> builder = new MacrosBuilder<>(Food.table());
        builder.setField(Schema.FoodTable.INDEX_NAME, spec.indexName);
        builder.setField(Schema.FoodTable.NAME, spec.name);
        builder.setField(Schema.FoodTable.VARIETY, spec.variety);
        builder.setField(Schema.FoodTable.VARIETY_AFTER_NAME, false);
        builder.setField(Schema.FoodTable.NOTES, spec.notes);
        builder.setField(Schema.FoodTable.CATEGORY, "Recipes"); //XXX
        builder.setField(Schema.FoodTable.FOOD_TYPE, FoodType.COMPOSITE.getName());


        //spec.ingredients

        try {
            CompositeFood composite = (CompositeFood) builder.build(); // Food.factory().construct() will ensure this
            // TODO remove this dependency!
            //dataSource.saveObject(composite); // save it
            //composite = dataSource.getFoodByIndexName(spec.indexName); // ... and recreate it with ID
            // create the ingredients

            composite.addIngredient(null);

            return composite;
        } catch (SchemaViolation e1) {
            throw e1;
        }
        // get the ID

    }

    static List<Food> processParsedCompositeFoods(Collection<CompositeFoodSpec> parseResult, MacrosDataSource ds) throws SQLException {
        Set<String> indexNames = extractAllIndexNames(parseResult);
        // for invalid index names, the map won't have an entry
        Map<String, Long> indexNameMap = ds.getFoodIdsByIndexName(indexNames);

        List<Food> results = new ArrayList<>(parseResult.size());
        for (CompositeFoodSpec spec : parseResult) {
            results.add(processCompositeFoodSpec(spec, ds));
        }
        return results;
    }

}
