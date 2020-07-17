package com.machfour.macros.objects

import java.util.Arrays
import java.util.Collections
import java.util.HashMap

/*
 * Units for measuring quantities of food (only). Not for nutrition measurements.
 */
class EnergyUnit private constructor(
        // Full name of this Unit
        override val name: String,
        // Abbreviation: must be unique among different Units.
        override val abbr: String)
    : Unit {

    override fun toString(): String {
        return "$name ($abbr)"
    }

    companion object {
        val KILOJOULES: EnergyUnit = EnergyUnit("Kilojoules", "kJ")
        val CALORIES: EnergyUnit = EnergyUnit("Calories", "kcal")

        private val INBUILT: List<EnergyUnit>

        // maps lower case abbreviations to objects
        private val ABBREVIATION_MAP: Map<String, EnergyUnit>

        init {
            INBUILT = Collections.unmodifiableList(listOf(KILOJOULES, CALORIES))

            val abbreviationMap = HashMap<String, EnergyUnit>(INBUILT.size, 1.0f)
            for (eu in INBUILT) {
                abbreviationMap[eu.abbr.toLowerCase()] = eu
            }
            ABBREVIATION_MAP = Collections.unmodifiableMap(abbreviationMap)
        }

        /*
         * Case insensitive matching of abbreviation
         */
        @JvmStatic
        fun fromAbbreviation(abbr: String, throwIfNotFound: Boolean): EnergyUnit? {
            val abbrLower = abbr.toLowerCase()
            return when {
                ABBREVIATION_MAP.containsKey(abbrLower) -> ABBREVIATION_MAP[abbrLower]
                throwIfNotFound -> throw IllegalArgumentException("No EnergyUnit exists with abbreviation '$abbr'")
                else -> null
            }
        }
    }
}
