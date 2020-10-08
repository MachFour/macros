package com.machfour.macros.util

import com.machfour.macros.core.Column
import com.machfour.macros.core.Schema.NutritionDataTable
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.QUANTITY
import com.machfour.macros.names.ColumnStrings
import com.machfour.macros.names.DefaultColumnStrings
import com.machfour.macros.objects.NutritionData
import com.machfour.macros.objects.Unit
import java.util.Formatter

object PrintFormatting {
    const val nameWidth = 45
    const val servingWidth = 6
    const val shortDataWidth = 4
    const val longDataWidth = 6

    fun formatQuantityAsVerbose(qty: Double? = null, verbose: Boolean = false): String {
        return formatQuantity(qty, width = if (verbose) longDataWidth else shortDataWidth, withDp = verbose)
    }

    fun formatQuantity(
            qty: Double? = null,
            unit: Unit? = null,
            width: Int = 0,
            unitWidth: Int = 2,
            withDp: Boolean = false,
            alignLeft: Boolean = false,
            forNullQty: String = "",
            abbreviateUnit: (Unit) -> String = { it.abbr },
            spaceBeforeUnit: Boolean = false
    ): String {
        qty ?: return forNullQty

        require(unitWidth <= 0 || width == 0 || unitWidth in 0 until(width)) { "If width != 0, must have width > unitWidth >= 0" }

        val unitAbbr = unit?.let { abbreviateUnit(it) }
        val finalUnitWidth = if (unitAbbr != null && unitWidth <= 0) {
            unitAbbr.length + (if (spaceBeforeUnit) 1 else 0)
        } else {
            unitWidth
        }

        val floatFmt = if (withDp) ".1f" else ".0f"
        val alignFmt = if (alignLeft) "" else "-"
        val unitFmt = "%${alignFmt}${finalUnitWidth}s"
        val unitStr = if (unitAbbr == null) ""
            else unitFmt.format((if (spaceBeforeUnit) " " else "") + unitAbbr)

        return Formatter().let {
            if (alignLeft) {
                it.format("%${floatFmt}${unitStr}", qty)
                if (width > 0) String.format("%-${width}s", it.toString()) else it.toString()
            } else {
                it.format("%" + (if (width > 0) width - finalUnitWidth else "") + floatFmt, qty)
                it.toString() + unitStr
            }

        }
    }

    // Converts the given data field into a string. Adds an asterisk if the data is missing.
    // Returns null if the input nutrition data is null
    @JvmStatic
    @JvmOverloads
    fun formatNutrnData(nd: NutritionData?, field: Column<NutritionData, Double>, withUnit: Boolean = false): String? {
        if (nd == null) {
            return null
        }
        val missing = !nd.hasCompleteData(field)
        return if (!withUnit) {
            formatQuantityAsVerbose(nd.amountOf(field), false) + if (missing) "*" else ""
        } else {
            //QtyUnit unit = QtyUnit.fromAbbreviation(NutritionData.getUnitStringForNutrient(field));
            // TODO
            val unit = if (field === QUANTITY) nd.qtyUnit
                else DefaultColumnStrings.instance.getUnit(field)
            formatQuantity(nd.amountOf(field), unit, width = 0, unitWidth = 0)
        }
    }

    // list of field that should be formatted without a decimal place (because the values are
    // typically large (in the default/metric unit)
    // TODO use unit instead of checking the exact column
    private val fieldsWithoutDp: Set<Column<NutritionData, Double>> = setOf(
            NutritionDataTable.CALORIES,
            NutritionDataTable.KILOJOULES,
            NutritionDataTable.OMEGA_3_FAT,
            NutritionDataTable.OMEGA_6_FAT,
            NutritionDataTable.IRON,
            NutritionDataTable.POTASSIUM,
            NutritionDataTable.SODIUM,
            NutritionDataTable.CALCIUM
    )

    // for formatting nutrition data in food details
    @JvmStatic
    fun foodDetailsFormat(nd: NutritionData?, field: Column<NutritionData, Double>, ndStrings: ColumnStrings): String? {
        nd ?: return null

        val needsDp = !fieldsWithoutDp.contains(field)
        val qty = nd.amountOf(field)
        val unit = if (field === QUANTITY) nd.qtyUnit else ndStrings.getUnit(field)
        return formatQuantity(qty, unit, width = if (needsDp) 12 else 10, withDp = needsDp)
    }

    // for formatting nutrition data in meal summaries (no decimal places)
    @JvmStatic
    fun mealSummaryFormat(nd: NutritionData?, field: Column<NutritionData, Double>, ndStrings: ColumnStrings): String? {
        return if (nd == null) null else mealSummaryFormatNotNull(nd, field, ndStrings)
    }
    // for formatting nutrition data in meal summaries (no decimal places)
    fun mealSummaryFormatNotNull(nd: NutritionData, field: Column<NutritionData, Double>, colStrings: ColumnStrings): String {
        return formatQuantity(
            qty = nd.amountOf(field),
            unit = colStrings.getUnit(field),
            width = 0,
            abbreviateUnit = { colStrings.getAbbr(it) },
            spaceBeforeUnit = true
        )

    }
}

