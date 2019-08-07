package com.machfour.macros.ingredients;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.machfour.macros.core.*;
import com.machfour.macros.core.datatype.Types;
import com.machfour.macros.objects.*;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.validation.SchemaViolation;

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
            throw new RuntimeException(String.format("No food found in ingredientMap with index name %s", spec.indexName));
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

        if (!builder.canConstruct()) {
            throw new SchemaViolation(builder.findAllErrors());
            // throw SchemaViolation
        }

        return builder.build();
    }

    private static Set<String> extractIngredientIndexNames(Collection<CompositeFoodSpec> allSpecs) {
        // say there are an average of 4 ingredients per composite food
        Set<String> indexNames = new HashSet<>(4 * allSpecs.size());
        for (CompositeFoodSpec cSpec : allSpecs) {
            for (IngredientSpec iSpec : cSpec.ingredients) {
                indexNames.add(iSpec.indexName);
            }
        }
        return indexNames;
    }


    // creates a composite food and ingredients objects from the given spec
    // NOTE that no IDs are ever created for the objects
    private static CompositeFood processCompositeFoodSpec(CompositeFoodSpec spec, Map<String, Long> indexNameMap) {
        MacrosBuilder<Food> builder = new MacrosBuilder<>(Food.table());
        builder.setField(Schema.FoodTable.INDEX_NAME, spec.indexName);
        builder.setField(Schema.FoodTable.NAME, spec.name);
        builder.setField(Schema.FoodTable.VARIETY, spec.variety);
        builder.setField(Schema.FoodTable.VARIETY_AFTER_NAME, false);
        builder.setField(Schema.FoodTable.NOTES, spec.notes);
        builder.setField(Schema.FoodTable.CATEGORY, "recipes"); //TODO
        // setting this means that Food.factory().construct() will create a CompositeFood
        builder.setField(Schema.FoodTable.FOOD_TYPE, FoodType.COMPOSITE.getName());

        if (!builder.canConstruct()) {
            throw new SchemaViolation(builder.findAllErrors());
            // throw SchemaViolation
        }

        CompositeFood composite = (CompositeFood) builder.build();
        // create the ingredients
        for (IngredientSpec iSpec : spec.ingredients) {
            // TODO remember that composite has no id!!
            Ingredient i = processIngredientSpec(iSpec, composite, indexNameMap);
            composite.addIngredient(i);
        }

        return composite; // NOTE ingredients have not yet been saved into the DB!
    }


    // Creates objects corresponding to the parsed composite food specs.
    // THE INGREDIENTS CANNOT BE SAVED INTO THE DATABASE AS IS, because they do not have the proper foreign keys set up
    // to save the object tree correctly, use the method saveCompositeFoods(compositeFoods, ds)
    static List<CompositeFood> createCompositeFoods(Collection<CompositeFoodSpec> parseResult, MacrosDataSource ds) throws SQLException {
        Set<String> indexNames = extractIngredientIndexNames(parseResult);
        // for invalid index names, the map won't have an entry
        Map<String, Long> indexNameMap = ds.getFoodIdsByIndexName(indexNames);

        List<CompositeFood> results = new ArrayList<>(parseResult.size());
        for (CompositeFoodSpec spec : parseResult) {
            results.add(processCompositeFoodSpec(spec, indexNameMap));
        }
        return results;
    }

    private static List<Ingredient> addIdToIngredients(CompositeFood cf) {
        assert cf.getObjectSource() == ObjectSource.DATABASE;
        assert cf.getId() != MacrosPersistable.NO_ID;

        List<Ingredient> ingredients = cf.getIngredients();
        List<Ingredient> newIngredients = new ArrayList<>(ingredients.size());

        for (Ingredient i : ingredients) {
            MacrosBuilder<Ingredient> builder = new MacrosBuilder<>(i);
            builder.setField(Schema.IngredientTable.COMPOSITE_FOOD_ID, cf.getId());
            newIngredients.add(builder.build());
        }
        return newIngredients;
    }

    // saves a composite food and all its ingredients into the database
    private static void saveCompositeFood(CompositeFood cf, MacrosDataSource ds) throws SQLException {

        // First save the food and then recreate with ID
        ds.saveObject(cf);
        cf = (CompositeFood) ds.getFoodByIndexName(cf.getIndexName());
        // So that we can edit the ingredients to have the ID
        List<Ingredient> newIngredients = addIdToIngredients(cf);
        // here we go!
        ds.insertObjects(newIngredients, false);
    }

    static void saveCompositeFoods(Collection<CompositeFood> compositeFoods, MacrosDataSource ds) throws SQLException {
        // TODO save all the composite foods and recreate them in one go
        // Then, save the ingredients at the same time.
        for (CompositeFood cf : compositeFoods) {
            saveCompositeFood(cf, ds);
        }
    }
}
