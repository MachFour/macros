package com.machfour.macros.names

import com.machfour.macros.objects.EnergyUnit
import com.machfour.macros.objects.QtyUnits
import com.machfour.macros.objects.Unit

/*
 * Singleton pattern
 */
class EnglishUnitNames private constructor(): UnitNamer {
    override fun getName(unit: Unit): String = unit.name
    override fun getAbbr(unit: Unit): String = unit.abbr
    override fun availableUnits(): List<Unit> = AVAILABLE_UNITS

    companion object {
        private val AVAILABLE_UNITS = listOf(
              QtyUnits.GRAMS
            , QtyUnits.MILLIGRAMS
            , QtyUnits.MILLILITRES
            , EnergyUnit.KILOJOULES
            , EnergyUnit.CALORIES
        )

        @JvmStatic
        val instance: EnglishUnitNames = EnglishUnitNames()
    }
}