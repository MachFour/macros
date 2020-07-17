package com.machfour.macros.objects

enum class FoodType constructor(val niceName: String) {
    PRIMARY("primary"),
    COMPOSITE("composite"),
    USDA("usda"),
    NUTTAB("nuttab"),
    SPECIAL("special");

    override fun toString(): String {
        return niceName
    }

    companion object {
        fun fromString(name: String): FoodType {
            return when (name) {
                "primary" -> PRIMARY
                "composite" -> COMPOSITE
                "usda" -> USDA
                "nuttab" -> NUTTAB
                "special" -> SPECIAL
                else -> throw NoSuchElementException("No FoodType with name '$name'.")
            }
        }
    }

}
