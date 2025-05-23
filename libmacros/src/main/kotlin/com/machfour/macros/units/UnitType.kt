package com.machfour.macros.units

enum class UnitType(val id: Int, val niceName: String) {
    NONE(0, "none"),
    MASS(1, "mass"),
    VOLUME(2, "volume"),
    ENERGY(4, "energy"),
    DENSITY(8, "density"),
    ;

    // returns true if the given flags match this UnitType's id
    internal fun matchedByFlags(flags: Int): Boolean {
        return (id and flags) != 0
    }

    companion object {
        private val idMap = mapOf(
            MASS.id to MASS, VOLUME.id to VOLUME, ENERGY.id to ENERGY, DENSITY.id to DENSITY
        )

        fun fromId(id: Int): UnitType = idMap.getValue(id)

        // allow specifying multiple unit types
        fun fromFlags(flags: Int): Set<UnitType> {
            return entries.filter { it.matchedByFlags(flags) }.toSet()
        }

        fun asFlags(types: Array<out UnitType>): Int {
            return types.sumOf { it.id }
        }

        fun Set<UnitType>.toFlags(): Int {
            return asFlags(toTypedArray())
        }
    }
}
