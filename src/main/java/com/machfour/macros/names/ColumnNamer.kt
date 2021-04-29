package com.machfour.macros.names

import com.machfour.macros.core.Column
import com.machfour.macros.entities.Nutrient

/*
 * Interface to provide user-readable names (strings) for nutrition data columns
 */
interface ColumnNamer {
    fun getFullName(col: Column<*, *>): String
    fun getAbbreviatedName(col: Column<*, *>): String

    fun getFullName(n: Nutrient): String
    fun getDisplayName(n: Nutrient): String
    fun getAbbreviatedName(n: Nutrient): String = getDisplayName(n)
}