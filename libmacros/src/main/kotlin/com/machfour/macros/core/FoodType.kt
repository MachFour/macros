package com.machfour.macros.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// TODO get rid of this enum
//  Possible replacements: isUserEntered, is3rdPartyData, isFromBulkDB
//  Check for other database ID
@Serializable
enum class FoodType(val niceName: String, val baseSearchRelevance: SearchRelevance) {
    @SerialName("primary")
    PRIMARY("primary", SearchRelevance.USER),
    @SerialName("composite")
    COMPOSITE("composite", SearchRelevance.USER),
    @SerialName("usda")
    USDA("usda", SearchRelevance.INBUILT),
    @SerialName("nuttab")
    NUTTAB("nuttab", SearchRelevance.INBUILT)
    ;

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
                else -> null
            }
        }
    }

}
