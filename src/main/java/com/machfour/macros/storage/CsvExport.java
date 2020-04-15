package com.machfour.macros.storage;

import com.machfour.macros.core.*;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

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
        Map<String, String> dataMap = new LinkedHashMap<>(); // preserve column index order
        for (Column<M, ?> col: data.getTable().columns()) {
            // null data gets mapped to empty string
            String value = data.getAsString(col);
            dataMap.put(col.sqlName(), value);
        }
        return dataMap;
    }

    private static ICsvMapWriter getMapWriter(Writer w) {
        // EXCEL_PREFERENCE sets newline character to '\n', quote character to '"' and delimiter to ','
        return new CsvMapWriter(w, CsvPreference.EXCEL_PREFERENCE);
    }

    public static <M extends MacrosPersistable<M>> void exportTable(Table<M> t, Writer outCsv, MacrosDatabase db) throws SQLException, IOException {
        // TODO do we need to get raw objects? This method should probably be protected...
        Map<Long, M> rawObjectMap = db.getAllRawObjects(t);
        List<M> allRawObjects = new ArrayList<>(rawObjectMap.values());
        // Collections.sort(allRawFoods, Comparator.comparingLong(MacrosPersistable::getId));
        writeObjectsToCsv(t, outCsv, allRawObjects);
    }
}

