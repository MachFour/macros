package com.machfour.macros.util

import com.machfour.macros.names.ColumnStrings
import com.machfour.macros.names.DefaultColumnStrings
import com.machfour.macros.objects.*
import com.machfour.macros.objects.Unit
import com.machfour.macros.objects.inbuilt.DefaultUnits
import com.machfour.macros.objects.inbuilt.Nutrients
import com.machfour.macros.objects.inbuilt.Units
import java.util.Formatter

object PrintFormatting {
    const val nameWidth = 42
    const val servingWidth = 6
    const val shortDataWidth = 6
    const val longDataWidth = 7

    fun formatQuantityAsVerbose(qty: Double? = null, verbose: Boolean = false): String {
        return formatQuantity(qty, width = if (verbose) longDataWidth else shortDataWidth, unitWidth = 2, withDp = verbose)
    }
    fun formatQuantity(
        nd: NutritionData,
        n: Nutrient,
        colStrings: ColumnStrings = DefaultColumnStrings.instance,
        withUnit: Boolean = false,
        width: Int = 0,
        unitWidth: Int = 0,
        withDp: Boolean = false,
        alignLeft: Boolean = false,
        forNullQty: String = "",
        spaceBeforeUnit: Boolean = false
    ) : String {
        val qty = nd.amountOf(n)
        val unit = if (!withUnit) null else nd.getUnitOrDefault(n)
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
        val alignFmt = if (alignLeft) "-" else ""
        val unitFmt = "%${alignFmt}${unitWidth}s"
        
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

    // Converts the given data field into a string. Adds an asterisk if the data is missing.
    // Returns null if the input nutrition data is null
    fun formatNutrnData(
        nd: NutritionData,
        nutrient: Nutrient,
        withUnit: Boolean = false,
        colStrings: ColumnStrings = DefaultColumnStrings.instance
    ): String {
        // TODO move this logic somewhere else
        val missing = !nd.hasCompleteData(nutrient)
        return if (!withUnit) {
            formatQuantityAsVerbose(nd.amountOf(nutrient), false) + if (missing) "*" else ""
        } else {
            formatQuantity(nd, nutrient, colStrings)
        }
    }

    // list of field that should be formatted without a decimal place (because the values are
    // typically large (in the default/metric unit)
    // TODO use unit instead of checking the exact column
    private val fieldsWithoutDp = setOf(
            Nutrients.ENERGY,
            Nutrients.OMEGA_3_FAT,
            Nutrients.OMEGA_6_FAT,
            Nutrients.IRON,
            Nutrients.POTASSIUM,
            Nutrients.SODIUM,
            Nutrients.CALCIUM
    )

    // for formatting nutrition data in food details
    fun foodDetailsFormat(nd: NutritionData?, nutrient: Nutrient, colStrings: ColumnStrings): String? {
        nd ?: return null

        val needsDp = !fieldsWithoutDp.contains(nutrient)
        val qty = nd.amountOf(nutrient)
        val unit = nd.getUnitOrDefault(nutrient)
        // TODO pass colStrings for unit abbreviation
        return formatQuantity(qty, unit, width = if (needsDp) 12 else 10, unitWidth = 2,  withDp = needsDp)
    }

    // for formatting nutrition data in meal summaries (no decimal places)
    fun mealSummaryFormat(nd: NutritionData?, n: Nutrient, ndStrings: ColumnStrings): String? {
        return if (nd == null) null else formatQuantity(
            nd = nd,
            n = n,
            colStrings = ndStrings,
            spaceBeforeUnit = true
        )
    }

    fun nutritionDataToText(nd: NutritionData, colStrings: ColumnStrings, nutrients: List<Nutrient>): String {
        return StringBuilder().run {
            for (n in nutrients) {
                val colName = colStrings.getName(n)
                val value = formatQuantity(nd, n, colStrings, withUnit = false)
                val unitStr = colStrings.getAbbr(nd.getUnitOrDefault(n))
                append("$colName: $value $unitStr")
                if (!nd.hasCompleteData(n)) {
                    // mark incomplete
                    append(" (*)")
                }
                appendLine()
            }
            toString()
        }
    }
}

