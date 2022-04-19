package com.machfour.macros.jvm

import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.createSplitSqlStatements
import com.machfour.macros.sql.createSqlStatements
import java.io.BufferedReader
import java.io.IOException
import java.io.Reader

fun java.sql.SQLException.wrapAsNativeException(): SqlException {
    val message = buildString {
        message?.let { append("$it\n") }
        sqlState?.let { append("SQLState ($it) ") }
        append("vendor code (${errorCode})")
    }
    return SqlException(message, cause)
}

fun IOException.wrapAsNativeException(): SqlException {
    return SqlException("[IO Exception] $message", cause)
}

@Throws(SqlException::class)
fun readSqlStatements(r: Reader, lineSep: String = " "): String {
    try {
        BufferedReader(r).use { reader ->
            return createSqlStatements(reader::readLine, reader::ready, lineSep)
        }
    } catch (e: IOException) {
        throw e.wrapAsNativeException()
    }
}

@Throws(SqlException::class)
fun readSplitSqlStatements(r: Reader, lineSep: String = " "): List<String> {
    try {
        BufferedReader(r).use { reader ->
            return createSplitSqlStatements(reader::readLine, reader::ready)
        }
    } catch (e: IOException) {
        throw e.wrapAsNativeException()
    }
}
