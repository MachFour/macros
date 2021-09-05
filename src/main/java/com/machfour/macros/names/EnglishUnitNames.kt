package com.machfour.macros.names

import com.machfour.macros.entities.Unit
import com.machfour.macros.units.*

object EnglishUnitNames: UnitStrings {

    override fun getName(unit: Unit): String = unit.name
    override fun getAbbr(unit: Unit): String = unit.abbr
    
    override val availableUnits: List<Unit> = listOf(
        GRAMS,
        MILLIGRAMS,
        MILLILITRES,
        KILOJOULES,
        CALORIES,
        OUNCES,
        FLUID_OUNCES,
    )
}