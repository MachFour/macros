package com.machfour.macros.cli.utils

import com.machfour.macros.names.DefaultDisplayStrings
import com.machfour.macros.names.EnglishColumnNames
import com.machfour.macros.names.NutrientStrings
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.nutrients.Nutrients
import com.machfour.macros.util.defaultNutrientsToPrint
import com.machfour.macros.util.formatNutrientData
import com.machfour.macros.util.javaTrim
import java.io.BufferedReader
import java.io.IOException
import java.io.PrintStream

fun PrintStream.printNutrientData(foodNutrientData: FoodNutrientData, verbose: Boolean) {
    val string = formatNutrientData(
        data = foodNutrientData,
        displayStrings = DefaultDisplayStrings,
        nutrients = defaultNutrientsToPrint,
        withDp = verbose,
        monoSpaceAligned = true
    )
    println(string)
}

fun PrintStream.printEnergyProportions(
    foodNutrientData: FoodNutrientData,
    verbose: Boolean,
    colNames: NutrientStrings = EnglishColumnNames
) {
    println("Energy proportions (approx.)")
    val fmt = if (verbose) "%15s: %5.1f%%\n" else "%15s: %4.0f %%\n"
    for (n in energyProportionNutrientsToPrint) {
        printf(fmt, colNames.getFullName(n), foodNutrientData.getEnergyProportion(n) *100)
    }
}


private val energyProportionNutrientsToPrint = setOf(
    Nutrients.PROTEIN,
    Nutrients.FAT,
    Nutrients.SATURATED_FAT,
    Nutrients.CARBOHYDRATE,
    Nutrients.SUGAR,
    Nutrients.FIBRE
)

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