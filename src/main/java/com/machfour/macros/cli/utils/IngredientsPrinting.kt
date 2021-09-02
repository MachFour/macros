package com.machfour.macros.cli.utils

import com.machfour.macros.entities.Ingredient
import com.machfour.macros.names.EnglishUnitNames
import com.machfour.macros.util.formatQuantity
import com.machfour.macros.util.stringJoin

/*
 * Ingredients printing parameters
 */
private const val quantityWidth = 10
private const val notesWidth = 25
private const val start = " | "
private const val sep = "  "
private const val end = " |"
private val lineFormat = start + strFmtL(foodNameWidth) + sep + strFmt(quantityWidth) + sep + strFmtL(notesWidth) + end
private const val lineLength = foodNameWidth + notesWidth + quantityWidth + 2 * sep.length + start.length + end.length - 2
private val hLine = " " + stringJoin(listOf("-"), copies = lineLength)

fun printIngredients(ingredients: List<Ingredient>) {
    // XXX use printLine(text, widths), etc function
    println(lineFormat.format("Name", "Quantity", "Notes"))
    println(hLine)
    for (i in ingredients) {
        // format:  <name>          (<notes>)     <quantity/serving>
        val iFood = i.food
        val notes = i.notes
        val name = iFood.mediumName
        val noteString = notes ?: ""
        val quantityString = formatQuantity(
            qty = i.quantity,
            unit = i.qtyUnit,
            unitStrings = EnglishUnitNames,
            width = quantityWidth,
            unitWidth = 2)
        println(lineFormat.format(name, quantityString, noteString))
        // TODO replace quantity with serving if specified
        //Serving iServing = i.getServing();
        //printf(" %-8s", iServing != null ? "(" + i.servingCountString() + " " +  iServing.name() + ")" : "");
    }
    println(hLine)
}

/*
 * Fixed width string format, left aligned
 */
private fun strFmtL(n: Int): String {
    return "%-${n}s"
}

/*
 * Fixed width string format
 */
private fun strFmt(n: Int): String {
    return "%${n}s"
}

// Fixed width string format
private fun strFmt(n: Int, leftAlign: Boolean): String {
    val align = if (leftAlign) "-" else ""
    return "%${align}${n}s"
}


