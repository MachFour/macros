package com.machfour.macros.names

import com.machfour.macros.objects.IUnit

/*
 * Interface to provide user-readable strings for units used in nutrition data
 */
interface UnitNamer {
    fun getName(unit: IUnit): String
    fun getAbbr(unit: IUnit): String
    val availableUnits: Collection<IUnit>
}