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
        nd: NutritionData,
        col: Column<NutritionData, Double>,
        colStrings: ColumnStrings,
        withUnit: Boolean = false,
        width: Int = 0,
        unitWidth: Int = 0,
        withDp: Boolean = false,
        alignLeft: Boolean = false,
        forNullQty: String = "",
        spaceBeforeUnit: Boolean = false
    ) : String {
        val qty = nd.amountOf(col)
        val unit = if (!withUnit) null else if (col === QUANTITY) nd.qtyUnit else colStrings.getUnit(col)
        val unitString = if (!withUnit) null else colStrings.getAbbr(unit!!)

        // TODO add asterisk to incomplete quantities
        return formatQuantity(
             qty,
             unit,
             width,
             unitWidth,
             withDp,
             alignLeft,
             forNullQty,
             unitString,
             spaceBeforeUnit
        )
    }
    fun formatQuantity(
        qty: Double? = null,
        unit: Unit? = null,
        width: Int = 0,
        unitWidth: Int = 2,
        withDp: Boolean = false,
        alignLeft: Boolean = false,
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

        require(unitWidth <= 0 || width == 0 || unitWidth in 0 until(width)) {
            "If width != 0, must have width > unitWidth >= 0 (including space before unit)"
        }


        val floatFmt = if (withDp) ".1f" else ".0f"
        val alignFmt = if (alignLeft) "" else "-"
        val unitFmt = "%${alignFmt}${unitWidth}s"
        
        if (unitString != null) {
            if (spaceBeforeUnit) {
                unitString = " " + unitString
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

    // Converts the given data field into a string. Adds an asterisk if the data is missing.
    // Returns null if the input nutrition data is null
    @JvmStatic
    @JvmOverloads
    fun formatNutrnData(
        nd: NutritionData?,
        field: Column<NutritionData, Double>,
        withUnit: Boolean = false,
        colStrings: ColumnStrings = DefaultColumnStrings.instance
    ): String? {
        if (nd == null) {
            return null
        }
        // TODO move this logic somewhere else
        val missing = !nd.hasCompleteData(field)
        return if (!withUnit) {
            formatQuantityAsVerbose(nd.amountOf(field), false) + if (missing) "*" else ""
        } else {
            formatQuantity(nd, field, colStrings)
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
        return if (nd == null) null else formatQuantity(
            nd = nd,
            col = field,
            colStrings = ndStrings,
            spaceBeforeUnit = true
        )
    }
}

