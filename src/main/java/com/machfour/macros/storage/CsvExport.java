package com.machfour.macros.storage;


import com.machfour.macros.core.*;
import com.machfour.macros.objects.*;
import com.machfour.macros.util.Pair;
import com.machfour.macros.validation.SchemaViolation;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

import static com.machfour.macros.core.Schema.FoodTable.INDEX_NAME;

public class CsvExport {
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
}
