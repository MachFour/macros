package com.machfour.macros.util

import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.Unit
import com.machfour.macros.names.DisplayStrings
import com.machfour.macros.names.UnitStrings
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.nutrients.Nutrients
import com.machfour.macros.units.NutrientUnits


fun formatNutrientValue(
    data: FoodNutrientData,
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

fun formatNutrientValue(
    data: FoodNutrientData,
    nutrient: Nutrient,
    unit: Unit,
    withDp: Boolean = false,
    defaultValue: Double = 0.0,
    width: Int = 0,
): String {
    val widthFmt = if (width > 0) "$width" else ""
    val floatFmt = if (withDp) ".1f" else ".0f"
    val qty = data.amountOf(nutrient, unit, defaultValue)
    return "%$widthFmt$floatFmt".format(qty)
}

internal fun formatQuantity(
    qty: Double,
    unit: Unit? = null,
    unitStrings: UnitStrings? = null,
    width: Int = 0,
    unitWidth: Int = 0,
    withDp: Boolean = false,
    alignLeft: Boolean = false,
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
    // first format the unit, recording final unit width
    val finalUnitWidth: Int
    val formattedUnitString = if (unit != null && unitAbbr.isNotEmpty()) {
        val space = if (spaceBeforeUnit) " " else ""
        val unitString = space + unitAbbr
        finalUnitWidth = if (unitWidth <= 0) unitString.length else (unitWidth + space.length)
        val left = if (unitAlignLeft) "-" else ""
        "%${left}${finalUnitWidth}s".format(unitString)
    } else {
        finalUnitWidth = 0
        ""
    }

    val floatFmt = if (withDp) ".1f" else ".0f"
    return if (alignLeft) {
        val formattedQty = "%${floatFmt}${formattedUnitString}".format(qty)
        if (width > 0) String.format("%-${width}s", formattedQty) else formattedQty
    } else {
        val qtyWidthStr = if (width > 0) "${width - finalUnitWidth}" else ""
        "%$qtyWidthStr$floatFmt".format(qty) + formattedUnitString
    }
}

val defaultNutrientsToPrint = listOf(
    Nutrients.ENERGY,
    Nutrients.PROTEIN,
    Nutrients.FAT,
    Nutrients.SATURATED_FAT,
    Nutrients.CARBOHYDRATE,
    Nutrients.SUGAR,
    Nutrients.FIBRE,
    Nutrients.SODIUM,
    Nutrients.CALCIUM
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
    val lineFormat = if (monoSpaceAligned) {
        val qtyLength = if (withDp) 6 else 4
        "%15s: %${qtyLength}s %-2s"
    } else {
        "%s: %s %s"
    }

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
            append(lineFormat.format(colName, value, unitStr))
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
