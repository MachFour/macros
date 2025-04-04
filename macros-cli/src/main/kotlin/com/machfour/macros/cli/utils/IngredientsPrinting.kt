package com.machfour.macros.cli.utils

import com.machfour.macros.entities.Ingredient
import com.machfour.macros.formatting.fmt
import com.machfour.macros.formatting.formatQuantity
import com.machfour.macros.names.EnglishUnitNames

// Ingredients printing parameters
private const val quantityWidth = 10
private const val notesWidth = 25
private const val sideEdge = "|"
private const val topBottomEdge = "-"
private const val lineLength = foodNameWidth + notesWidth + quantityWidth + sideEdge.length + sideEdge.length + 6
private val hLine = " " + sideEdge + topBottomEdge.repeat(lineLength - 2) + sideEdge

fun printLine(foodName: String, quantityString: String, noteString: String) {
    // have to treat food name specially in case it contains unicode characters
    val formattedName = foodName.fmtUnicode(foodNameWidth, leftAlign = true)
    println(" $sideEdge $formattedName  ${quantityString.fmt(quantityWidth)}  "
            + noteString.fmt(notesWidth, true) + " $sideEdge")
}

fun printIngredients(ingredients: List<Ingredient>) {
    println(hLine)
    printLine("Name", "Quantity", "Notes")
    println(hLine)
    for (i in ingredients) {
        // format:  <name>          (<notes>)     <quantity/serving>
        val name = i.food.mediumName
        val noteString = i.notes ?: ""
        val quantityString = formatQuantity(
            qty = i.quantity,
            unit = i.qtyUnit,
            unitStrings = EnglishUnitNames,
            width = quantityWidth,
            unitWidth = 2
        )
        printLine(name, quantityString, noteString)
        // TODO replace quantity with serving if specified
        //Serving iServing = i.getServing();
        //printf(" %-8s", iServing != null ? "(" + i.servingCountString() + " " +  iServing.name() + ")" : "");
    }
    println(hLine)
}