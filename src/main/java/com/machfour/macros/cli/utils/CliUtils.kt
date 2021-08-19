package com.machfour.macros.cli.utils

import com.machfour.macros.names.ColumnNamer
import com.machfour.macros.names.DefaultColumnStrings
import com.machfour.macros.names.EnglishColumnNames
import com.machfour.macros.entities.Ingredient
import com.machfour.macros.nutrientdata.FoodNutrientData
import com.machfour.macros.names.EnglishUnitNames
import com.machfour.macros.entities.inbuilt.Nutrients
import com.machfour.macros.util.javaTrim
import com.machfour.macros.util.PrintFormatting
import com.machfour.macros.util.stringJoin
import java.io.BufferedReader
import java.io.IOException
import java.io.PrintStream

object CliUtils {
    fun FoodNutrientData.printNutrientData(verbose: Boolean, out: PrintStream) {
        val string = PrintFormatting.nutrientData(
            nd = this,
            colStrings = DefaultColumnStrings.instance,
            nutrients = PrintFormatting.defaultNutrientsToPrint,
            withDp = verbose,
            monoSpaceAligned = true
        )
        out.println(string)
    }

    private val energyProportionNutrientsToPrint = setOf(
        Nutrients.PROTEIN,
        Nutrients.FAT,
        Nutrients.SATURATED_FAT,
        Nutrients.CARBOHYDRATE,
        Nutrients.SUGAR,
        Nutrients.FIBRE
    )

    fun FoodNutrientData.printEnergyProportions(
        verbose: Boolean,
        out: PrintStream,
        colNames: ColumnNamer = EnglishColumnNames.instance
    ) {
        out.println("Energy proportions (approx.)")
        val fmt = if (verbose) "%15s: %5.1f%%\n" else "%15s: %4.0f %%\n"
        for (n in energyProportionNutrientsToPrint) {
            out.printf(fmt, colNames.getFullName(n), getEnergyProportion(n)*100)
        }
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

    /*
     * Ingredients printing parameters
     */
    private const val quantityWidth = 10
    private const val notesWidth = 25
    private const val nameWidth = MealPrinter.nameWidth
    private const val start = " | "
    private const val sep = "  "
    private const val end = " |\n"
    private val lineFormat = start + strFmtL(nameWidth) + sep + strFmt(quantityWidth) + sep + strFmtL(notesWidth) + end
    private const val lineLength = nameWidth + notesWidth + quantityWidth + 2 * sep.length + start.length + end.length - 2
    private val hLine = " " + stringJoin(listOf("-"), copies = lineLength)

    fun printIngredients(ingredients: List<Ingredient>, out: PrintStream) {
        // XXX use printLine(text, widths), etc function
        out.printf(lineFormat, "Name", "Quantity", "Notes")
        out.println(hLine)
        for (i in ingredients) {
            // format:  <name>          (<notes>)     <quantity/serving>
            val iFood = i.food
            val notes = i.notes
            val name = iFood.mediumName
            val noteString = notes ?: ""
            val quantityString = PrintFormatting.quantity(
                    qty = i.quantity,
                    unit = i.qtyUnit,
                    unitNamer = EnglishUnitNames.instance,
                    width = quantityWidth,
                    unitWidth = 2)
            out.printf(lineFormat, name, quantityString, noteString)
            // TODO replace quantity with serving if specified
            //Serving iServing = i.getServing();
            //out.printf(" %-8s", iServing != null ? "(" + i.servingCountString() + " " +  iServing.name() + ")" : "");
        }
        out.println(hLine)
    }

    // command line inputs
    // returns null if there was an error or input was invalid
    fun getIntegerInput(input: BufferedReader, out: PrintStream, min: Int, max: Int): Int? {
        val inputString = getStringInput(input, out) ?: return null
        return try {
            inputString.toInt().takeIf { it in min..max }
        } catch (ignore: NumberFormatException) {
            out.println("Bad number format: '$inputString'")
            null
        }
    }

    // command line inputs
    // returns null if there was an error or input was invalid
    fun getDoubleInput(input: BufferedReader, out: PrintStream): Double? {
        val inputString = getStringInput(input, out) ?: return null
        return try {
            inputString.toDouble().takeIf { it.isFinite() }
        } catch (ignore: NumberFormatException) {
            out.println("Bad number format: '$inputString'")
            null
        }
    }

    fun getStringInput(input: BufferedReader, out: PrintStream): String? {
        return try {
            input.readLine()?.javaTrim()
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

    fun getChar(input: BufferedReader, out: PrintStream): Char {
        val inputString = getStringInput(input, out)
        return if (inputString.isNullOrEmpty()) '\u0000' else inputString[0]
    }
}