package com.machfour.macros.names

import com.machfour.macros.objects.Unit

/*
 * Interface to provide user-readable strings for units used in nutrition data
 */
interface UnitNamer {
    fun getName(unit: Unit): String
    fun getAbbr(unit: Unit): String
    fun availableUnits(): Collection<Unit>
}