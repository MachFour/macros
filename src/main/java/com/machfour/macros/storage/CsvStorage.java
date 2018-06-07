package com.machfour.macros.storage;

import com.machfour.macros.core.*;
import com.machfour.macros.data.*;
import com.machfour.macros.util.Pair;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CsvStorage {
    public static final String FOOD_CSV_FILENAME = "/home/max/devel/macros/macros-data/foods.csv";
    public static final String SERVING_CSV_FILENAME = "/home/max/devel/macros/macros-data/foods.csv";

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
                objectList.add(MacrosEntity.construct(table, data, ObjectSource.RESTORE));
            }
        }
        System.out.println("Warning: unknown columns: " + unrecognisedStrings);
        return objectList;
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
            StorageUtils.nullableStringToColumnData(data, col, value);
        }
        return data;
    }

    private static ICsvMapReader getMapReader(String fileName) throws IOException {
        // EXCEL_PREFERENCE sets newline character to '\n', quote character to '"' and delimiter to ','
        return new CsvMapReader(new FileReader(fileName), CsvPreference.EXCEL_PREFERENCE);
    }

    /*
     * Ability to add custom field conversion methods when importing from user-generated CSV
     *  -> idea: user shouldn't have to worry about IDs. So for foreign key references in the user CSV,
     *     another key column is used instead of the ID. Then at import time we have to convert back to the ID.
     *
     * E.g. for foods:
     *  - create and link appropriate tags
     *  - look up quantity unit
     *  - look up category
     */
    public static Pair<List<Food>, List<NutritionData>> buildFoodObjectTree(String foodCsv) throws IOException {
        Pair<List<Food>, List<NutritionData>> objectLists = new Pair<>(new ArrayList<>(), new ArrayList<>());
        Table<NutritionData> ndTable = Schema.NutritionDataTable.instance();
        Table<Food> foodTable = Schema.FoodTable.instance();
        try (ICsvMapReader mapReader = getMapReader(foodCsv)) {
            final String[] header = mapReader.getHeader(true);
            Map<String, String> csvRow;
            while ((csvRow = mapReader.read(header)) != null) {
                ColumnData<NutritionData> ndData = extractData(csvRow, Schema.NutritionDataTable.instance());
                ColumnData<Food> foodData = extractData(csvRow, Schema.FoodTable.instance());
                NutritionData nd = MacrosEntity.construct(ndTable, ndData, ObjectSource.IMPORT);
                Food f = MacrosEntity.construct(foodTable, foodData, ObjectSource.IMPORT);
                //f.setNutritionData(nd); without pairs, needed to recover nutrition data from return value
                nd.setFkParentBy2aryKey(Schema.NutritionDataTable.FOOD_ID, f);
                objectLists.first.add(f);
                objectLists.second.add(nd);
            }
        }
        return objectLists;
    }
    public static List<Serving> buildServings(String servingCsv) throws IOException {
        List<Serving> servings = new ArrayList<>();
        Table<Serving> sTable = Schema.ServingTable.instance();
        try (ICsvMapReader mapReader = getMapReader(servingCsv)) {
            final String[] header = mapReader.getHeader(true);
            Map<String, String> csvRow;
            while ((csvRow = mapReader.read(header)) != null) {
                ColumnData<Serving> servingData = extractData(csvRow, Schema.ServingTable.instance());
                Serving s = MacrosEntity.construct(sTable, servingData, ObjectSource.IMPORT);
                String foodIndexName = csvRow.get(Schema.FoodTable.INDEX_NAME.sqlName());
                s.setFkParentBy2aryKey(Schema.ServingTable.FOOD_ID, Schema.FoodTable.instance(), Schema.FoodTable.INDEX_NAME, foodIndexName);
                servings.add(s);
            }
        }
        return servings;
    }
}
