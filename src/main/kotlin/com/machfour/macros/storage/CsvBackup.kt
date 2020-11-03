package com.machfour.macros.storage

import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.Table
import org.supercsv.io.CsvMapWriter
import org.supercsv.io.ICsvMapWriter
import org.supercsv.prefs.CsvPreference
import java.io.IOException
import java.io.Writer
import java.sql.SQLException

object CsvBackup {
    @Throws(IOException::class)
    fun <M : MacrosEntity<M>> writeObjectsToCsv(table: Table<M>, csvOut: Writer, objects: Collection<M>) {
        val header = table.columnsByName.keys.toTypedArray()
        getMapWriter(csvOut).use { writer ->
            // header columns are used as the keys to the Map
            writer.writeHeader(*header)
            // iterate over objects, each one becomes 1 line of CSV
            objects.forEach {
                val dataStrings = prepareDataForExport(it.allData)
                writer.write(dataStrings, *header)
            }
        }
    }

    // don't edit keyset!
    private fun <M> prepareDataForExport(data: ColumnData<M>): Map<String, String> {
        return LinkedHashMap<String, String>().apply {
            data.table.columns.forEach { col ->
                // null data gets mapped to empty string
                this[col.sqlName] = data.getAsRawString(col)
            }
        }
    }

    // EXCEL_PREFERENCE sets newline character to '\n', quote character to '"' and delimiter to ','
    private fun getMapWriter(w: Writer): ICsvMapWriter = CsvMapWriter(w, CsvPreference.EXCEL_PREFERENCE)

    @Throws(SQLException::class, IOException::class)
    fun <M : MacrosEntity<M>> exportTable(db: MacrosDataSource, t: Table<M>, outCsv: Writer) {
        // TODO do we need to get raw objects? This method should probably be protected...
        val rawObjectMap = db.getAllRawObjects(t)
        val allRawObjects: List<M> = ArrayList(rawObjectMap.values)
        // Collections.sort(allRawFoods, Comparator.comparingLong(MacrosPersistable::getId));
        writeObjectsToCsv(t, outCsv, allRawObjects)
    }
}