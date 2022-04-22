package com.machfour.macros.util

import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.NutrientValue
import com.machfour.macros.entities.Unit
import com.machfour.macros.names.DisplayStrings
import com.machfour.macros.names.UnitStrings
import com.machfour.macros.nutrients.*
import com.machfour.macros.units.NutrientUnits

// chooses unit based on what's in the FoodNutrientData object, or allows override
fun <M: NutrientValue<M>> formatNutrientValue(
    data: GenericNutrientData<M>,
    nutrient: Nutrient,
    nutrientUnits: NutrientUnits,
    preferDataUnit: Boolean = false,
    withDp: Boolean = false,
    defaultValue: Double = 0.0,
    width: Int = 0,
): String {
    val unit = if (preferDataUnit) {
        data.getUnit(nutrient, nutrientUnits)
    } else {
        nutrientUnits[nutrient]
    }
    return formatNutrientValue(data, nutrient, unit, withDp, defaultValue, width)
}

fun <M: NutrientValue<M>> formatNutrientValue(
    data: GenericNutrientData<M>,
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

fun formatQuantity(
    qty: Double,
    unit: Unit? = null,
    unitStrings: UnitStrings? = null,
    width: Int = 0,
    unitWidth: Int = 0,
    withDp: Boolean = false,
    qtyAlignLeft: Boolean = false,
    unitAlignLeft: Boolean = false,
    spaceBeforeUnit: Boolean = false
): String {
    require(unitWidth <= 0 || width == 0 || (unitWidth + if (spaceBeforeUnit) 1 else 0) in 0 until width) {
        "If width != 0, must have width > unitWidth >= 0 (unitWidth excludes space before unit)"
    }

    val unitAbbr = if (unit != null) {
        unitStrings?.getAbbr(unit) ?: unit.abbr
    } else {
        ""
    }

    val qtyString = qty.toString(precision = if (withDp) 1 else 0)

    // first format the unit, recording final unit width
    val unitSpace = if (spaceBeforeUnit) " " else ""
    val unitString = if (unit != null && unitAbbr.isNotEmpty()) {
        unitSpace + unitAbbr.fmt(unitWidth, unitAlignLeft)
    } else {
        ""
    }

    return if (qtyAlignLeft) {
        (qtyString + unitString).fmt(width, true)
    } else {
         qtyString.fmt(width - unitString.length) + unitString
    }
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
    data: FoodNutrientData,
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
                nutrientUnits = nutrientUnits,
                preferDataUnit = preferDataUnits,
                withDp = withDp
            )
            if (monoSpaceAligned) {
                append(colName.fmt(minWidth = 15))
                append(": ")
                append(value.fmt(minWidth = if (withDp) 6 else 4))
                append(" ")
                append(unitStr.fmt(minWidth = 2, leftAlign = true))

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
