package com.machfour.macros.jvm

import com.machfour.ksv.CsvParseException
import com.machfour.macros.csv.*
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.datatype.TypeCastException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@Throws(SqlException::class, ZipException::class)
fun SqlDatabase.createZipBackup(zipFileOut: OutputStream) {
    ZipOutputStream(zipFileOut).use { zip ->
        val writer = zip.writer()
        for (table in csvZipBackupTables) {
            val entry = ZipEntry(table.backupName).apply { comment = zipEntryComment }
            zip.putNextEntry(entry)
            writer.write(exportTableToCsv(table))
            zip.closeEntry()
        }
        zip.finish()
        zip.close()
    }
}

@Throws(SqlException::class, CsvParseException::class, IOException::class, TypeCastException::class, ZipException::class)
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
                restoreTable(table, reader.readText())
            }
        }
    }
}
