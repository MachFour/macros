package com.machfour.macros.names

import com.machfour.macros.objects.inbuilt.Units
import com.machfour.macros.objects.Unit

/*
 * Singleton pattern
 */
class EnglishUnitNames private constructor(): UnitNamer {
    companion object {
        private val AVAILABLE_UNITS = listOf(
            Units.GRAMS
            , Units.MILLIGRAMS
            , Units.MILLILITRES
            , Units.KILOJOULES
            , Units.CALORIES
        )

        val instance: EnglishUnitNames = EnglishUnitNames()
    }

    override fun getName(unit: Unit): String = unit.name
    override fun getAbbr(unit: Unit): String = unit.abbr
    override val availableUnits: List<Unit> = AVAILABLE_UNITS
}