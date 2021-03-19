package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.CliUtils
import com.machfour.macros.cli.utils.CliUtils.printNutrientData
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.entities.CompositeFood
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodType
import com.machfour.macros.queries.FoodQueries
import com.machfour.macros.util.DateTimeUtils

import java.io.PrintStream
import java.sql.SQLException


class ShowFood(config: MacrosConfig) : CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "show"
        private const val USAGE = "Usage: $programName $NAME <index_name>"

        fun printFoodSummary(f: Food, out: PrintStream) {
            val dateFormat = DateTimeUtils.LOCALIZED_DATETIME_MEDIUM
            out.println("Name:          ${f.longName}")
            out.println("Notes:         ${f.notes ?: ""}")
            out.println("Category:      ${f.foodCategory}")
            out.println()
            out.println("Type:          ${f.foodType}")
            out.println("Created on:    ${dateFormat.format(f.createInstant)}")
            out.println("Last modified: ${dateFormat.format(f.modifyInstant)}")
        }

        fun printFood(f: Food, verbose: Boolean, out: PrintStream) {
            out.println("============")
            out.println(" Food Data  ")
            out.println("============")
            out.println()
            out.println()
            printFoodSummary(f, out)

            out.println("================================")
            out.println()
            /*
             * Nutrition data
             */
            val nd = f.nutrientData
            val unit = nd.qtyUnitAbbr
            out.println("Nutrition data (source: ${f.dataSource})")
            out.println()

            if (f.density != null) {
                // width copied from printFoodSummary()
                out.printf("Density:       %.2f (g/ml)\n", f.density)
                out.println()
            }

            // if entered not per 100g, print both original amount and per 100 g
            if (nd.quantity != 100.0) {
                out.printf("Per %.0f%s:\n", nd.quantity, unit)
                nd.printNutrientData(verbose, out)
                out.println()
            }
            out.printf("Per %.0f%s:\n", nd.quantity, unit) // should now be 100
            nd.rescale100().printNutrientData(verbose, out)
            out.println()

            /*
             * Servings
             */
            out.println("================================")
            out.println()
            out.println("Servings:")
            out.println()

            val servings = f.servings
            if (servings.isNotEmpty()) {
                for (s in servings) {
                    out.println(" - ${s.name}: %.1f${s.qtyUnitAbbr}".format(s.quantity))
                }
            } else {
                out.println("(No servings recorded)")
            }

            out.println()
            /*
         * Ingredients
         */

            if (f.foodType != FoodType.COMPOSITE) {
                return
            }
            assert(f is CompositeFood)

            val cf = f as CompositeFood

            out.println("================================")
            out.println()
            out.println("Ingredients:")
            out.println()
            val ingredients = cf.ingredients
            if (ingredients.isNotEmpty()) {
                CliUtils.printIngredients(ingredients, out)
                out.println()
            } else {
                out.println("(No ingredients recorded (but there probably should be!)")
            }
            out.println("================================")
            out.println()
        }
    }

    override fun doAction(args: List<String>) : Int {
        if (args.size == 1 || args.contains("--help")) {
            printHelp()
            if (args.size == 1) {
                out.println("Please enter the index name of the food to show")
            }
            return -1
        }
        var verbose = false
        if (args.contains("-v") || args.contains("--verbose")) {
            verbose = true
        }

        val ds = config.dataSourceInstance
        val indexName = args[1]
        val foodToList: Food?
        try {
            foodToList = FoodQueries.getFoodByIndexName(ds, indexName)
        } catch (e: SQLException) {
            out.print("SQL exception occurred: ")
            out.println(e.errorCode)
            return 1
        }

        if (foodToList == null) {
            out.println("No food found with index name $indexName")
            return 1
        }

        printFood(foodToList, verbose, out)
        return 0
    }

}
