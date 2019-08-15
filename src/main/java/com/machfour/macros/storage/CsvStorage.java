package com.machfour.macros.storage;

import com.machfour.macros.core.*;
import com.machfour.macros.objects.*;
import com.machfour.macros.util.Pair;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class CsvStorage {
    /*
     * Method for reading CSV files that directly correspond to a table
     */
    public static <M> List<M> buildObjectsForRestore(Table<M> table, Reader csvData) throws IOException {
        Map<String, Column<M, ?>> columnsByName = table.columnsByName();
        List<M> objectList = new ArrayList<>();
        Set<String> unrecognisedStrings = new HashSet<>();

        try (ICsvMapReader mapReader = getMapReader(csvData)) {
            // header columns are used as the keys to the Map
            final String[] header = mapReader.getHeader(true);
            for (String colName : header) {
                if (!columnsByName.containsKey(colName)) {
                    unrecognisedStrings.add(colName);
                }
            }
            // iterate over lines in CSV
            Map<String, String> csvRow;
            while ((csvRow = mapReader.read(header)) != null) {
                ImportData<M> data = extractData(csvRow, table);
                objectList.add(table.getFactory().construct(data, ObjectSource.RESTORE));
            }
        }
        System.out.println("Warning: unknown columns: " + unrecognisedStrings);
        return objectList;
    }
    public static <M extends MacrosPersistable<M>> void writeObjectsToCsv(Table<M> table, Writer csvOut, Collection<M> objects) throws IOException {
        final String[] header = table.columnsByName().keySet().toArray(new String[0]);
        try (ICsvMapWriter mapWriter = getMapWriter(csvOut)) {
            // header columns are used as the keys to the Map
            mapWriter.writeHeader(header);
            // iterate over lines in CSV
            for (M object : objects) {
                Map<String, String> dataStrings = prepareDataForExport(object.getAllData());
                mapWriter.write(dataStrings, header);
            }
        }
    }

    // don't edit csvRow keyset!
    private static <M> ImportData<M> extractData(Map<String, String> csvRow, Table<M> table) {
        Set<String> relevantCols = new HashSet<>(csvRow.keySet());
        relevantCols.retainAll(table.columnsByName().keySet());
        ImportData<M> data = new ImportData<>(table);
        for (String colName: relevantCols) {
            String value = csvRow.get(colName);
            Column<M, ?> col = table.columnForName(colName);
            // map empty strings in CSV to null
            data.putFromNullableString(col, value);
        }
        return data;
    }

    // don't edit keyset!
    private static <M> Map<String, String> prepareDataForExport(ColumnData<M> data) {
        Map<String, String> dataMap = new HashMap<>();
        for (Map.Entry<String, Column<M, ?>> entry: data.getTable().columnsByName().entrySet()) {
            // null data gets mapped to empty string
            String value = data.getAsString(entry.getValue());
            dataMap.put(entry.getKey(), value);
        }
        return dataMap;
    }

    private static ICsvMapReader getMapReader(Reader r) {
        // EXCEL_PREFERENCE sets newline character to '\n', quote character to '"' and delimiter to ','
        return new CsvMapReader(r, CsvPreference.EXCEL_PREFERENCE);
    }
    private static ICsvMapWriter getMapWriter(Writer w) {
        // EXCEL_PREFERENCE sets newline character to '\n', quote character to '"' and delimiter to ','
        return new CsvMapWriter(w, CsvPreference.EXCEL_PREFERENCE);
    }

    // Returns map of food index name to parsed food and nutrition columnData objects
    private static Map<String, Pair<ImportData<Food>, ImportData<NutritionData>>> getFoodData(Reader foodCsv) throws IOException {
        Map<String, Pair<ImportData<Food>, ImportData<NutritionData>>> data = new HashMap<>();
        try (ICsvMapReader mapReader = getMapReader(foodCsv)) {
            final String[] header = mapReader.getHeader(true);
            Map<String, String> csvRow;
            while ((csvRow = mapReader.read(header)) != null) {
                ImportData<Food> foodData = extractData(csvRow, Schema.FoodTable.instance());
                ImportData<NutritionData> ndData = extractData(csvRow, Schema.NutritionDataTable.instance());
                data.put(foodData.get(Schema.FoodTable.INDEX_NAME), new Pair<>(foodData, ndData));
            }
        }
        return data;
    }

    // map from composite food index name to list of ingredients
    private static Map<String, List<ImportData<Ingredient>>> getIngredientData(Reader ingredientCsv) throws IOException {
        Map<String, List<ImportData<Ingredient>>> data = new HashMap<>();
        try (ICsvMapReader mapReader = getMapReader(ingredientCsv)) {
            final String[] header = mapReader.getHeader(true);
            Map<String, String> csvRow;
            while ((csvRow = mapReader.read(header)) != null) {
                // XXX CSV contains food index names, while the DB wants food IDs - how to convert?????
                ImportData<Ingredient> ingredientData = extractData(csvRow, Schema.IngredientTable.instance());
                data.add(ingredientData);
                String compositeFoodIndexName = csvRow.get("recipe_index_name");
                String ingredientFoodIndexName = csvRow.get("ingredient_index_name");
                // TODO what now???? :(((
            }
        }
        return data;

    }

    // creates Composite food objects with ingredients lists (all with no IDs), but the ingredients are raw
    // (don't have linked food objects of their own)
    //
    static Map<String, CompositeFood> buildCompositeFoodObjectTree(Reader recipeCsv, Reader ingredientsCsv) throws IOException {
        Map<String, CompositeFood> foodMap = new HashMap<>();
        Map<String, Pair<ImportData<Food>, ImportData<NutritionData>>> recipeData = getFoodData(recipeCsv);
        // nutrition data may not be complete, so we can't create it yet. Just create the foods
        for (Pair<ImportData<Food>, ImportData<NutritionData>> rowData : recipeData.values()) {
            ImportData<Food> foodData = rowData.first;
            foodData.put(Schema.FoodTable.FOOD_TYPE, FoodType.COMPOSITE.getName());
            Food f = Food.factory().construct(foodData, ObjectSource.IMPORT);
            assert f instanceof CompositeFood;

            if (foodMap.containsKey(f.getIndexName())) {
                // TODO make this nicer
                throw new RuntimeException("Imported recipes contained duplicate index name: " + f.getIndexName());
            }
            foodMap.put(f.getIndexName(), (CompositeFood)f);
        }

        for (ImportData<Ingredient> ingredientImportData : getIngredientData(ingredientsCsv).values()) {
            Ingredient i = Ingredient.factory().construct(ingredientImportData, ObjectSource.IMPORT);
            CompositeFood recipe = foodMap.get(i.)

        }
    }

    // returns a pair of maps from food index name to corresponding food objects and nutrition data objects respectively
    // TODO can probably refactor this to just return one food
    static Map<String, Food> buildFoodObjectTree(Reader foodCsv) throws IOException {
        Map<String, Food> foodMap = new HashMap<>();
        for (Pair<ImportData<Food>, ImportData<NutritionData>> rowData : getFoodData(foodCsv).values()) {
            ImportData<Food> foodData = rowData.first;
            ImportData<NutritionData> ndData = rowData.second;
            Food f = Food.factory().construct(foodData, ObjectSource.IMPORT);
            NutritionData nd = NutritionData.factory().construct(ndData, ObjectSource.IMPORT);
            f.setNutritionData(nd); //without pairs, needed to recover nutrition data from return value
            nd.setFkParentNaturalKey(Schema.NutritionDataTable.FOOD_ID, Schema.FoodTable.INDEX_NAME, f);
            if (foodMap.containsKey(f.getIndexName())) {
                // TODO make this nicer
                throw new RuntimeException("Imported foods contained duplicate index name: " + f.getIndexName());
            }
            foodMap.put(f.getIndexName(), f);
        }
        return foodMap;
    }

    static List<Serving> buildServings(Reader servingCsv) throws IOException {
        List<Serving> servings = new ArrayList<>();
        try (ICsvMapReader mapReader = getMapReader(servingCsv)) {
            final String[] header = mapReader.getHeader(true);
            Map<String, String> csvRow;
            while ((csvRow = mapReader.read(header)) != null) {
                ImportData<Serving> servingData = extractData(csvRow, Schema.ServingTable.instance());
                String foodIndexName = csvRow.get(Schema.FoodTable.INDEX_NAME.sqlName());
                Serving s = Serving.factory().construct(servingData, ObjectSource.IMPORT);
                s.setFkParentNaturalKey(Schema.ServingTable.FOOD_ID, Schema.FoodTable.INDEX_NAME, foodIndexName);
                servings.add(s);
            }
        }
        return servings;
    }

    public static Collection<Food> importFoodData(Reader foodCsv, MacrosDatabase db, boolean allowOverwrite) throws IOException, SQLException {
        Map<String, Food> csvFoods = buildFoodObjectTree(foodCsv);
        // collect all of the index names to be imported, and check if they're already in the DB.
        Set<String> newIndexNames = csvFoods.keySet();
        List<String> existingIndexNames = db.selectColumn(Food.table(), Schema.FoodTable.INDEX_NAME, Schema.FoodTable.INDEX_NAME, newIndexNames, false);
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
        Map<String, NutritionData> ndObjects = new HashMap<>(newIndexNames.size(), 1);
        for (Food f : csvFoods.values()) {
            ndObjects.put(f.getIndexName(), f.getNutritionData());
        }
        if (!existingIndexNames.isEmpty()) {
            System.out.println("The following foods will be imported; others had index names already present in the database:");
            for (String indexName : newIndexNames) {
                System.out.println(indexName);
            }
        }
        db.saveObjects(csvFoods.values(), ObjectSource.IMPORT);
        List<NutritionData> completedNd = db.completeForeignKeys(ndObjects.values(), Schema.NutritionDataTable.FOOD_ID);
        db.saveObjects(completedNd, ObjectSource.IMPORT);
        return csvFoods.values();
    }

    // TODO detect existing servings
    public static void importServings(Reader servingCsv, MacrosDatabase db, boolean allowOverwrite) throws IOException, SQLException {
        List<Serving> csvServings = CsvStorage.buildServings(servingCsv);
        List<Serving> completedServings = db.completeForeignKeys(csvServings, Schema.ServingTable.FOOD_ID);
        db.saveObjects(completedServings, ObjectSource.IMPORT);
    }

}
