package com.machfour.macros.objects

enum class UnitType(val id: Long, val niceName: String) {
    MASS(1, "grams"),
    VOLUME(2, "volume"),
    ENERGY(3, "energy");

    companion object {
        private val ID_MAP = mapOf(
              MASS.id to MASS
            , VOLUME.id to VOLUME
            , ENERGY.id to ENERGY
        )
        fun fromId(id: Long): UnitType = ID_MAP.getValue(id)
    }

}
