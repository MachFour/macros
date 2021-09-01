package com.machfour.macros.names

import com.machfour.macros.entities.Unit

/*
 * Interface to provide user-readable strings for units used in nutrition data
 */
interface UnitStrings {
    fun getName(unit: Unit): String
    fun getAbbr(unit: Unit): String
    val availableUnits: Collection<Unit>
}