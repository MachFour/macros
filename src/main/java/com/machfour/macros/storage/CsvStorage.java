package com.machfour.macros.storage;

import com.machfour.macros.data.Column;
import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;
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
     * General method for reading tables which were written programmatically.
     * This method can't handle implicit foreign keys in the data (i.e. referring to a non-ID key column)
     * if that is not what is specified in the SQL
     * See methods below for specialised importing of user-generated tables where foreign key columns are more friendly
     */

    public static <M> List<M> readObjects(Table<M> table, String fileName) throws IOException {
        Map<String, Column<M, ?, ?>> columnsByName = table.columnsByName();
        List<M> objectList = new ArrayList<>();
        Set<String> unrecognisedStrings = new HashSet<>();

        // EXCEL_PREFERENCE sets newline character to '\n', quote character to '"' and delimiter to ','
        try (ICsvMapReader mapReader = new CsvMapReader(new FileReader(fileName), CsvPreference.EXCEL_PREFERENCE)) {
            // header columns are used as the keys to the Map
            final String[] header = mapReader.getHeader(true);
            Map<String, String> csvStrings;
            // iterate over lines in CSV
            while ((csvStrings = mapReader.read(header)) != null) {
                ColumnData<M> convertedData = new ColumnData<>(table);
                for (String colName : csvStrings.keySet()) {
                    if (columnsByName.keySet().contains(colName)) {
                        Column<M, ?, ?> column = table.columnForName(colName);
                        // map empty strings in CSV to null
                        SqlUtils.nullableStringToColumnData(convertedData, column, csvStrings.get(colName));
                    } else {
                        unrecognisedStrings.add(colName);
                    }
                }
                objectList.add(table.construct(convertedData, false));
            }
        }
        for (String colName : unrecognisedStrings) {
            System.out.println("Warning: column '" + colName + "' not recognised for " + table.name() + " table");
        }
        return objectList;
    }

    /*
     * TODO Include specialised methods for importing foods, servings and ingredients from user-generated CSV
     */
}
