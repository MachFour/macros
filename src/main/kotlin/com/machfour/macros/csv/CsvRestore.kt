package com.machfour.macros.csv

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.queries.saveObjects
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.datatype.TypeCastException
import java.io.IOException
import java.io.InputStream
import java.io.Reader
import java.sql.SQLException
import java.util.zip.ZipException
import java.util.zip.ZipInputStream

// Method for reading CSV files that directly correspond to a table
@Throws(IOException::class, TypeCastException::class)
private fun <M> buildObjectsForRestore(table: Table<M>, csvData: Reader): List<M> {
    val csvMapReader = getCsvMapReader(csvData)
    
    // header columns are used as the keys to the Map
    val header: Array<String>? = csvMapReader.getHeader(true)

    return if (header != null) {
        val unrecognisedColumns = header.filterNot { table.columnsByName.containsKey(it) }
        println("Warning: unknown columns: $unrecognisedColumns")

        ArrayList<M>().apply {
            while (true) {
                val nextRow = csvMapReader.read(*header) ?: break
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
@Throws(SQLException::class, IOException::class, TypeCastException::class)
fun <M : MacrosEntity<M>> SqlDatabase.restoreTable(t: Table<M>, csvData: Reader) {
    val objects = buildObjectsForRestore(t, csvData)
    saveObjects(this, objects, ObjectSource.RESTORE)
}

@Throws(SQLException::class, IOException::class, TypeCastException::class, ZipException::class)
fun SqlDatabase.restoreFromZip(zipInput: InputStream) {
    ZipInputStream(zipInput).use { zip ->
        val reader = zip.reader()
        while (true) {
            val entry = zip.nextEntry ?: break
            //Log.d(TAG, "doCsvRestore(): found entry in zip: ${entry.name}")
            if (entry.comment != zipEntryComment) {
                println("Entry '${entry.name}' is not commented with '$zipEntryComment', skipping")
                continue
            }
            val table = csvZipBackupTables.find { entry.name == it.backupName }
            if (table != null) {
                println("Entry '${entry.name}' does not match a backup table name, skipping")
                restoreTable(table, reader)
            }
        }
    }

}