package com.machfour.macros.util

import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.names.DisplayStrings
import com.machfour.macros.names.UnitStrings
import com.machfour.macros.entities.*
import com.machfour.macros.entities.Unit
import com.machfour.macros.nutrients.Nutrients

object PrintFormatting {
    fun nutrient(
        nd: FoodNutrientData,
        n: Nutrient,
        displayStrings: DisplayStrings? = null,
        withUnit: Boolean = false,
        width: Int = 0,
        unitWidth: Int = 0,
        withDp: Boolean = false,
        alignLeft: Boolean = false,
        unitAlignLeft: Boolean = false,
        spaceBeforeUnit: Boolean = false
    ): String {
        val qty = nd.amountOf(n, defaultValue = 0.0)
        val unit = if (withUnit) {
            requireNotNull(displayStrings) { "If units are needed, displayStrings must be given" }
            nd.getUnitOrDefault(n)
        } else {
            null
        }

        // TODO add asterisk to incomplete quantities
        return quantity(
            qty = qty,
            unit = unit,
            unitStrings = displayStrings,
            width = width,
            unitWidth = unitWidth,
            withDp = withDp,
            alignLeft = alignLeft,
            unitAlignLeft = unitAlignLeft,
            spaceBeforeUnit = spaceBeforeUnit
        )
    }

    fun quantity(
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

    fun nutrientData(
        nd: FoodNutrientData,
        displayStrings: DisplayStrings,
        nutrients: Collection<Nutrient> = defaultNutrientsToPrint,
        withDp: Boolean = false,
        monoSpaceAligned: Boolean = false,
    ): String {
        // TODO get these lengths from ColumnStrings?
        val lineFormat = if (monoSpaceAligned) {
            val qtyLength = if (withDp) 6 else 4
            "%15s: %${qtyLength}s %-2s"
        } else {
            "%s: %s %s"
        }

        return buildString {
            for (n in nutrients) {
                val colName = displayStrings.getFullName(n)
                val value = nutrient(nd, n, displayStrings, withUnit = false, withDp = withDp)
                val unitStr = displayStrings.getAbbr(nd.getUnitOrDefault(n))
                append(lineFormat.format(colName, value, unitStr))
                //append("$colName: $value $unitStr")
                if (!nd.hasCompleteData(n)) {
                    // mark incomplete
                    append(" (*)")
                }
                appendLine()
            }
            // delete last newline
            deleteAt(lastIndex)
        }
    }
}

