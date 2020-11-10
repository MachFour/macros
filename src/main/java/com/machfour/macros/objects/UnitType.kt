package com.machfour.macros.objects

enum class UnitType(val id: Long, val niceName: String) {
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
        fun fromId(id: Long): UnitType = idMap.getValue(id)

        // allow specifying multiple unit types
        fun fromFlags(flags: Long) : Set<UnitType> {
            return values().filter { it.id and flags != 0L }.toSet()
        }

        fun asFlags(types: Set<UnitType>) : Long {
            return types.map { it.id }.sum()
        }
    }

}
