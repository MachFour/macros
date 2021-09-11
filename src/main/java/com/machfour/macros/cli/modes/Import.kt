package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.ArgParsingResult
import com.machfour.macros.cli.utils.findArgumentFromFlag
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.csv.CsvException
import com.machfour.macros.csv.importFoodData
import com.machfour.macros.csv.importRecipes
import com.machfour.macros.csv.importServings
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.Serving
import com.machfour.macros.queries.clearTable
import com.machfour.macros.queries.deleteAllCompositeFoods
import com.machfour.macros.queries.deleteAllIngredients
import com.machfour.macros.sql.datatype.TypeCastException
import java.io.FileReader
import java.io.IOException
import java.sql.SQLException

class Import(config: MacrosConfig) : CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "import"
        private const val USAGE = "Usage: $programName $NAME [--clear] [--norecipes] [--nofoods] " +
                " [-f <foods.csv>] [-s <servings.csv>] [-r <recipes.csv>] [-i <ingredients.csv>]"
    }

    override fun printHelp() {
        println("Imports CSV data for foods, servings, recipes and ingredients into the database.")
        println("Only foods with index names not already in the database will be imported.")
        println("However, it will try to import all servings, and so will fail if duplicate servings exist.")
        println()
        println("Options:")
        println("  --clear               removes existing data before import")
        println("  --nofoods             don't import food and serving data (if --clear is used, do not clear)")
        println("  --norecipes           don't import recipe data (if --clear is used, do not clear)")
        println("  -f <foods.csv>        read custom foods file (default: ${config.foodCsvPath}")
        println("  -s <servings.csv>     read custom servings file (default: ${config.servingCsvPath}")
        println("  -r <recipes.csv>      read custom recipes file (default: ${config.recipeCsvPath}")
        println("  -i <ingredients.csv>  read custom ingredients file (default: ${config.ingredientsCsvPath}")
        println()
        println("Note: If any custom CSV files are specified, no default paths will be used.")

    }


    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 0
        }
        val doClear = args.contains("--clear")
        val noRecipes = args.contains("--norecipes")
        val noFoodsServings = args.contains("--nofoods")
        val foodCsvArg = findArgumentFromFlag(args, "-f")
        val servingCsvArg = findArgumentFromFlag(args, "-s")
        val recipeCsvArg = findArgumentFromFlag(args, "-r")
        val ingredientCsvArg = findArgumentFromFlag(args, "-i")
        val foodCsvOverride: String?
        when (foodCsvArg) {
            is ArgParsingResult.KeyValFound -> foodCsvOverride = foodCsvArg.argument
            is ArgParsingResult.ValNotFound -> {
                printlnErr("Error - '-f' flag given but no food.csv specified")
                return 1
            }
            else -> {
                foodCsvOverride = null
            }
        }
        // TODO servings, etc
        println("servingCsvArg: $servingCsvArg")
        println("recipeCsvArg: $recipeCsvArg")
        println("ingredientCsvArg: $ingredientCsvArg")
        println()

        val foodCsvFile = foodCsvOverride ?: config.foodCsvPath
        val servingCsvFile = config.servingCsvPath
        val recipeCsvFile = config.recipeCsvPath
        val ingredientsCsvFile = config.ingredientsCsvPath
        val db = config.database
        try {
            if (doClear) {
                if (!noFoodsServings) {
                    println("Clearing existing foods, servings, nutrition data and ingredients...")
                    // have to clear in reverse order
                    // TODO Ingredients, servings, NutrientValues cleared by cascade?
                    deleteAllIngredients(db)
                    clearTable(db, Serving.table)
                    clearTable(db, FoodNutrientValue.table)
                    clearTable(db, Food.table)
                } else if (!noRecipes) {
                    println("Clearing existing recipes and ingredients...")
                    // nutrition data deleted by cascade
                    deleteAllIngredients(db)
                    deleteAllCompositeFoods(db)
                } else {
                    println("Warning: nothing was cleared because both --nofoods and --norecipes were used")
                }
            }
            if (!noFoodsServings) {
                println("Importing foods and nutrition data into database...")
                val conflictingFoods: Map<String, Food>
                FileReader(foodCsvFile).use {
                    conflictingFoods = importFoodData(db, it, false)
                }

                if (conflictingFoods.isNotEmpty()) {
                    println("The following foods will be imported; others had index names already present in the database:")
                    conflictingFoods.forEach { println(it.key) }
                }


                println("Saved foods and nutrition data")
                FileReader(servingCsvFile).use { importServings(db, it, false) }
                println("Saved servings")
                println()
            }
            if (!noRecipes) {
                println("Importing recipes and ingredients into database...")
                FileReader(recipeCsvFile).use { recipeCsv ->
                    FileReader(ingredientsCsvFile).use { ingredientsCsv ->
                        importRecipes(db, recipeCsv, ingredientsCsv)
                    }
                }
                println("Saved recipes and ingredients")
                println()
            }
        } catch (e1: SQLException) {
            println()
            printlnErr("SQL Exception occurred: ${e1.message}")
            return 1
        } catch (e2: IOException) {
            println()
            printlnErr("IO exception occurred: ${e2.message}")
            return 1
        } catch (e3: TypeCastException) {
            println()
            printlnErr("Type cast exception occurred: ${e3.message}")
            printlnErr("Please check the format of the CSV files")
            return 1
        } catch (e4: CsvException) {
            println()
            printlnErr("CSV import exception occurred: ${e4.message}")
            printlnErr("Please check the format of the CSV files")
        }

        println()
        println("Import completed successfully")
        return 0
    }
}
