package com.machfour.macros.formatting

import com.machfour.macros.entities.Unit
import com.machfour.macros.names.UnitStrings

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

    val unitAbbr = unit?.let { unitStrings?.getAbbr(it) ?: it.abbr } ?: ""
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

