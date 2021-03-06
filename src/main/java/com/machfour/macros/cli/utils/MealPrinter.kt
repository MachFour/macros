package com.machfour.macros.cli.utils

import com.machfour.macros.cli.utils.CliUtils.printEnergyProportions
import com.machfour.macros.cli.utils.CliUtils.printNutrientData
import com.machfour.macros.core.NutrientData
import com.machfour.macros.names.EnglishColumnNames
import com.machfour.macros.names.EnglishUnitNames
import com.machfour.macros.objects.*
import com.machfour.macros.objects.Unit
import com.machfour.macros.objects.inbuilt.Nutrients
import com.machfour.macros.util.PrintFormatting
import com.machfour.macros.util.StringJoiner
import com.machfour.macros.util.UnicodeUtils
import java.io.PrintStream

object MealPrinter {
    // printing widths
    const val nameWidth = 41
    const val servingWidth = 7
    const val shortDataWidth = 6
    const val longDataWidth = 7


    private const val columnSep = "  "
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

    private fun nutritionDataToRow(name: String, nd: NutrientData, qty: Double, unit: Unit, verbose: Boolean): List<String> {
        val nutrientColumns = if (verbose) verboseTableCols else conciseTableCols
        val nutrientWidth = if (verbose) longDataWidth else shortDataWidth

        return ArrayList<String>(nutrientColumns.size + 2).apply {
            // add food name
            this += name
            // add nutrients, formatting to be the appropriate width
            for (nutrient in nutrientColumns) {
                this += PrintFormatting.nutrient(
                    nd = nd,
                    n = nutrient,
                    width = nutrientWidth,
                    withDp = verbose
                )
            }
            // add quantity and unit
            this += PrintFormatting.quantity(
                    qty = qty,
                    unit = unit,
                    unitNamer = EnglishUnitNames.instance,
                    width = servingWidth,
                    unitWidth = 2,
                    alignLeft = false,
                    spaceBeforeUnit = true,
                    unitAlignLeft = true
            )
        }
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
        rowWidths.add(nameWidth)
        rightAlign.add(false)
        // next columns have names for each nutrient (heading) then corresponding data
        for (col in nutrientCols) {
            if (verbose) {
                headingRow.add(EnglishColumnNames.longerNutrientNames.getValue(col))
                rowWidths.add(longDataWidth)
            } else {
                headingRow.add(EnglishColumnNames.briefNutrientNames.getValue(col))
                rowWidths.add(shortDataWidth)
            }
            rightAlign.add(true)
        }
        // last column is quantity, so is a bit longer
        headingRow.add(EnglishColumnNames.briefNutrientNames.getValue(Nutrients.QUANTITY))
        rowWidths.add(servingWidth)
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
            val nd = fp.nutrientData.withDefaultUnits()
            dataRows.add(nutritionDataToRow(name, nd, fp.quantity, fp.qtyUnit, verbose))
        }
        for (row in dataRows) {
            printRow(row, rowWidths, rightAlign, columnSep, out)
        }
        // now print total
        out.println(rowSeparator)
        val totalName = "Total for ${meal.name}"
        val totalNd = meal.nutrientTotal()
        // for total data, just use the quantity and unit from the sum
        val totalRow =
            nutritionDataToRow(totalName, totalNd, totalNd.quantity, totalNd.qtyUnit, verbose)
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
                    m.nutrientTotal().rescale100().printNutrientData(verbose, out)
                    out.println("=============================")
                    out.println()
                }
            }
        }
        if (grandTotal) {
            out.println("====================")
            out.println("Total for all meals:")
            out.println("====================")
            NutrientData.sum(meals.map { it.nutrientTotal() }).let {
                it.printNutrientData(verbose, out)
                out.println()
                it.printEnergyProportions(verbose, out)
            }
        }
    }

}