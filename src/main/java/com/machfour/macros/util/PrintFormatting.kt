package com.machfour.macros.util

import com.machfour.macros.core.Column
import com.machfour.macros.core.Schema.NutritionDataTable
import com.machfour.macros.names.ColumnStrings
import com.machfour.macros.names.DefaultColumnStrings
import com.machfour.macros.objects.NutritionData
import com.machfour.macros.objects.Unit
import java.util.*

object PrintFormatting {
    const val nameWidth = 45
    const val servingWidth = 6
    const val shortDataWidth = 4
    const val longDataWidth = 6

    @JvmStatic
    fun formatQuantityAsVerbose(qty: Double? = null, verbose: Boolean = false): String {
        return formatQuantity(qty, width = if (verbose) longDataWidth else shortDataWidth, withDp = verbose)
    }

    @JvmOverloads
    @JvmStatic
    fun formatQuantity(
            qty: Double? = null,
            unit: Unit? = null,
            width: Int = 0,
            unitWidth: Int = 2,
            withDp: Boolean = false,
            alignLeft: Boolean = false,
            forNullQty: String = ""
    ): String {
        qty ?: return forNullQty

        require(unitWidth <= 0 || width == 0 || unitWidth in 0 until(width)) { "If width != 0, must have width > unitWidth >= 0" }

        val finalUnitWidth = if (unit != null && unitWidth <= 0) unit.abbr.length else unitWidth
        val f = Formatter(Locale.getDefault())

        val floatFmt = if (withDp) ".1f" else ".0f"
        val alignFmt = if (alignLeft) "" else "-"
        val unitStr = if (unit == null) "" else String.format("%${alignFmt}${finalUnitWidth}s", unit.abbr)
        return if (alignLeft) {
            f.format("%${floatFmt}%s${unitStr}", qty)
            if (width > 0) String.format("%-${width}s", f.toString()) else f.toString()
        } else {
            f.format("%" + (if (width > 0) width - finalUnitWidth else "") + floatFmt, qty)
            f.toString() + unitStr
        }
    }

    // Converts the given data field into a string. Adds an asterisk if the data is missing.
    // Returns null if the input nutrition data is null
    @JvmOverloads
    @JvmStatic
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
            val unit = DefaultColumnStrings.getInstance().getUnit(field)
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
        return formatQuantity(
                qty = nd.amountOf(field),
                unit = ndStrings.getUnit(field),
                width = if (needsDp) 12 else 10,
                withDp = needsDp)
    }

    // for formatting nutrition data in meal summaries (no decimal places)
    @JvmStatic
    fun mealSummaryFormat(nd: NutritionData?, field: Column<NutritionData?, Double?>, ndStrings: ColumnStrings): String? {
        nd ?: return null

        val unit = ndStrings.getUnit(field)
        return formatQuantity(nd.amountOf(field), width = 0) + " ${unit.abbr}"
    }
}

