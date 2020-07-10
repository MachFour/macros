package com.machfour.macros.storage;

import com.machfour.macros.core.Column;
import com.machfour.macros.core.MacrosEntity;
import com.machfour.macros.core.ObjectSource;
import com.machfour.macros.core.Table;
import com.machfour.macros.core.datatype.TypeCastException;
import com.machfour.macros.queries.Queries;

import org.supercsv.io.ICsvMapReader;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CsvRestore {

    /*
     * Method for reading CSV files that directly correspond to a table
     * 'out' is for printing error messages
     */
    public static <M> List<M> buildObjectsForRestore(Table<M> table, Reader csvData, PrintStream out)
            throws IOException, TypeCastException {
        Map<String, Column<M, ?>> columnsByName = table.getColumnsByName();
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


    public static <M extends MacrosEntity<M>> void restoreTable(Table<M> t, Reader csvData, MacrosDataSource db, PrintStream out)
            throws SQLException, IOException, TypeCastException {
        List<M> objects = buildObjectsForRestore(t, csvData, out);
        Queries.saveObjects(db, objects, ObjectSource.RESTORE);
    }
}
