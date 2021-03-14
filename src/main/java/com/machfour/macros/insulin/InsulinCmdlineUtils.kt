package com.machfour.macros.insulin

import com.machfour.macros.insulin.InsulinCalculations.insulinForCarbs
import com.machfour.macros.insulin.InsulinCalculations.insulinForProtein
import com.machfour.macros.nutrientdata.FoodNutrientData
import java.io.PrintStream

object InsulinCmdlineUtils {
    // parses a commmand line for the given flag, then tries to parse the string following that flag
    // in the form <ic ratio> <ic ratio>:<protein factor>, where <ic ratio> and <protein factor> are Doubles
    @Throws(java.lang.NumberFormatException::class)
    fun parseArgument(arg: String) : Pair<Double, Double?> {
        val insulinParams = arg.split(":", limit = 2)
        val icRatio = insulinParams[0].toDouble()
        val proteinFactor = insulinParams.takeIf { it.size == 2 }?.get(1)?.toDouble()
        return Pair(icRatio, proteinFactor)
    }

    private const val labelPrintWidth = "8"
    private const val unitsPrintWidth = "6"
    private val labels = listOf("Carbs", "Protein", "Total")

    fun printInsulin(out: PrintStream, nd: FoodNutrientData, icRatio: Double, proteinFactor: Double?) {
        out.println("========")
        out.println("Insulin:")
        out.println("========")
        out.println()

        val insulinParams = InsulinParams(icRatio, proteinFactor ?: 0.0)

        val forCarbs = insulinForCarbs(nd, insulinParams) ?: 0.0
        val forProtein = insulinForProtein(nd, insulinParams) ?: 0.0
        val total = forCarbs + forProtein

        val values = listOf(forCarbs, forProtein, total)
        for (i in 0 until 3) {
            out.printf("%${labelPrintWidth}s: %${unitsPrintWidth}.2fU\n", labels[i], values[i])
        }

        out.println()
        out.println("IC Ratio: $icRatio")
        out.println("Protein factor: $proteinFactor")

    }
}
