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
    return SqlException(message = message, cause = cause, code = errorCode)
}

fun IOException.wrapAsNativeException(): SqlException {
    return SqlException("[IO Exception] $message", cause)
}

private fun BufferedReader.getLines() = generateSequence {
    if (ready()) readLine() else null
}

@Throws(SqlException::class)
fun Reader.readSqlStatements(lineSep: String = " "): String {
    try {
        return BufferedReader(this).use {
            createSqlStatements(lines = it.getLines(), lineSep = lineSep)
        }
    } catch (e: IOException) {
        throw e.wrapAsNativeException()
    }
}

@Throws(SqlException::class)
fun Reader.readSplitSqlStatements(): List<String> {
    try {
        BufferedReader(this).use {
            return createSplitSqlStatements(it.getLines())
        }
    } catch (e: IOException) {
        throw e.wrapAsNativeException()
    }
}
