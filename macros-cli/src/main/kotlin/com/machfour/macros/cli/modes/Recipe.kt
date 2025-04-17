package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CliConfig
import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.cliGetChar
import com.machfour.macros.cli.utils.printIngredients
import com.machfour.macros.cli.utils.printNutrientData
import com.machfour.macros.entities.CompositeFood
import com.machfour.macros.formatting.toString
import com.machfour.macros.ingredients.readRecipes
import com.machfour.macros.ingredients.saveRecipes
import com.machfour.macros.sql.SqlException
import java.io.FileReader
import java.io.IOException


class Recipe(config: CliConfig): CommandImpl(config) {
    override val name = "recipe"
    override val usage = "Usage: ${config.programName} $name <recipes.json>"

    override fun doAction(args: List<String>) : Int {
        if (args.contains("--help")) {
            printHelp()
            return 2
        } else if (args.size < 2) {
            println(usage)
            return 2
        }

        val ds = config.database
        val recipes = ArrayList<CompositeFood>()

        try {
            FileReader(args[1]).use { jsonReader ->
                println("Importing recipes...")
                recipes.addAll(readRecipes(jsonReader, ds))
            }
        } catch (e1: IOException) {
            println("IO exception occurred while reading recipes file: " + e1.message)
            return 1
        } catch (e2: SqlException) {
            println("SQL exception occurred while creating recipe objects: " + e2.message)
            return 1
        }

        if (recipes.isEmpty()) {
            println("No recipes read! Check the recipes file")
            return 2
        }

        println("The following recipes were found:")

        for (cf in recipes) {
            println()
            println("Name: " + cf.mediumName)
            println()
            println("Ingredients:")
            println()
            printIngredients(cf.ingredients)
            println()
            println("Nutrition Information:")
            println()
            val nd = cf.nutrientData
            val qty = nd.perQuantity
            val unit = qty.unit
            // if entered not per 100g, print both original amount and per 100 g
            if (qty.amount != 100.0) {
                println("Per ${qty.amount.toString(0)}$unit:")
                printNutrientData(nd, false)
                println()
            }
            println("Per 100${unit.abbr}:")
            printNutrientData(nd.rescale(100.0, unit), false)
            println()
            println("================================================")
            println()
        }

        val article = if (recipes.size == 1) "this" else "these"
        val plural = if (recipes.size == 1) "" else "s"
        print("Would you like to save $article food$plural? [y/N] ")
        val response = cliGetChar()
        println()
        if (response == 'y' || response == 'Y') {
            try {
                saveRecipes(recipes, ds)
                println("Recipes saved!")
            } catch (e: SqlException) {
                println("SQL exception occurred while saving recipe objects: " + e.message)
                println("Recipes not saved")
                return 1
            }
        } else {
            println("Okay.")
        }
        return 0
    }
}
