package com.machfour.macros.ingredients;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.machfour.macros.core.Column;
import com.machfour.macros.core.ColumnData;
import com.machfour.macros.core.MacrosBuilder;
import com.machfour.macros.core.Schema;
import com.machfour.macros.objects.*;
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
     *      "name": "<name>",                           (mandatory)
     *      "index_name": "<index name>",               (mandatory)
     *      "variety": "<variety>",                     (optional)
     *      "notes": "<notes>",                         (optional)
     *      "ingredients": [
     *          "index_name": "<ingredient index name>" (mandatory)
     *          "quantity": <quantity (float)>,         (mandatory)
     *          "quantity_unit": "<unit abbr>",         (mandatory)
     *          "notes": "<notes>",                     (optional)
     *       ]
     * },
     * {
     *      ...  <second composite food>
     * },
     * ...  <more composite foods>
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

    static Ingredient processIngredientSpec(IngredientSpec spec, Food composite, Map<String, Long> ingredientMap) {
        // get food from index name
        // get quantity
        // get quantity unit
        // get notes
        // create Ingredients Object

        if (!ingredientMap.containsKey(spec.indexName)) {
            // exception: no such food
        }

        Long ingredientId = ingredientMap.get(spec.indexName);
        assert ingredientId != null;

        MacrosBuilder<Ingredient> builder = new MacrosBuilder<>(Ingredient.table());
        builder.setField(Schema.IngredientTable.COMPOSITE_FOOD_ID, composite.getId());
        builder.setField(Schema.IngredientTable.INGREDIENT_FOOD_ID, ingredientId);
        builder.setField(Schema.IngredientTable.SERVING_ID, null); // TODO
        builder.setField(Schema.IngredientTable.QUANTITY_UNIT, spec.quantityUnit);
        builder.setField(Schema.IngredientTable.NOTES, spec.notes);
        builder.setField(Schema.IngredientTable.QUANTITY, spec.quantity);

        return builder.build();
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

    private static Food processCompositeFoodSpec(CompositeFoodSpec spec, MacrosDataSource ds, Map<String, Long> indexNameMap)
            throws SQLException {
        MacrosBuilder<Food> builder = new MacrosBuilder<>(Food.table());
        builder.setField(Schema.FoodTable.INDEX_NAME, spec.indexName);
        builder.setField(Schema.FoodTable.NAME, spec.name);
        builder.setField(Schema.FoodTable.VARIETY, spec.variety);
        builder.setField(Schema.FoodTable.VARIETY_AFTER_NAME, false);
        builder.setField(Schema.FoodTable.NOTES, spec.notes);
        builder.setField(Schema.FoodTable.CATEGORY, "Recipes"); //TODO
        builder.setField(Schema.FoodTable.FOOD_TYPE, FoodType.COMPOSITE.getName());


        //spec.ingredients

        try {
            // Food.factory().construct() will ensure the dynamic type is CompositeFood
            CompositeFood composite = (CompositeFood) builder.build();
            if (false) {
                // We need to save it, then recreate with ID. TODO find a better way!
                ds.saveObject(composite);
                composite = (CompositeFood) ds.getFoodByIndexName(spec.indexName);
            }

            // create the ingredients
            for (IngredientSpec iSpec : spec.ingredients) {
                // TODO remember that composite has no id!!
                Ingredient i = processIngredientSpec(iSpec, composite, indexNameMap);
                composite.addIngredient(i);
            }

            return composite; // NOTE ingredients have not yet been saved into the DB!
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
            results.add(processCompositeFoodSpec(spec, ds, indexNameMap));
        }
        return results;
    }

}
