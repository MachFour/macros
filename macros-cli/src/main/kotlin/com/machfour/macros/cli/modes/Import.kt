package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CliConfig
import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.ArgParsingResult
import com.machfour.macros.cli.utils.findArgumentFromFlag
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.csv.CsvException
import com.machfour.macros.csv.importFoodData
import com.machfour.macros.csv.importRecipes
import com.machfour.macros.csv.importServings
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.Serving
import com.machfour.macros.queries.clearTable
import com.machfour.macros.queries.deleteAllCompositeFoods
import com.machfour.macros.queries.deleteAllIngredients
import com.machfour.macros.schema.FoodNutrientValueTable
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.ServingTable
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.datatype.TypeCastException
import java.io.FileReader
import java.io.IOException


private fun getCsvFile(args: List<String>, flag: String, default: String): String? {
    return when (val arg = findArgumentFromFlag(args, flag)) {
        is ArgParsingResult.KeyValFound -> arg.argument
        is ArgParsingResult.ValNotFound -> null
        else -> default
    }

}


class Import(config: CliConfig) : CommandImpl(config) {
    override val name = "import"
    override val usage = "Usage: ${config.programName} $name [--clear] [--norecipes] [--nofoods] " +
            " [-f <foods.csv>] [-s <servings.csv>] [-r <recipes.csv>] [-i <ingredients.csv>]"

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


        val foodCsvFile = getCsvFile(args, "-f", config.foodCsvPath) ?: run {
            printlnErr("Error - '-f' flag given but food.csv not specified")
            return 1
        }

        val servingCsvFile = getCsvFile(args, "-s", config.servingCsvPath) ?: run {
            printlnErr("Error - '-s' flag given but serving.csv not specified")
            return 1
        }

        val recipeCsvFile = getCsvFile(args, "-r", config.recipeCsvPath) ?: run {
            printlnErr("Error - '-r' flag given but recipe.csv not specified")
            return 1
        }

        val ingredientsCsvFile = getCsvFile(args, "-i", config.ingredientsCsvPath) ?: run {
            printlnErr("Error - '-i' flag given but ingredient.csv not specified")
            return 1
        }

        println()

        val db = config.database
        try {
            if (doClear) {
                if (!noFoodsServings) {
                    println("Clearing existing foods, servings, nutrition data and ingredients...")
                    // have to clear in reverse order
                    // TODO Ingredients, servings, NutrientValues cleared by cascade?
                    deleteAllIngredients(db)
                    clearTable(db, ServingTable)
                    clearTable(db, FoodNutrientValueTable)
                    clearTable(db, FoodTable)
                } else if (!noRecipes) {
                    println("Clearing existing recipes and ingredients...")
                    // nutrition data deleted by cascade
                    deleteAllIngredients(db)
                    deleteAllCompositeFoods(db)
                } else {
                    println("Warning: nothing was cleared because both --nofoods and --norecipes were used")
                }
                println()
            }
            if (!noFoodsServings) {
                println("Importing foods and nutrition data from $foodCsvFile...")

                val foodKeyCol = if (foodCsvFile.contains("nuttab")) {
                    FoodTable.NUTTAB_INDEX
                } else {
                    FoodTable.INDEX_NAME
                }

                val conflictingFoods: Map<String, Food>
                FileReader(foodCsvFile).use { reader ->
                    conflictingFoods = importFoodData(db, reader, foodKeyCol)
                }

                if (conflictingFoods.isNotEmpty()) {
                    println("Note: the following ${conflictingFoods.size} duplicate foods were not imported:")
                    conflictingFoods.forEach { println(it.key) }
                }

                println("Saved foods and nutrition data")
                println()

                println("Importing servings from $servingCsvFile")
                val duplicatedServings: Map<Long, Serving>
                FileReader(servingCsvFile).use { reader ->
                    duplicatedServings = importServings(db, reader, foodKeyCol, true)
                }
                println("Saved servings")
                println("Note: skipped ${duplicatedServings.size} duplicated servings")
                println()
            }
            if (!noRecipes) {
                println("Importing recipes and ingredients from $recipeCsvFile...")
                FileReader(recipeCsvFile).use { recipeReader ->
                    FileReader(ingredientsCsvFile).use { ingredientsReader ->
                        importRecipes(db, recipeReader, ingredientsReader)
                    }
                }
                println("Saved recipes and ingredients")
                println()
            }
        } catch (e1: SqlException) {
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
