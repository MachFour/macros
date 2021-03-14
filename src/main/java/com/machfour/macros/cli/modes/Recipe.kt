package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.CliUtils
import com.machfour.macros.cli.utils.CliUtils.printNutrientData
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.ingredients.IngredientsParser
import com.machfour.macros.entities.CompositeFood

import java.io.FileReader
import java.io.IOException
import java.sql.SQLException


class Recipe(config: MacrosConfig): CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "recipe"
        private val USAGE = "Usage: $programName $NAME <recipes.json>"
    }

    override fun doAction(args: List<String>) : Int {
        if (args.contains("--help")) {
            printHelp()
            return -1
        } else if (args.size < 2) {
            out.println(usage)
            return -1
        }

        val ds = config.dataSourceInstance
        val recipes = ArrayList<CompositeFood>()

        try {
            FileReader(args[1]).use { jsonReader ->
                out.println("Importing recipes...")
                recipes.addAll(IngredientsParser.readRecipes(jsonReader, ds))
            }
        } catch (e1: IOException) {
            out.println("IO exception occurred while reading recipes file: " + e1.message)
            return 1
        } catch (e2: SQLException) {
            out.println("SQL exception occurred while creating recipe objects: " + e2.message)
            return 1
        }

        if (recipes.isEmpty()) {
            out.println("No recipes read! Check the recipes file")
            return 2
        }

        out.println("The following recipes were found:")

        for (cf in recipes) {
            out.println()
            out.println("Name: " + cf.mediumName)
            out.println()
            out.println("Ingredients:")
            out.println()
            CliUtils.printIngredients(cf.ingredients, out)
            out.println()
            out.println("Nutrition Information:")
            out.println()
            val nd = cf.nutrientData
            val unit = nd.qtyUnitAbbr
            // if entered not per 100g, print both original amount and per 100 g
            if (nd.quantity != 100.0) {
                out.printf("Per %.0f%s:\n", nd.quantity, unit)
                nd.printNutrientData(false, out)
                out.println()
            }
            out.printf("Per %.0f%s:\n", nd.quantity, unit) // should now be 100
            nd.rescale100().printNutrientData(false, out)
            out.println()
            out.println("================================================")
            out.println()
        }

        val article = if (recipes.size == 1) "this" else "these"
        val plural = if (recipes.size == 1) "" else "s"
        out.print("Would you like to save $article food$plural? [y/N] ")
        val response = CliUtils.getChar(input, out)
        out.println()
        if (response == 'y' || response == 'Y') {
            try {
                IngredientsParser.saveRecipes(recipes, ds)
                out.println("Recipes saved!")
            } catch (e: SQLException) {
                out.println("SQL exception occurred while saving recipe objects: " + e.message)
                out.println("Recipes not saved")
                return 1
            }
        } else {
            out.println("Okay.")
        }
        return 0
    }
}
