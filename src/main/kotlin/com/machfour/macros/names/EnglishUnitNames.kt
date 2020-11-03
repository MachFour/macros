package com.machfour.macros.names

import com.machfour.macros.objects.Units
import com.machfour.macros.objects.IUnit

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

    override fun getName(unit: IUnit): String = unit.name
    override fun getAbbr(unit: IUnit): String = unit.abbr
    override val availableUnits: List<IUnit> = AVAILABLE_UNITS
}