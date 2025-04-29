package com.machfour.macros.sql

// Multiplatform SQL exception class
class SqlException(
    message: String? = null,
    cause: Throwable? = null,
    val code: Int? = null,
): RuntimeException(message ?: cause?.message, cause) {
    companion object {
        fun <M, J: Any> forTypeCastError(rawValue: Any?, c: Column<M, J>): SqlException {
            return SqlException(
                message = "Could not convert value '$rawValue' for column ${c.table}.${c.sqlName} (type ${c.type})",
            )
        }
    }
}