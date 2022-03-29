package com.machfour.macros.sql

// Multiplatform SQL exception class
class SqlException(message: String? = null, cause: Throwable? = null): RuntimeException(message, cause) {
    companion object {
        fun <M, J> forTypeCastError(rawValue: Any?, c: Column<M, J>): SqlException {
            return SqlException("Could not convert value '$rawValue' for column ${c.table}.${c.sqlName} (type ${c.type})")
        }
    }
}