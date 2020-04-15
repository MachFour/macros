package com.machfour.macros.storage;

import com.machfour.macros.core.Column;
import com.machfour.macros.core.MacrosPersistable;
import com.machfour.macros.core.ObjectSource;
import com.machfour.macros.core.Table;
import org.supercsv.io.ICsvMapReader;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.*;

public class CsvRestore {

    /*
     * Method for reading CSV files that directly correspond to a table
     * 'out' is for printing error messages
     */
    public static <M> List<M> buildObjectsForRestore(Table<M> table, Reader csvData, PrintStream out) throws IOException {
        Map<String, Column<M, ?>> columnsByName = table.columnsByName();
        List<M> objectList = new ArrayList<>();
        Set<String> unrecognisedStrings = new HashSet<>();

        try (ICsvMapReader mapReader = CsvImport.getMapReader(csvData)) {
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
                ImportData<M> data = CsvImport.extractData(csvRow, table);
                objectList.add(table.getFactory().construct(data, ObjectSource.RESTORE));
            }
        }
        out.println("Warning: unknown columns: " + unrecognisedStrings);
        return objectList;
    }


    public static <M extends MacrosPersistable<M>> void restoreTable(Table<M> t, Reader csvData, MacrosDatabase db, PrintStream out)
            throws SQLException, IOException {
        List<M> objects = buildObjectsForRestore(t, csvData, out);
        db.saveObjects(objects, ObjectSource.RESTORE);
    }
}
