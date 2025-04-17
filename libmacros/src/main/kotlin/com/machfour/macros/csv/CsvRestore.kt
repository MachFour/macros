package com.machfour.macros.csv

import com.machfour.ksv.CsvParseException
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.queries.saveObjects
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.datatype.TypeCastException

// Method for reading CSV files that directly correspond to a table
@Throws(CsvParseException::class, TypeCastException::class)
private fun <M> buildObjectsForRestore(table: Table<M>, csvData: String): List<M> {

    val csvRows = getCsvParser().parse(csvData)
    val header = csvRows.firstOrNull()

    return if (header != null) {
        val unrecognisedColumns = header.filterNot { table.columnsByName.containsKey(it) }
        println("Warning: unknown columns: $unrecognisedColumns")

        buildList {
            for (i in 1 until csvRows.size) {
                val column = csvRows[i]
                val nextRow = header.indices.associate { j -> header[j] to column[j] }
                val data = extractCsvData(nextRow, table)
                add(table.construct(data, ObjectSource.RESTORE))
            }
        }
    } else {
        println("Warning: EOF encountered when reading header")
        emptyList()
    }
}

// Adds the content of the CSV data to the table. Any conflicts in IDs will cause the operation to fail
@Throws(SqlException::class, CsvParseException::class, TypeCastException::class)
fun <M : MacrosEntity> SqlDatabase.restoreTable(t: Table<M>, csvData: String) {
    val objects = buildObjectsForRestore(t, csvData)
    saveObjects(this, t, objects, ObjectSource.RESTORE)
}
