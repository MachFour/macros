package com.machfour.macros.core

enum class FoodType(val niceName: String, val defaultSearchRelevance: SearchRelevance) {
    PRIMARY("primary", SearchRelevance.USER),
    COMPOSITE("composite", SearchRelevance.USER),
    USDA("usda", SearchRelevance.INBUILT),
    NUTTAB("nuttab", SearchRelevance.INBUILT),
    SPECIAL("special", SearchRelevance.INBUILT);

    override fun toString(): String {
        return niceName
    }
    
    companion object {
        fun fromString(name: String): FoodType? {
            return when (name) {
                "primary" -> PRIMARY
                "composite" -> COMPOSITE
                "usda" -> USDA
                "nuttab" -> NUTTAB
                "special" -> SPECIAL
                else -> null
            }
        }
    }

}
