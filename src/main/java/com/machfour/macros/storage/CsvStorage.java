package com.machfour.macros.storage;

import com.machfour.macros.core.*;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.objects.Serving;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class CsvStorage {
    /*
     * Method for reading CSV files that directly correspond to a table
     */
    public static <M> List<M> buildObjectsForRestore(Table<M> table, String fileName) throws IOException {
        Map<String, Column<M, ?>> columnsByName = table.columnsByName();
        List<M> objectList = new ArrayList<>();
        Set<String> unrecognisedStrings = new HashSet<>();

        try (ICsvMapReader mapReader = getMapReader(fileName)) {
            // header columns are used as the keys to the Map
            final String[] header = mapReader.getHeader(true);
            for (String colName : header) {
                if (!columnsByName.keySet().contains(colName)) {
                    unrecognisedStrings.add(colName);
                }
            }
            // iterate over lines in CSV
            Map<String, String> csvRow;
            while ((csvRow = mapReader.read(header)) != null) {
                ColumnData<M> data = extractData(csvRow, table);
                objectList.add(table.getFactory().construct(data, ObjectSource.RESTORE));
            }
        }
        System.out.println("Warning: unknown columns: " + unrecognisedStrings);
        return objectList;
    }
    public static <M extends MacrosPersistable<M>> void writeObjectsToCsv(Table<M> table, String fileName, Collection<M> objects) throws IOException {
        final String[] header = table.columnsByName().keySet().toArray(new String[0]);
        try (ICsvMapWriter mapWriter = getMapWriter(fileName)) {
            // header columns are used as the keys to the Map
            mapWriter.writeHeader(header);
            // iterate over lines in CSV
            for (M object : objects) {
                Map<String, String> dataStrings = prepareDataForExport(object.getAllData());
                mapWriter.write(dataStrings, header);
            }
        }
    }

    // don't edit keyset!
    private static <M> ColumnData<M> extractData(Map<String, String> csvRow, Table<M> table) {
        Set<String> relevantCols = new HashSet<>(csvRow.keySet());
        relevantCols.retainAll(table.columnsByName().keySet());
        ColumnData<M> data = new ColumnData<>(table);
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
            String value = data.getAsNotNullString(entry.getValue());
            dataMap.put(entry.getKey(), value);
        }
        return dataMap;
    }

    private static ICsvMapReader getMapReader(String fileName) throws IOException {
        // EXCEL_PREFERENCE sets newline character to '\n', quote character to '"' and delimiter to ','
        return new CsvMapReader(new FileReader(fileName), CsvPreference.EXCEL_PREFERENCE);
    }
    private static ICsvMapWriter getMapWriter(String fileName) throws IOException {
        // EXCEL_PREFERENCE sets newline character to '\n', quote character to '"' and delimiter to ','
        return new CsvMapWriter(new FileWriter(fileName), CsvPreference.EXCEL_PREFERENCE);
    }

    // returns a pair of maps from food index name to corresponding food objects and nutrition data objects respectively
    // TODO can probably refactor this to just return one food
    public static Map<String, Food> buildFoodObjectTree(String foodCsv) throws IOException {
        Map<String, Food> foodMap = new HashMap<>();
        try (ICsvMapReader mapReader = getMapReader(foodCsv)) {
            final String[] header = mapReader.getHeader(true);
            Map<String, String> csvRow;
            while ((csvRow = mapReader.read(header)) != null) {
                ColumnData<Food> foodData = extractData(csvRow, Schema.FoodTable.instance());
                Food f = Food.factory().construct(foodData, ObjectSource.IMPORT);

                ColumnData<NutritionData> ndData = extractData(csvRow, Schema.NutritionDataTable.instance());
                NutritionData nd = NutritionData.factory().construct(ndData, ObjectSource.IMPORT);

                f.setNutritionData(nd); //without pairs, needed to recover nutrition data from return value
                nd.setFkParentNaturalKey(Schema.NutritionDataTable.FOOD_ID, Schema.FoodTable.INDEX_NAME, f);
                if (foodMap.containsKey(f.getIndexName())) {
                    // TODO make this nicer
                    throw new RuntimeException("Imported foods contained two foods with the same index name: " + f.getIndexName());
                }
                foodMap.put(f.getIndexName(), f);
            }
        }
        return foodMap;
    }
    public static List<Serving> buildServings(String servingCsv) throws IOException {
        List<Serving> servings = new ArrayList<>();
        try (ICsvMapReader mapReader = getMapReader(servingCsv)) {
            final String[] header = mapReader.getHeader(true);
            Map<String, String> csvRow;
            while ((csvRow = mapReader.read(header)) != null) {
                ColumnData<Serving> servingData = extractData(csvRow, Schema.ServingTable.instance());
                Serving s = Serving.factory().construct(servingData, ObjectSource.IMPORT);
                String foodIndexName = csvRow.get(Schema.FoodTable.INDEX_NAME.sqlName());
                s.setFkParentNaturalKey(Schema.ServingTable.FOOD_ID, Schema.FoodTable.INDEX_NAME, foodIndexName);
                servings.add(s);
            }
        }
        return servings;
    }

    public static Collection<Food> importFoodData(String foodCsv, MacrosDatabase db, boolean allowOverwrite) throws IOException, SQLException {
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
    public static void importServings(String servingCsv, MacrosDatabase db, boolean allowOverwrite) throws IOException, SQLException {
        List<Serving> csvServings = CsvStorage.buildServings(servingCsv);
        List<Serving> completedServings = db.completeForeignKeys(csvServings, Schema.ServingTable.FOOD_ID);
        db.saveObjects(completedServings, ObjectSource.IMPORT);
    }

}
