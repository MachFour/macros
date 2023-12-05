package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CliConfig
import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.ArgParsingResult
import com.machfour.macros.cli.utils.findArgumentFromFlag
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.csv.*
import com.machfour.macros.entities.Serving
import com.machfour.macros.queries.clearTable
import com.machfour.macros.queries.deleteAllCompositeFoods
import com.machfour.macros.queries.deleteAllIngredients
import com.machfour.macros.schema.FoodNutrientValueTable
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.ServingTable
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.datatype.TypeCastException
import java.io.FileReader


private fun getCsvFile(args: List<String>, flag: String, default: String): String? {
    return when (val arg = findArgumentFromFlag(args, flag)) {
        is ArgParsingResult.KeyValFound -> arg.argument
        is ArgParsingResult.ValNotFound -> null
        else -> default
    }

}

internal fun clearFoodsAndServings(db: SqlDatabase) {
    // have to clear in reverse order
    // TODO Ingredients, servings, NutrientValues cleared by cascade?
    deleteAllIngredients(db)
    clearTable(db, ServingTable)
    clearTable(db, FoodNutrientValueTable)
    clearTable(db, FoodTable)
}

internal fun clearRecipes(db: SqlDatabase) {
    // nutrition data deleted by cascade
    deleteAllIngredients(db)
    deleteAllCompositeFoods(db)
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
        val doRecipes = !args.contains("--norecipes")
        val doFoodsAndServings = !args.contains("--nofoods")


        val foodCsvFile = getCsvFile(args, "-f", config.foodCsvPath) ?: run {
            printlnErr("'-f' must specify <food.csv> path")
            return 1
        }

        val servingCsvFile = getCsvFile(args, "-s", config.servingCsvPath) ?: run {
            printlnErr("'-s' flag must specify <serving.csv> path")
            return 1
        }

        val recipeCsvFile = getCsvFile(args, "-r", config.recipeCsvPath) ?: run {
            printlnErr("'-r' flag must specify <recipe.csv> path")
            return 1
        }

        val ingredientsCsvFile = getCsvFile(args, "-i", config.ingredientsCsvPath) ?: run {
            printlnErr("'-i' must specify <ingredient.csv> path")
            return 1
        }

        println()

        val db = config.database
        try {
            if (doClear) {
                if (doFoodsAndServings) {
                    println("Clearing existing foods, servings, nutrition data and ingredients")
                    clearFoodsAndServings(db)
                } else if (doRecipes) {
                    println("Clearing existing recipes and ingredients")
                    clearRecipes(db)
                } else {
                    println("Warning: nothing was cleared because both --nofoods and --norecipes were used")
                }
                println()
            }
            if (doFoodsAndServings) {
                println("Importing foods and nutrition data from $foodCsvFile...")

                val foodKeyCol = if (foodCsvFile.contains("nuttab")) {
                    FoodTable.NUTTAB_INDEX
                } else {
                    FoodTable.INDEX_NAME
                }

                val csvFoods = FileReader(foodCsvFile).use { readFoodData(it.readText(), foodKeyCol) }
                saveImportedFoods(db, csvFoods, foodKeyCol).takeIf { it.isNotEmpty() }?.let {
                    println("Note: the following ${it.size} duplicate foods were not imported")
                    it.forEach { (_, food) -> println(food.indexName) }
                }

                println("Saved foods and nutrition data")
                println()

                println("Importing servings from $servingCsvFile")
                val duplicatedServings: Map<Long, Serving>
                FileReader(servingCsvFile).use { reader ->
                    duplicatedServings = importServings(db, reader.readText(), foodKeyCol, true)
                }
                println("Saved servings")
                println("Note: skipped ${duplicatedServings.size} duplicated servings")
                println()
            }
            if (doRecipes) {
                println("Importing recipes and ingredients from $recipeCsvFile...")
                FileReader(recipeCsvFile).use { recipeReader ->
                    FileReader(ingredientsCsvFile).use { ingredientsReader ->
                        importRecipes(db, recipeReader.readText(), ingredientsReader.readText())
                    }
                }
                println("Saved recipes and ingredients")
                println()
            }
        } catch (e1: SqlException) {
            println()
            printlnErr("SQL error: ${e1.message}")
            return 1
        } catch (e2: CsvException) {
            println()
            printlnErr("CSV error: ${e2.message}")
            printlnErr("Please check the format of the CSV files")
            return 1
        } catch (e3: TypeCastException) {
            println()
            printlnErr("Type error: ${e3.message}")
            printlnErr("Please check the format of the CSV files")
            return 1
        }

        println()
        println("Import completed successfully")
        return 0
    }
}
