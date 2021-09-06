package com.machfour.macros.csv

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.queries.MacrosDataSource
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.datatype.TypeCastException
import java.io.IOException
import java.io.Reader
import java.sql.SQLException

// Method for reading CSV files that directly correspond to a table
@Throws(IOException::class, TypeCastException::class)
private fun <M> buildObjectsForRestore(table: Table<M>, csvData: Reader): List<M> {
    val columnsByName = table.columnsByName
    val objectList = ArrayList<M>()
    val unrecognisedColumns = HashSet<String>()
    getCsvMapReader(csvData).use { reader ->
        // header columns are used as the keys to the Map
        val header: Array<String>? = reader.getHeader(true)
        if (header == null) {
            println("Warning: EOF encountered when reading header")
            return emptyList()
        }
        // store unrecognised header columns in map
        header.filterNotTo(unrecognisedColumns) { columnsByName.containsKey(it) }

        while(true) {
            val nextRow = reader.read(*header) ?: break
            val data = extractCsvData(nextRow, table)
            objectList += table.factory.construct(data, ObjectSource.RESTORE)
        }
    }
    println("Warning: unknown columns: $unrecognisedColumns")
    return objectList
}

// Adds the content of the CSV data to the table. Any conflicts in IDs will cause the operation to fail
@Throws(SQLException::class, IOException::class, TypeCastException::class)
fun <M : MacrosEntity<M>> restoreTable(ds: MacrosDataSource, t: Table<M>, csvData: Reader) {
    val objects = buildObjectsForRestore(t, csvData)
    ds.saveObjects(objects, ObjectSource.RESTORE)
}