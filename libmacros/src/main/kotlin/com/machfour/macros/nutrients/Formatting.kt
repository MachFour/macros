package com.machfour.macros.nutrients

import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.Unit
import com.machfour.macros.formatting.fmt
import com.machfour.macros.formatting.toString
import com.machfour.macros.names.DisplayStrings
import com.machfour.macros.units.NutrientUnits

// chooses unit based on what's in the FoodNutrientData object, or allows override
fun formatNutrientValue(
    data: NutrientData,
    nutrient: Nutrient,
    units: NutrientUnits,
    preferDataUnit: Boolean = false,
    withDp: Boolean = false,
    defaultValue: Double = 0.0,
    width: Int = 0,
): String {
    val unit = if (preferDataUnit) {
        data.getUnit(nutrient, units)
    } else {
        units[nutrient]
    }
    return formatNutrientValue(data, nutrient, unit, withDp, defaultValue, width)
}

fun formatNutrientValue(
    data: NutrientData,
    nutrient: Nutrient,
    unit: Unit,
    withDp: Boolean = false,
    defaultValue: Double = 0.0,
    width: Int = 0,
): String {
    return data.amountOf(nutrient, unit, defaultValue)
        .toString(precision = if (withDp) 1 else 0)
        .fmt(width)
}

val defaultNutrientsToPrint = listOf(
    ENERGY,
    PROTEIN,
    FAT,
    SATURATED_FAT,
    CARBOHYDRATE,
    SUGAR,
    FIBRE,
    SODIUM,
    CALCIUM
)

fun formatNutrientData(
    data: NutrientData,
    displayStrings: DisplayStrings,
    nutrientUnits: NutrientUnits,
    nutrients: Collection<Nutrient> = defaultNutrientsToPrint,
    preferDataUnits: Boolean = false,
    withDp: Boolean = false,
    monoSpaceAligned: Boolean = false,
): String {

    return buildString {
        for (n in nutrients) {
            val colName = displayStrings.getFullName(n)
            val unit = if (preferDataUnits) data.getUnit(n, nutrientUnits) else nutrientUnits[n]
            val unitStr = displayStrings.getAbbr(unit)
            val value = formatNutrientValue(
                data = data,
                nutrient = n,
                units = nutrientUnits,
                preferDataUnit = preferDataUnits,
                withDp = withDp
            )
            if (monoSpaceAligned) {
                append(colName.fmt(minWidth = 15))
                append(": ")
                append(value.fmt(minWidth = if (withDp) 6 else 4))
                append(" ")
                append(unitStr.fmt(minWidth = 2, alignLeft = true))

            } else {
                append("$colName: $value $unitStr")
            }
            //append("$colName: $value $unitStr")
            if (!data.hasCompleteData(n)) {
                // mark incomplete
                append(" (*)")
            }
            appendLine()
        }
        // delete last newline
        deleteAt(lastIndex)
    }
}