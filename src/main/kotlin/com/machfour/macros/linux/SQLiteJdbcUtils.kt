package com.machfour.macros.linux

import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.createSqlStatements
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import java.io.BufferedReader
import java.io.IOException
import java.io.Reader
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

internal fun java.sql.SQLException.wrapAsNativeException(): SqlException {
    val message = buildString {
        message?.let { append("it\n") }
        sqlState?.let { append("SQLState ($it) ") }
        append("vendor code (${errorCode})")
    }
    return SqlException(message, cause)
}

internal fun IOException.wrapAsNativeException(): SqlException {
    return SqlException("[IO Exception] $message", cause)
}

@Throws(SqlException::class)
internal fun readSqlStatements(r: Reader, lineSep: String = " "): String {
    try {
        BufferedReader(r).use { reader ->
            return createSqlStatements(reader::readLine, reader::ready, lineSep)
        }
    } catch (e: IOException) {
        throw e.wrapAsNativeException()
    }
}

