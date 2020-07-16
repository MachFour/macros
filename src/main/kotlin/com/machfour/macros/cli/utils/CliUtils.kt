package com.machfour.macros.cli.utils

import com.machfour.macros.core.Schema.NutritionDataTable
import com.machfour.macros.names.ColumnNamer
import com.machfour.macros.names.ColumnUnits
import com.machfour.macros.names.DefaultColumnUnits
import com.machfour.macros.names.EnglishColumnNames
import com.machfour.macros.objects.Ingredient
import com.machfour.macros.objects.NutritionData
import com.machfour.macros.util.MiscUtils.javaTrim
import com.machfour.macros.util.PrintFormatting
import com.machfour.macros.util.PrintFormatting.formatQuantity
import com.machfour.macros.util.StringJoiner.Companion.of
import java.io.BufferedReader
import java.io.IOException
import java.io.PrintStream
import java.util.*

object CliUtils {
    private val allNutrientsToPrint = listOf(
        NutritionDataTable.KILOJOULES
        , NutritionDataTable.CALORIES
        , NutritionDataTable.PROTEIN
        , NutritionDataTable.FAT
        , NutritionDataTable.SATURATED_FAT
        , NutritionDataTable.CARBOHYDRATE
        , NutritionDataTable.SUGAR
        , NutritionDataTable.FIBRE
        , NutritionDataTable.SODIUM
        , NutritionDataTable.CALCIUM
    )

    @JvmStatic
    fun printPer100g(nd: NutritionData, verbose: Boolean, out: PrintStream) {
        printNutritionData(nd.rescale(100.0), verbose, out)
    }

    // TODO use methods from PrintFormatting here?
    @JvmStatic
    fun printNutritionData(nd: NutritionData, verbose: Boolean, out: PrintStream) {
        // TODO pass in ColumnUnits and ColumnNamer
        val colNamer: ColumnNamer = EnglishColumnNames.instance
        val colUnits: ColumnUnits = DefaultColumnUnits.instance
        // TODO get these lengths from ColumnNamer
        val lineFormat = if (verbose) "%15s: %6.1f %-2s" else "%15s: %4.0f %-2s"
        for (col in allNutrientsToPrint) {
            val value = nd.amountOf(col, 0.0)
            val unitStr = colUnits.getUnit(col).abbr
            val colName = colNamer.getName(col!!)
            out.print(String.format(lineFormat, colName, value, unitStr))
            if (!nd.hasCompleteData(col)) {
                // mark incomplete
                out.print(" (*)")
            }
            out.println()
        }
    }

    @JvmStatic
    fun printEnergyProportions(nd: NutritionData, verbose: Boolean, out: PrintStream) {
        // TODO pass in ColumnUnits and ColumnNamer
        val colNamer: ColumnNamer = EnglishColumnNames.instance
        out.println("Energy proportions (approx.)")
        // TODO get these lengths from ColumnNamer / ColumnUnits
        val proportionMap = nd.makeEnergyProportionsMap()
        val fmt = if (verbose) "%15s: %5.1f%%\n" else "%15s: %4.0f %%\n"
        for (col in proportionMap.keys) {
            out.printf(fmt, colNamer.getName(col!!), proportionMap[col])
        }
    }

    /*
     * Fixed width string format, left aligned
     */
    private fun strFmtL(n: Int): String {
        return "%-" + n + "s"
    }

    /*
     * Fixed width string format
     */
    private fun strFmt(n: Int): String {
        return "%" + n + "s"
    }

    /*
     * Fixed width string format
     */
    private fun strFmt(n: Int, leftAlign: Boolean): String {
        return "%" + (if (leftAlign) "-" else "") + n + "s"
    }

    /*
     * Ingredients printing parameters
     */
    private const val quantityWidth = 10
    private const val notesWidth = 25
    private const val nameWidth = PrintFormatting.nameWidth
    private const val start = " | "
    private const val sep = "  "
    private const val end = " |\n"
    private val lineFormat = start + strFmtL(nameWidth) + sep + strFmt(quantityWidth) + sep + strFmtL(notesWidth) + end
    private const val lineLength = nameWidth + notesWidth + quantityWidth + 2 * sep.length + start.length + end.length - 2
    private val hLine = " " + of("-").copies(lineLength).join()

    @JvmStatic
    fun printIngredients(ingredients: List<Ingredient>, out: PrintStream) {
        // XXX use printLine(text, widths), etc function
        out.printf(lineFormat, "Name", "Quantity", "Notes")
        out.println(hLine)
        for (i in ingredients) {
            // format:  <name>          (<notes>)     <quantity/serving>
            val iFood = i.ingredientFood
            val notes = i.notes
            val name = iFood.mediumName
            val noteString = notes ?: ""
            val quantityString = formatQuantity(i.quantity(), i.qtyUnit(), quantityWidth)
            out.printf(lineFormat, name, quantityString, noteString)
            // TODO replace quantity with serving if specified
            //Serving iServing = i.getServing();
            //out.printf(" %-8s", iServing != null ? "(" + i.servingCountString() + " " +  iServing.name() + ")" : "");
        }
        out.println(hLine)
    }

    // command line inputs
    // returns null if there was an error or input was invalid
    @JvmStatic
    fun getIntegerInput(`in`: BufferedReader, out: PrintStream, min: Int, max: Int): Int? {
        val input = getStringInput(`in`, out) ?: return null
        return try {
            input.toInt().takeIf { it in min..max }
        } catch (ignore: NumberFormatException) {
            out.println("Bad number format: '${input}'")
            null
        }
    }

    // command line inputs
    // returns null if there was an error or input was invalid
    @JvmStatic
    fun getDoubleInput(`in`: BufferedReader, out: PrintStream): Double? {
        val input = getStringInput(`in`, out) ?: return null
        return try {
            input.toDouble().takeIf { it.isFinite() }
        } catch (ignore: NumberFormatException) {
            out.printf("Bad number format: '%s'\n", input)
            null
        }
    }

    @JvmStatic
    fun getStringInput(`in`: BufferedReader, out: PrintStream): String? {
        return try {
            `in`.readLine()?.javaTrim()
        } catch (e: IOException) {
            out.println("Error reading input: " + e.message)
            null
        }
    }

    fun clearTerminal(out: PrintStream) {
        // this is what /usr/bin/clear outputs on my terminal
        //out.println("\u001b\u005b\u0048\u001b\u005b\u0032\u004a");
        // equivalent in octal
        out.println("\u001b\u005b\u0048\u001b\u005b\u0032\u004a")
    }

    @JvmStatic
    fun getChar(`in`: BufferedReader, out: PrintStream): Char {
        val input = getStringInput(`in`, out)
        return if (input.isNullOrEmpty()) '\u0000' else input[0]
    }
}