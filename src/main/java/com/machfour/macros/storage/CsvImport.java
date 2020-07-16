package com.machfour.macros.storage;

import com.machfour.macros.core.*;
import com.machfour.macros.core.datatype.TypeCastException;
import com.machfour.macros.objects.CompositeFood;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.FoodType;
import com.machfour.macros.objects.Ingredient;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.objects.Serving;
import com.machfour.macros.queries.FkCompletion;
import com.machfour.macros.queries.FoodQueries;
import com.machfour.macros.queries.Queries;
import com.machfour.macros.util.Pair;
import com.machfour.macros.validation.SchemaViolation;

import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.machfour.macros.core.Schema.FoodTable.INDEX_NAME;

public class CsvImport {
    // don't edit csvRow keyset!
    static <M> ColumnData<M> extractData(Map<String, String> csvRow, Table<M> table) throws TypeCastException {
        Set<String> relevantCols = new HashSet<>(csvRow.keySet());
        relevantCols.retainAll(table.getColumnsByName().keySet());
        ColumnData<M> data = new ColumnData<>(table);
        for (String colName: relevantCols) {
            String value = csvRow.get(colName);
            Column<M, ?> col = table.getColumnsByName().get(colName);
            // map empty strings in CSV to null
            if (value == null) {
                data.putFromRaw(col, null);
            } else {
                data.putFromString(col, value.trim());
            }
        }
        return data;
    }

    static ICsvMapReader getMapReader(Reader r) {
        // EXCEL_PREFERENCE sets newline character to '\n', quote character to '"' and delimiter to ','
        return new CsvMapReader(r, CsvPreference.EXCEL_PREFERENCE);
    }

    // returns true if all fields in the CSV are blank AFTER IGNORING WHITESPACE
    private static boolean allValuesEmpty(Map<String, String> csvRow) {
        boolean result = true;
        for (String s : csvRow.values()) {

            if (s != null && !s.trim().isEmpty()) {
                result = false;
                break;
            }
        }
        return result;
    }
    // Returns map of food index name to parsed food and nutrition columnData objects
    private static List<Pair<ColumnData<Food>, ColumnData<NutritionData>>> getFoodData(Reader foodCsv)
            throws IOException, TypeCastException {
        List<Pair<ColumnData<Food>, ColumnData<NutritionData>>> data = new ArrayList<>();
        try (ICsvMapReader mapReader = getMapReader(foodCsv)) {
            final String[] header = mapReader.getHeader(true);
            Map<String, String> csvRow;
            while ((csvRow = mapReader.read(header)) != null) {
                if (allValuesEmpty(csvRow)) {
                    continue; // it's a blank row
                }
                ColumnData<Food> foodData = extractData(csvRow, Schema.FoodTable.instance());
                ColumnData<NutritionData> ndData = extractData(csvRow, Schema.NutritionDataTable.instance());
                data.add(new Pair<>(foodData, ndData));
            }
        }
        return data;
    }

    // map from composite food index name to list of ingredients
    // XXX adding the db to get ingredient food objects looks ugly
    private static Map<String, List<Ingredient>> makeIngredients(Reader ingredientCsv, MacrosDataSource ds)
            throws IOException, SQLException, TypeCastException {
        Map<String, List<Ingredient>> data = new HashMap<>();
        try (ICsvMapReader mapReader = getMapReader(ingredientCsv)) {
            final String[] header = mapReader.getHeader(true);
            Map<String, String> csvRow;
            while ((csvRow = mapReader.read(header)) != null) {
                if (allValuesEmpty(csvRow)) {
                    continue; // it's a blank row
                }
                // XXX CSV contains food index names, while the DB wants food IDs - how to convert?????
                ColumnData<Ingredient> ingredientData = extractData(csvRow, Schema.IngredientTable.instance());

                String compositeIndexName = csvRow.get("recipe_index_name");
                String ingredientIndexName = csvRow.get("ingredient_index_name");
                // TODO error handling
                Food ingredientFood = FoodQueries.getFoodByIndexName(ds, ingredientIndexName);
                if (ingredientFood == null) {
                    throw new RuntimeException("No ingredient exists with index name: " + ingredientIndexName);
                }

                ingredientData.put(Schema.IngredientTable.INGREDIENT_FOOD_ID, ingredientFood.getId());

                Ingredient i = Ingredient.factory().construct(ingredientData, ObjectSource.IMPORT);
                //ingredientData.putExtraData(Schema.IngredientTable.COMPOSITE_FOOD_ID, compositeFoodIndexName);
                i.setFkParentNaturalKey(Schema.IngredientTable.COMPOSITE_FOOD_ID, INDEX_NAME, compositeIndexName);
                i.setIngredientFood(ingredientFood);

                // add the new ingredient data to the existing list in the map, or create one if it doesn't yet exist.
                if (data.containsKey(compositeIndexName)) {
                    data.get(compositeIndexName).add(i);
                } else {
                    List<Ingredient> recipeIngredients = new ArrayList<>();
                    recipeIngredients.add(i);
                    data.put(compositeIndexName, recipeIngredients);
                }
            }
        } catch (SQLException e) {
            throw e; // TODO throw new CSVImportException(csvData)
        }
        return data;

    }

    // creates Composite food objects with ingredients lists (all with no IDs), but the ingredients are raw
    // (don't have linked food objects of their own)
    //
    static Map<String, CompositeFood> buildCompositeFoodObjectTree(Reader recipeCsv, Map<String, List<Ingredient>> ingredients)
            throws IOException, TypeCastException {
        // preserve insertion order
        Map<String, CompositeFood> foodMap = new LinkedHashMap<>();
        Map<String, ColumnData<NutritionData>> ndMap = new LinkedHashMap<>();
        // nutrition data may not be complete, so we can't create it yet. Just create the foods
        for (Pair<ColumnData<Food>, ColumnData<NutritionData>> rowData : getFoodData(recipeCsv)) {
            ColumnData<Food> foodData = rowData.getFirst();
            ColumnData<NutritionData> ndData = rowData.getSecond();

            foodData.put(Schema.FoodTable.FOOD_TYPE, FoodType.COMPOSITE.getName());
            Food f = Food.factory().construct(foodData, ObjectSource.IMPORT);
            assert f instanceof CompositeFood;

            if (foodMap.containsKey(f.getIndexName())) {
                // TODO make this nicer
                throw new RuntimeException("Imported recipes contained duplicate index name: " + f.getIndexName());
            }
            foodMap.put(f.getIndexName(), (CompositeFood)f);
            ndMap.put(f.getIndexName(), ndData);
        }

        for (Map.Entry<String, List<Ingredient>> ingredientsByRecipe : ingredients.entrySet()) {
            CompositeFood recipeFood = foodMap.get(ingredientsByRecipe.getKey());
            for (Ingredient i: ingredientsByRecipe.getValue()) {
                i.setCompositeFood(recipeFood);
                recipeFood.addIngredient(i);
            }
        }
        // now we can finally create the nutrition data

        for (CompositeFood cf : foodMap.values()) {
            ColumnData<NutritionData> csvNutritionData = ndMap.get(cf.getIndexName());
            if (csvNutritionData.hasData(Schema.NutritionDataTable.QUANTITY)) {
                // assume that there is overriding data
                NutritionData overridingData = NutritionData.factory().construct(csvNutritionData, ObjectSource.IMPORT);
                cf.setNutritionData(overridingData);
                // calling cf.getnData will now correctly give all the values
            }
        }
        return foodMap;
    }

    // returns a pair of maps from food index name to corresponding food objects and nutrition data objects respectively
    // TODO can probably refactor this to just return one food
    static Map<String, Food> buildFoodObjectTree(Reader foodCsv) throws IOException, TypeCastException {
        // preserve insertion order
        Map<String, Food> foodMap = new LinkedHashMap<>();
        for (Pair<ColumnData<Food>, ColumnData<NutritionData>> rowData : getFoodData(foodCsv)) {
            ColumnData<Food> foodData = rowData.getFirst();
            ColumnData<NutritionData> ndData = rowData.getSecond();
            Food f;
            NutritionData nd;
            try {
                f = Food.factory().construct(foodData, ObjectSource.IMPORT);
            } catch (SchemaViolation e) {
                // TODO make this nicer
                throw new RuntimeException("Schema violation detected in food: " + e.getMessage() +
                        "\nData:\n" + foodData.toString());
                //continue;
            }
            try {
                nd = NutritionData.factory().construct(ndData, ObjectSource.IMPORT);
            } catch (SchemaViolation e) {
                // TODO make this nicer
                throw new RuntimeException("Schema violation detected in nutrition data: " + e.getMessage() +
                        "\nData:\n" + foodData.toString());
                //continue;
            }
            f.setNutritionData(nd); // without pairs, needed to recover nutrition data from return value
            if (foodMap.containsKey(f.getIndexName())) {
                // TODO make this nicer
                throw new RuntimeException("Imported foods contained duplicate index name: " + f.getIndexName());
            }
            foodMap.put(f.getIndexName(), f);
        }
        return foodMap;
    }

    static List<Serving> buildServings(Reader servingCsv) throws IOException, TypeCastException {
        List<Serving> servings = new ArrayList<>();
        try (ICsvMapReader mapReader = getMapReader(servingCsv)) {
            final String[] header = mapReader.getHeader(true);
            Map<String, String> csvRow;
            while ((csvRow = mapReader.read(header)) != null) {
                ColumnData<Serving> servingData = extractData(csvRow, Serving.table());
                String foodIndexName = csvRow.get(INDEX_NAME.getSqlName());
                Serving s = Serving.factory().construct(servingData, ObjectSource.IMPORT);
                // TODO move next line to be run immediately before saving
                s.setFkParentNaturalKey(Schema.ServingTable.FOOD_ID, INDEX_NAME, foodIndexName);
                servings.add(s);
            }
        }
        return servings;
    }

    private static Set<String> findDuplicateIndexNames(Collection<String> indexNames, MacrosDataSource ds) throws SQLException {
        List<String> duplicateList = Queries.selectColumn(ds, Food.table(), INDEX_NAME, INDEX_NAME, indexNames, false);
        Set<String> duplicates = new HashSet<>(duplicateList.size());
        duplicates.addAll(duplicateList);
        return duplicates;
    }

    // foods maps from index name to food object. Food object must have nutrition data attached by way of getnData()
    private static void saveImportedFoods(Map<String, ? extends Food> foods, MacrosDataSource ds) throws SQLException {
        // collect all of the index names to be imported, and check if they're already in the DB.
        Set<String> newIndexNames = foods.keySet();
        Set<String> existingIndexNames = findDuplicateIndexNames(newIndexNames, ds);
        /*
        if (allowOverwrite) {
            Map<String, Food> overwriteFoods = new HashMap<>();
        }
        TODO since we only have the index name, need to write another update function to use the secondary key.
        But also, doing a whole bunch of individual writes to the DB (one for each update is compararatively slow
        Maybe it's easier to forget about importing with overwrite. But deleting and re-importing *currently* is an issue
        because FoodPortions are stored using the food ID... Maybe we should switch to using index name.
         */

        // remove entries corresponding to existing foods; this actually modifies the original map
        newIndexNames.removeAll(existingIndexNames);
        // get out the nutrition data
        Map<String, NutritionData> ndObjects = new LinkedHashMap<>(newIndexNames.size(), 1);
        for (Food f : foods.values()) {
            NutritionData nd = f.getNutritionData();
            // link it to the food so that the DB can create the correct foreign key entries
            nd.setFkParentNaturalKey(Schema.NutritionDataTable.FOOD_ID, INDEX_NAME, f);
            ndObjects.put(f.getIndexName(), nd);
        }
        if (!existingIndexNames.isEmpty()) {
            System.out.println("The following foods will be imported; others had index names already present in the database:");
            for (String indexName : newIndexNames) {
                System.out.println(indexName);
            }
        }
        Queries.saveObjects(ds, foods.values(), ObjectSource.IMPORT);
        List<NutritionData> completedNd = FkCompletion.completeForeignKeys(ds, ndObjects.values(), Schema.NutritionDataTable.FOOD_ID);
        Queries.saveObjects(ds, completedNd, ObjectSource.IMPORT);
    }

    public static void importFoodData(Reader foodCsv, MacrosDataSource ds, boolean allowOverwrite)
            throws IOException, SQLException, TypeCastException {
        Map<String, Food> csvFoods = buildFoodObjectTree(foodCsv);
        saveImportedFoods(csvFoods, ds);
    }

    // TODO detect existing servings
    public static void importServings(Reader servingCsv, MacrosDataSource ds, boolean allowOverwrite)
            throws IOException, SQLException, TypeCastException {
        List<Serving> csvServings = CsvImport.buildServings(servingCsv);
        List<Serving> completedServings = FkCompletion.completeForeignKeys(ds, csvServings, Schema.ServingTable.FOOD_ID);
        Queries.saveObjects(ds, completedServings, ObjectSource.IMPORT);
    }

    public static void importRecipes(Reader recipeCsv, Reader ingredientCsv, MacrosDataSource ds)
            throws IOException, SQLException, TypeCastException {
        Map<String, List<Ingredient>> ingredientsByRecipe = makeIngredients(ingredientCsv, ds);
        Map<String, CompositeFood> csvRecipes = buildCompositeFoodObjectTree(recipeCsv, ingredientsByRecipe);
        Set<String> duplicateRecipes = findDuplicateIndexNames(csvRecipes.keySet(), ds);
        // todo remove the extra duplicate check from inside this function
        saveImportedFoods(csvRecipes, ds);

        // add all the ingredients for non-duplicated recipes to one big list, then save them all
        List<Ingredient> allIngredients = new ArrayList<>(3*ingredientsByRecipe.size()); // assume 3 ingredients per recipe on average
        duplicateRecipes.removeAll(ingredientsByRecipe.keySet());
        for (List<Ingredient> recipeIngredients : ingredientsByRecipe.values()) {
            allIngredients.addAll(recipeIngredients);
        }

        List<Ingredient> completedIngredients = FkCompletion.completeForeignKeys(ds, allIngredients, Schema.IngredientTable.COMPOSITE_FOOD_ID);
        Queries.saveObjects(ds, completedIngredients, ObjectSource.IMPORT);
    }

}
