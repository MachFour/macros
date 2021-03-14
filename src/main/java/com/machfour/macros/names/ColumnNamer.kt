package com.machfour.macros.names

import com.machfour.macros.core.Column
import com.machfour.macros.entities.Nutrient

/*
 * Interface to provide user-readable names (strings) for nutrition data columns
 */
interface ColumnNamer {
    fun getName(col: Column<*, *>): String
    fun getAbbreviatedName(col: Column<*, *>): String = getName(col)

    fun getName(n: Nutrient): String
    fun getAbbreviatedName(n: Nutrient): String = getName(n)
}