package com.machfour.macros.foodname

import com.machfour.macros.entities.Food
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.sql.Column

interface FoodDescription {
    val indexName: String
    val basicName: String
    val brand: String?
    val variety: String?
    val extraDesc: String?
    val notes: String?
    val dataSource: String?
    val dataNotes: String?

    fun getDescriptionData(col: Column<Food, String>): String? {
        return when (col) {
            FoodTable.NAME -> basicName
            FoodTable.BRAND -> brand
            FoodTable.VARIETY -> variety
            FoodTable.EXTRA_DESC -> extraDesc
            FoodTable.NOTES -> notes
            FoodTable.INDEX_NAME -> indexName
            FoodTable.DATA_SOURCE -> dataSource
            FoodTable.DATA_NOTES -> dataNotes
            else -> null
        }
    }

    fun hasDescriptionData(col: Column<Food, String>): Boolean {
        return getDescriptionData(col) != null
    }
    val shortName: String
        get() = prettyFormat(withBrand = false, withVariety = false)

    val longName: String
        get() = prettyFormat(withExtra = true)

    val mediumName: String
        get() = prettyFormat()

    val sortableName: String
        get() = prettyFormat(withExtra = true, sortable = true)

}

val foodDescriptionColumns = listOf(
    FoodTable.NAME,
    FoodTable.BRAND,
    FoodTable.VARIETY,
    FoodTable.EXTRA_DESC,
    FoodTable.NOTES,
    FoodTable.INDEX_NAME,
    FoodTable.DATA_SOURCE,
    FoodTable.DATA_NOTES,
)

fun indexNamePrototype(
    basicName: String,
    brand: String?,
    variety: String?,
    extraDesc: String?,
): String {
    // use sortable name but replace sequences of spaces (and dashes) with a single dash
    return formatFoodName(
        basicName = basicName,
        brand = brand,
        variety = variety,
        extraDesc = extraDesc,
        withBrand = true,
        withVariety = true,
        withExtra = true,
        sortable = true
    )
        .replace(Regex("[()\\[\\]{}&%~`!$#@*^+=:;<>?'|\"/\\\\]"), replacement = "")
        .replace(Regex("[\\s-,]+"), replacement = "-")
        .removePrefix("-")
        .removeSuffix("-")
}

/*
 * Order of fields:
 * if sortable:
 *     <name>, <brand>, <variety> (<extra desc>)
 * else:
 *     <brand> <variety> <name> (<extra desc>)
 */
fun formatFoodName(
    basicName: String,
    brand: String?,
    variety: String?,
    extraDesc: String?,
    withBrand: Boolean = true,
    withVariety: Boolean = true,
    withExtra: Boolean = false,
    sortable: Boolean = false,
    includeEmptyFields: Boolean = true,
): String {
    val isPresent: (String?) -> Boolean = {
        it != null && (includeEmptyFields || it.isNotEmpty())
    }

    return buildString {
        append(basicName)
        if (sortable) {
            if (withBrand && isPresent(brand)) {
                append(", $brand")
            }
            if (isPresent(variety)) {
                append(", $variety")
            }
        } else {
            if (withVariety && isPresent(variety)) {
                insert(0, "$variety ")
            }
            if (withBrand && isPresent(brand)) {
                insert(0, "$brand ")
            }
        }
        if (withExtra && isPresent(extraDesc)) {
            append(" ($extraDesc)")
        }
    }

}

private fun FoodDescription.prettyFormat(
    withBrand: Boolean = true,
    withVariety: Boolean = true,
    withExtra: Boolean = false,
    sortable: Boolean = false
): String {
    return formatFoodName(
        basicName = basicName,
        brand = brand,
        variety = variety,
        extraDesc = extraDesc,
        withBrand = withBrand,
        withVariety = withVariety,
        withExtra = withExtra,
        sortable = sortable,
    )
}

