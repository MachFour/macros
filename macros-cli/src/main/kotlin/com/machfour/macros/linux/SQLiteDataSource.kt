package com.machfour.macros.linux

import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import java.nio.file.Paths

internal fun makeSQLiteDataSource(dbFile: String): SQLiteDataSource {
    val dbPath = when(dbFile) {
        "", ":memory:" -> dbFile // in-memory database
        else -> Paths.get(dbFile).toAbsolutePath()
    }
    return SQLiteDataSource().apply {
        url = "jdbc:sqlite:${dbPath}"
        config = makeDefaultConfig()
    }
}

internal fun makeDefaultConfig(): SQLiteConfig {
    return SQLiteConfig().apply {
        enableRecursiveTriggers(true)
        enforceForeignKeys(true)
    }
}

