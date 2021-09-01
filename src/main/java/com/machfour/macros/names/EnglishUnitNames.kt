package com.machfour.macros.names

import com.machfour.macros.units.Units
import com.machfour.macros.entities.Unit

object EnglishUnitNames: UnitStrings {

    override fun getName(unit: Unit): String = unit.name
    override fun getAbbr(unit: Unit): String = unit.abbr
    
    override val availableUnits: List<Unit> = listOf(
        Units.GRAMS,
        Units.MILLIGRAMS,
        Units.MILLILITRES,
        Units.KILOJOULES,
        Units.CALORIES,
        Units.OUNCES,
        Units.FLUID_OUNCES,
    )
}