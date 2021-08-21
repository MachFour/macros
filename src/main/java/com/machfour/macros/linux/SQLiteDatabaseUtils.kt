package com.machfour.macros.linux

import java.nio.file.Paths
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource

object SQLiteDatabaseUtils {
    @Suppress("NewApi")
    fun makeSQLiteDataSource(dbFile: String): SQLiteDataSource {
        val dbPath = Paths.get(dbFile).toAbsolutePath()
        return SQLiteDataSource().apply {
            url = "jdbc:sqlite:${dbPath}"
            config = SQLiteConfig().apply {
                enableRecursiveTriggers(true)
                enforceForeignKeys(true)
            }
        }
    }
}