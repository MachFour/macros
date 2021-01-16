package com.machfour.macros.sql

enum class SqlQueryMode(val sql: String) {
    INSERT("INSERT"), SELECT("SELECT"), DELETE("DELETE"), UPDATE("UPDATE")
}

enum class OrderByDirection(val sql: String) {
    ASCENDING("ASC"), DESCENDING("DESC")
}

enum class OrderByNullPrecedence(val sql: String) {
    NULLS_FIRST("NULLS FIRST"), NULLS_LAST("NULLS LAST")
}

enum class Conjuction(val sql: String) {
    AND("AND"), OR("OR")
}
