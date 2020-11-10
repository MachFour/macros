package com.machfour.macros.cli.utils

import com.machfour.macros.names.EnglishColumnNames
import com.machfour.macros.objects.*
import com.machfour.macros.objects.Unit
import com.machfour.macros.objects.inbuilt.Nutrients
import com.machfour.macros.util.PrintFormatting
import com.machfour.macros.util.PrintFormatting.formatQuantity
import com.machfour.macros.util.StringJoiner
import com.machfour.macros.util.UnicodeUtils
import com.machfour.macros.util.UnicodeUtils.countDoubleWidthChars
import java.io.PrintStream
import kotlin.collections.ArrayList

object MealPrinter {
    private const val columnSep = "   "
    private val conciseTableCols = listOf(
          Nutrients.ENERGY
        , Nutrients.PROTEIN
        , Nutrients.FAT
        , Nutrients.CARBOHYDRATE
    )

    private val verboseTableCols = listOf(
          Nutrients.ENERGY
        , Nutrients.PROTEIN
        , Nutrients.FAT
        , Nutrients.SATURATED_FAT
        , Nutrients.CARBOHYDRATE
        , Nutrients.SUGAR
        , Nutrients.FIBRE
        , Nutrients.SODIUM
        , Nutrients.CALCIUM
    )

    /*
     * Prints a row of a table (helper method for printMeal())
     */
    private fun printRow(row: List<String>, widths: List<Int>, rightAlign: List<Boolean>, sep: String, out: PrintStream) {
        assert(row.size == widths.size && row.size == rightAlign.size)
        for (i in row.indices) {
            out.print(UnicodeUtils.formatString(row[i], widths[i], !rightAlign[i]) + sep)
        }
        out.println()
    }

    private fun nutritionDataToRow(name: String, nd: NutritionData, qty: Double, unit: Unit, verbose: Boolean): List<String> {
        val nutrientColumns = if (verbose) verboseTableCols else conciseTableCols
        val row: MutableList<String> = ArrayList(nutrientColumns.size + 2)
        // add food name
        row += name
        val nutrientWidth = if (verbose) PrintFormatting.longDataWidth else PrintFormatting.shortDataWidth
        // add nutrients, formatting to be the appropriate width
        for (nutrient in nutrientColumns) {
            row += formatQuantity(nd, nutrient, width = nutrientWidth, unitWidth = 2, withDp = verbose)
        }
        // add quantity and unit
        row.add(formatQuantity(qty, unit, width = PrintFormatting.servingWidth, unitWidth = 2))
        return row
    }

    fun printMeal(meal: Meal, verbose: Boolean, out: PrintStream) {
        val nutrientCols = if (verbose) verboseTableCols else conciseTableCols
        //Columns: first the food name, then one for each nutrient, then quantity/serving
        val numCols = 1 + nutrientCols.size + 1
        // holds the meal name and labels for each nutrient column
        val headingRow: MutableList<String> = ArrayList(numCols)
        val rowWidths: MutableList<Int> = ArrayList(numCols)
        val rightAlign: MutableList<Boolean> = ArrayList(numCols)
        // first column has meal name (heading) and food names for other rows
        headingRow.add(meal.name)
        rowWidths.add(PrintFormatting.nameWidth)
        rightAlign.add(false)
        // next columns have names for each nutrient (heading) then corresponding data
        for (col in nutrientCols) {
            if (verbose) {
                headingRow.add(EnglishColumnNames.longerNutrientNames.getValue(col))
                rowWidths.add(PrintFormatting.longDataWidth)
            } else {
                headingRow.add(EnglishColumnNames.briefNutrientNames.getValue(col))
                rowWidths.add(PrintFormatting.shortDataWidth)
            }
            rightAlign.add(true)
        }
        // last column is quantity, so is a bit longer
        headingRow.add(EnglishColumnNames.briefNutrientNames.getValue(Nutrients.QUANTITY))
        rowWidths.add(PrintFormatting.servingWidth)
        rightAlign.add(true)

        // row separator spans all columns plus each separator, but we discount the space
        // after the last separator
        val rowSepLength = rowWidths.sum() + rowWidths.size * columnSep.length - 1
        val rowSeparator = StringJoiner.of("=").copies(rowSepLength).join()
        printRow(headingRow, rowWidths, rightAlign, columnSep, out)
        out.println(rowSeparator)
        // now we get to the actual data
        val dataRows: MutableList<List<String>> = ArrayList()
        for (fp in meal.getFoodPortions()) {
            val name = fp.food.mediumName
            val nd = fp.nutritionData.withDefaultUnits()
            dataRows.add(nutritionDataToRow(name, nd, fp.quantity, fp.qtyUnit, verbose))
        }
        for (row in dataRows) {
            printRow(row, rowWidths, rightAlign, columnSep, out)
        }
        // now print total
        out.println(rowSeparator)
        val totalName = "Total for ${meal.name}"
        val totalNd = meal.nutritionTotal
        // for total data, just use the quantity and unit from the sum
        val totalRow = nutritionDataToRow(totalName, totalNd, totalNd.quantity, totalNd.qtyUnit, verbose)
        printRow(totalRow, rowWidths, rightAlign, columnSep, out)
    }

    fun printMeals(meals: Collection<Meal>, out: PrintStream, verbose: Boolean = false, per100: Boolean = false, grandTotal: Boolean = true) {
        out.println("============")
        out.println("Meal totals:")
        out.println("============")
        out.println()
        for (m in meals) {
            if (m.getFoodPortions().isEmpty()) {
                out.println("Meal ${m.name} has no recorded data")
            } else {
                printMeal(m, verbose, out)
                out.println()
                if (per100) {
                    out.println("== Nutrient total per 100g ==")
                    CliUtils.printPer100g(m.nutritionTotal, verbose, out)
                    out.println("=============================")
                    out.println()
                }
            }
        }
        if (grandTotal) {
            val allNutData: MutableList<NutritionData> = ArrayList()
            for (m in meals) {
                allNutData.add(m.nutritionTotal)
            }
            val totalNutData = NutritionCalculations.sum(allNutData)
            out.println("====================")
            out.println("Total for all meals:")
            out.println("====================")
            CliUtils.printNutritionData(totalNutData, verbose, out)
            out.println()
            CliUtils.printEnergyProportions(totalNutData, verbose, out)
        }
    }

}