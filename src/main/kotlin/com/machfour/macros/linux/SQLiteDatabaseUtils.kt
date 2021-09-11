package com.machfour.macros.linux

import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import java.nio.file.Paths

@Suppress("NewApi")
internal fun makeSQLiteDataSource(dbFile: String): SQLiteDataSource {
    val dbPath = Paths.get(dbFile).toAbsolutePath()
    return SQLiteDataSource().apply {
        url = "jdbc:sqlite:${dbPath}"
        config = SQLiteConfig().apply {
            enableRecursiveTriggers(true)
            enforceForeignKeys(true)
        }
    }
}