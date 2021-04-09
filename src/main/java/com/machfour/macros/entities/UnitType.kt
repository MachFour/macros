package com.machfour.macros.entities

enum class UnitType(val id: Int, val niceName: String) {
    NONE(0, "none"),
    MASS(1, "grams"),
    VOLUME(2, "volume"),
    ENERGY(4, "energy"),
    DENSITY(8, "density"),
    ;

    companion object {
        private val idMap = mapOf(
              MASS.id to MASS
            , VOLUME.id to VOLUME
            , ENERGY.id to ENERGY
            , DENSITY.id to DENSITY
        )
        fun fromId(id: Int): UnitType = idMap.getValue(id)

        // allow specifying multiple unit types
        fun fromFlags(flags: Int) : Set<UnitType> {
            return values().filter { it.id and flags != 0 }.toSet()
        }

        fun asFlags(types: Set<UnitType>) : Int {
            return types.sumOf { it.id }
        }
    }

}
