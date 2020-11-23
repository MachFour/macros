package com.machfour.macros.util

import com.machfour.macros.core.NutrientData
import com.machfour.macros.names.ColumnStrings
import com.machfour.macros.objects.*
import com.machfour.macros.objects.Unit
import com.machfour.macros.objects.inbuilt.Nutrients
import java.util.Formatter

object PrintFormatting {
    const val nameWidth = 42
    const val servingWidth = 6
    const val shortDataWidth = 6
    const val longDataWidth = 7

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
            forNullQty: String = "",
            spaceBeforeUnit: Boolean = false
    ) : String {
        val qty = nd.amountOf(n, defaultValue = 0.0)
        val unit: Unit?
        val unitString: String?
        if (withUnit) {
            requireNotNull(colStrings) { "If units are needed, colStrings must be given" }
            unit = nd.getUnitOrDefault(n)
            unitString = colStrings.getAbbr(unit)
        } else {
            unit = null
            unitString = null
        }

        // TODO add asterisk to incomplete quantities
        return formatQuantity(
                qty,
                unit,
                width,
                unitWidth,
                withDp,
                alignLeft,
                unitAlignLeft,
                forNullQty,
                unitString,
                spaceBeforeUnit
        )
    }
    fun formatQuantity(
            qty: Double? = null,
            unit: Unit? = null,
            width: Int = 0,
            unitWidth: Int = 0,
            withDp: Boolean = false,
            alignLeft: Boolean = false,
            unitAlignLeft: Boolean = false,
            forNullQty: String = "",
            unitString: String? = null,
            spaceBeforeUnit: Boolean = false
    ): String {
        qty ?: return forNullQty

        var unitString = unitString
        if (unitString == null && unit != null) {
            unitString = unit.abbr
        }

        var unitWidth = unitWidth
        if (unitString != null && unitWidth <= 0) {
            unitWidth = unitString.length
            if (spaceBeforeUnit) {
                unitWidth += 1
            }
        } 

        require(unitWidth <= 0 || width == 0 || unitWidth in 0 until width) {
            "If width != 0, must have width > unitWidth >= 0 (including space before unit)"
        }


        val floatFmt = if (withDp) ".1f" else ".0f"
        val unitAlign = if (unitAlignLeft) "-" else ""
        val unitFmt = "%${unitAlign}${unitWidth}s"
        
        if (unitString != null) {
            if (spaceBeforeUnit) {
                unitString = " $unitString"
            }
            unitString = unitFmt.format((if (spaceBeforeUnit) " " else "") + unitString)
        } else {
            unitString = ""
        }

        return Formatter().let {
            if (alignLeft) {
                it.format("%${floatFmt}${unitString}", qty)
                if (width > 0) String.format("%-${width}s", it.toString()) else it.toString()
            } else {
                it.format("%" + (if (width > 0) width - unitWidth else "") + floatFmt, qty)
                it.toString() + unitString
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

