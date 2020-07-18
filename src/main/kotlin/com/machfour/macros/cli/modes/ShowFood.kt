package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.CliUtils
import com.machfour.macros.core.Schema
import com.machfour.macros.objects.CompositeFood
import com.machfour.macros.objects.Food
import com.machfour.macros.objects.FoodType
import com.machfour.macros.objects.Ingredient
import com.machfour.macros.objects.NutritionData
import com.machfour.macros.objects.Serving
import com.machfour.macros.queries.FoodQueries
import com.machfour.macros.storage.MacrosDataSource
import com.machfour.macros.util.DateTimeUtils

import java.io.PrintStream
import java.sql.SQLException
import java.time.format.DateTimeFormatter
import java.util.Objects


class ShowFood : CommandImpl(NAME, USAGE) {
    companion object {
        private const val NAME = "show"
        private val USAGE = "Usage: $programName $NAME <index_name>"

        fun printFoodSummary(f: Food, out: PrintStream) {
            val dateFormat = DateTimeUtils.LOCALIZED_DATETIME_MEDIUM
            out.printf("Name:          %s\n", f.mediumName)
            out.printf("Notes:         %s\n", Objects.toString(f.notes, "(no notes)"))
            out.printf("Category:      %s\n", f.foodCategory)
            out.println()
            out.printf("Type:          %s\n", f.foodType.niceName)
            out.printf("Created on:    %s\n", dateFormat.format(f.createInstant))
            out.printf("Last modified: %s\n", dateFormat.format(f.modifyInstant))
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
            var nd = f.getNutritionData()
            val unit = nd.qtyUnitAbbr()
            out.println("Nutrition data (source: ${nd.getData(Schema.NutritionDataTable.DATA_SOURCE)})")
            out.println()

            if (nd.density != null) {
                // width copied from printFoodSummary()
                out.printf("Density:       %.2f (g/ml)\n", nd.density)
                out.println()
            }

            // if entered not per 100g, print both original amount and per 100 g
            if (nd.quantity != 100.0) {
                out.printf("Per %.0f%s:\n", nd.quantity, unit)
                CliUtils.printNutritionData(nd, verbose, out)
                out.println()
                nd = nd.rescale(100.0)
            }
            out.printf("Per %.0f%s:\n", nd.quantity, unit) // should now be 100
            CliUtils.printNutritionData(nd, verbose, out)
            out.println()

            /*
             * Servings
             */
            out.println("================================")
            out.println()
            out.println("Servings:")
            out.println()

            val servings = f.getServings()
            if (!servings.isEmpty()) {
                for (s in servings) {
                    out.printf(" - %s: %.1f%s\n", s.name, s.quantity, s.qtyUnit.abbr)
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
            val ingredients = cf.getIngredients()
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
