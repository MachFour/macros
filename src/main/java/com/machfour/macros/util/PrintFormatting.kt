package com.machfour.macros.util

import com.machfour.macros.core.NutrientData
import com.machfour.macros.names.ColumnStrings
import com.machfour.macros.names.UnitNamer
import com.machfour.macros.objects.*
import com.machfour.macros.objects.Unit
import com.machfour.macros.objects.inbuilt.Nutrients
import java.util.Formatter

object PrintFormatting {
    fun formatQuantity(
            nd: NutrientData,
            n: Nutrient,
            colStrings: ColumnStrings? = null,
            withUnit: Boolean = false,
            width: Int = 0,
            unitWidth: Int = 0,
            withDp: Boolean = false,
            alignLeft: Boolean = false,
            unitAlignLeft: Boolean = false,
            spaceBeforeUnit: Boolean = false
    ) : String {
        val qty = nd.amountOf(n, defaultValue = 0.0)
        val unit = if (withUnit) {
            requireNotNull(colStrings) { "If units are needed, colStrings must be given" }
            nd.getUnitOrDefault(n)
        } else {
            null
        }

        // TODO add asterisk to incomplete quantities
        return formatQuantity(
                qty = qty,
                unit = unit,
                unitNamer = colStrings,
                width = width,
                unitWidth = unitWidth,
                withDp = withDp,
                alignLeft = alignLeft,
                unitAlignLeft = unitAlignLeft,
                spaceBeforeUnit = spaceBeforeUnit
        )
    }
    fun formatQuantity(
            qty: Double,
            unit: Unit? = null,
            unitNamer: UnitNamer? = null,
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
            unitNamer?.getAbbr(unit) ?: unit.abbr
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
        return Formatter().let {
            if (alignLeft) {
                it.format("%${floatFmt}${formattedUnitString}", qty)
                if (width > 0) String.format("%-${width}s", it.toString()) else it.toString()
            } else {
                val qtyWidthStr = if (width > 0) "${width - finalUnitWidth}" else ""
                it.format("%$qtyWidthStr$floatFmt", qty)
                it.toString() + formattedUnitString
            }

        }
    }

    val defaultNutrientsToPrint = listOf(
          Nutrients.ENERGY
        , Nutrients.PROTEIN
        , Nutrients.FAT
        , Nutrients.SATURATED_FAT
        , Nutrients.CARBOHYDRATE
        , Nutrients.SUGAR
        , Nutrients.FIBRE
        , Nutrients.SODIUM
        , Nutrients.CALCIUM
    )


    fun nutritionDataToText(
            nd: NutrientData,
            colStrings: ColumnStrings,
            nutrients: List<Nutrient> = defaultNutrientsToPrint,
            withDp: Boolean = false,
            monoSpaceAligned: Boolean = false,
    ) : String {
        // TODO get these lengths from ColumnStrings?
        val lineFormat = if (monoSpaceAligned) {
            val qtyLength = if (withDp) 6 else 4
            "%15s: %${qtyLength}s %-2s"
        } else {
            "%s: %s %s"
        }

        return buildString {
            for (i in nutrients.indices) {
                if (i != 0) {
                    appendLine()
                }
                val n = nutrients[i]
                val colName = colStrings.getName(n)
                val value = formatQuantity(nd, n, colStrings, withUnit = false, withDp = withDp)
                val unitStr = colStrings.getAbbr(nd.getUnitOrDefault(n))
                append(lineFormat.format(colName, value, unitStr))
                //append("$colName: $value $unitStr")
                if (!nd.hasCompleteData(n)) {
                    // mark incomplete
                    append(" (*)")
                }
            }
        }
    }
}

