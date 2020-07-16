package com.machfour.macros.names

import com.machfour.macros.core.Column

/*
 * Interface to provide user-readable names (strings) for nutrition data columns
 */
interface ColumnNamer {
    fun getName(col: Column<*, *>): String

    // JVM default
    fun getAbbreviatedName(col: Column<*, *>): String {
        return getName(col)
    }
}