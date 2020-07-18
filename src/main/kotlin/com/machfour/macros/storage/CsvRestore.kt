package com.machfour.macros.storage

import com.machfour.macros.core.*
import com.machfour.macros.core.datatype.TypeCastException
import com.machfour.macros.queries.Queries
import java.io.IOException
import java.io.PrintStream
import java.io.Reader
import java.sql.SQLException

object CsvRestore {
    /*
     * Method for reading CSV files that directly correspond to a table
     * 'out' is for printing error messages
     */
    @Throws(IOException::class, TypeCastException::class)
    private fun <M> buildObjectsForRestore(table: Table<M>, csvData: Reader, out: PrintStream): List<M> {
        val columnsByName: Map<String, Column<M, *>> = table.columnsByName
        val objectList: MutableList<M> = ArrayList()
        val unrecognisedColumns: MutableSet<String> = HashSet()
        CsvImport.getMapReader(csvData).use { mapReader ->
            // header columns are used as the keys to the Map
            val header : Array<String>? = mapReader.getHeader(true)
            if (header == null) {
                out.println("Warning: EOF encountered when reading header")
                return emptyList()
            }
            // store unrecognised header columns in map
            header.filter { colName -> !columnsByName.containsKey(colName) }.forEach { unrecognisedColumns.add(it) }
            // iterate over lines in CSV
            var csvRow: Map<String, String>?
            while (mapReader.read(*header).also { csvRow = it } != null) {
                val data = CsvImport.extractData(csvRow!!, table)
                objectList += table.factory.construct(data, ObjectSource.RESTORE)
            }
        }
        out.println("Warning: unknown columns: $unrecognisedColumns")
        return objectList
    }

    @Throws(SQLException::class, IOException::class, TypeCastException::class)
    fun <M : MacrosEntity<M>> restoreTable(db: MacrosDataSource, t: Table<M>, csvData: Reader, out: PrintStream) {
        val objects = buildObjectsForRestore(t, csvData, out)
        Queries.saveObjects(db, objects, ObjectSource.RESTORE)
    }
}