package com.machfour.macros.names

import com.machfour.macros.sql.Column
import com.machfour.macros.entities.Nutrient

/*
 * Interface to provide user-readable names (strings) for nutrition data columns
 */
interface ColumnStrings {
    fun getFullName(col: Column<*, *>): String
    fun getAbbreviatedName(col: Column<*, *>): String
}