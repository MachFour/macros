package com.machfour.macros.storage;

import com.machfour.macros.core.*;
import com.machfour.macros.objects.*;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

import static com.machfour.macros.core.Schema.FoodTable.INDEX_NAME;

public class CsvExport {
    static <M extends MacrosPersistable<M>> void writeObjectsToCsv(Table<M> table, Writer csvOut, Collection<M> objects) throws IOException {
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

    private static ICsvMapWriter getMapWriter(Writer w) {
        // EXCEL_PREFERENCE sets newline character to '\n', quote character to '"' and delimiter to ','
        return new CsvMapWriter(w, CsvPreference.EXCEL_PREFERENCE);
    }

    public static void exportFoods(Writer foodCsv, MacrosDatabase db) throws SQLException, IOException {
        // TODO do we need to get raw objects? This method should probably be protected...
        Map<Long, Food> rawFoodMap = db.getAllRawObjects(Food.table());
        List<Food> allRawFoods = new ArrayList<>(rawFoodMap.values());
        // Collections.sort(allRawFoods, Comparator.comparingLong(Food::getId));
        writeObjectsToCsv(Food.table(), foodCsv, allRawFoods);
    }

    public static void exportNutritionData(Writer nutritionDataCsv, MacrosDatabase db) throws SQLException, IOException {
        Map<Long, NutritionData> rawNdMap = db.getAllRawObjects(NutritionData.table());
        List<NutritionData> allRawFoods = new ArrayList<>(rawNdMap.values());
        // Collections.sort(allRawFoods, Comparator.comparingLong(NutritionData::getId));
        writeObjectsToCsv(NutritionData.table(), nutritionDataCsv, allRawFoods);

    }

    public static void exportServings(Writer servingCsv, MacrosDatabase db) throws SQLException, IOException {
        Map<Long, Serving> rawServingMap = db.getAllRawObjects(Serving.table());
        List<Serving> allRawServings = new ArrayList<>(rawServingMap.values());
        // Collections.sort(allRawServings, Comparator.comparingLong(Serving::getId));
        writeObjectsToCsv(Serving.table(), servingCsv, allRawServings);

    }

    public static void exportIngredients(Writer ingredientsCsv, MacrosDatabase db) throws SQLException, IOException {
        Map<Long, Ingredient> rawIngredientMap = db.getAllRawObjects(Ingredient.table());
        List<Ingredient> allRawIngredients = new ArrayList<>(rawIngredientMap.values());
        //Collections.sort(allRawIngredients, Comparator.comparingLong(Ingredient::getId));
        writeObjectsToCsv(Ingredient.table(), ingredientsCsv, allRawIngredients);
    }
}

