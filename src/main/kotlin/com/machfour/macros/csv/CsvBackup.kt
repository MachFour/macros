package com.machfour.macros.csv

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.queries.getAllRawObjects
import com.machfour.macros.schema.*
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.Table
import org.supercsv.io.CsvMapWriter
import org.supercsv.io.ICsvMapWriter
import org.supercsv.prefs.CsvPreference
import java.io.IOException
import java.io.OutputStream
import java.io.Writer
import java.sql.SQLException
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipOutputStream

// don't edit keyset!
private fun <M> prepareDataForBackup(data: RowData<M>): Map<String, String> {
    return buildMap {
        data.table.columns.forEach { col ->
            // null data gets mapped to empty string
            put(col.sqlName, data.getAsRawString(col))
        }
    }
}

// EXCEL_PREFERENCE sets newline character to '\n', quote character to '"' and delimiter to ','
internal fun getMapWriter(w: Writer): ICsvMapWriter = CsvMapWriter(w, CsvPreference.EXCEL_PREFERENCE)

@Throws(IOException::class)
fun <M : MacrosEntity<M>> writeObjectsToCsv(table: Table<M>, csvOut: Writer, objects: Collection<M>) {
    val header = table.columnsByName.keys.toTypedArray()
    val csvMapWriter = getMapWriter(csvOut)

    // header columns are used as the keys to the Map
    csvMapWriter.writeHeader(*header)
    // iterate over objects, each one becomes 1 line of CSV
    objects.forEach {
        val dataStrings = prepareDataForBackup(it.data)
        csvMapWriter.write(dataStrings, *header)
    }
    csvMapWriter.flush()
}

// listed in dependency order (later tables have foreign keys referring to previous ones)
internal val csvZipBackupTables: List<Table<out MacrosEntity<*>>> = listOf(
    UnitTable,
    NutrientTable,
    FoodTable,
    FoodNutrientValueTable,
    ServingTable,
    MealTable,
    FoodPortionTable,
    IngredientTable,
)

internal const val zipEntryComment = "MacrosDBBackup"

// returns app specific data directory
internal val <M> Table<M>.backupName: String
    get() = "$name.csv"

@Throws(SQLException::class, IOException::class)
fun <M : MacrosEntity<M>> SqlDatabase.exportTableToCsv(t: Table<M>, outCsv: Writer) {
    val rawObjectMap = getAllRawObjects(this, t)
    val allRawObjects: List<M> = ArrayList(rawObjectMap.values)
    writeObjectsToCsv(t, outCsv, allRawObjects)
}


@Throws(IOException::class, SQLException::class, ZipException::class)
fun SqlDatabase.createZipBackup(zipFileOut: OutputStream) {
    ZipOutputStream(zipFileOut).use { zip ->
        val writer = zip.writer()
        for (table in csvZipBackupTables) {
            val entry = ZipEntry(table.backupName).apply { comment = zipEntryComment }
            zip.putNextEntry(entry)
            exportTableToCsv(table, writer)
            zip.closeEntry()
        }
        zip.finish()
        zip.close()
    }
}
