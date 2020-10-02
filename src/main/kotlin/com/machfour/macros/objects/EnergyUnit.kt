package com.machfour.macros.objects

/*
 * Units for measuring quantities of food (only). Not for nutrition measurements.
 */

// name: Full name of this Unit
// abbrevaitionh: must be unique among different Units.
sealed class EnergyUnit private constructor(override val name: String, override val abbr: String) : Unit {
    companion object {
        private const val kjName = "Kilojoules"
        private const val kjAbbr = "kj"
        private const val calName = "Calories"
        private const val calAbbr = "cal"

        private val abbreviationMap = linkedMapOf(kjAbbr to Kilojoules, calAbbr to Calories)

        fun fromAbbreviation(abbr: String, throwIfNotFound: Boolean): EnergyUnit? {
            val key = abbr.toLowerCase()
            return if (throwIfNotFound) abbreviationMap.getValue(key) else abbreviationMap[key]
        }
    }

    override fun toString(): String {
        return "$name ($abbr)"
    }

    object Kilojoules: EnergyUnit(kjName, kjAbbr)
    object Calories: EnergyUnit(calName, calAbbr)
}
