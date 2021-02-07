package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.ArgParsing
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.core.datatype.TypeCastException
import com.machfour.macros.objects.*
import com.machfour.macros.queries.FoodPortionQueries
import com.machfour.macros.queries.FoodQueries
import com.machfour.macros.persistence.CsvException
import com.machfour.macros.persistence.CsvImport.importFoodData
import com.machfour.macros.persistence.CsvImport.importRecipes
import com.machfour.macros.persistence.CsvImport.importServings
import java.io.FileReader
import java.io.IOException
import java.sql.SQLException

class Import(config: MacrosConfig) : CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "import"
        private val USAGE = "Usage: $programName $NAME [--clear] [--norecipes] [--nofoods] " +
                " [-f <foods.csv>] [-s <servings.csv>] [-r <recipes.csv>] [-i <ingredients.csv>]"
    }

    override fun printHelp() {
        out.println("Imports CSV data for foods, servings, recipes and ingredients into the database.")
        out.println("Only foods with index names not already in the database will be imported.")
        out.println("However, it will try to import all servings, and so will fail if duplicate servings exist.")
        out.println()
        out.println("Options:")
        out.println("  --clear               removes existing data before import")
        out.println("  --nofoods             don't import food and serving data (if --clear is used, do not clear)")
        out.println("  --norecipes           don't import recipe data (if --clear is used, do not clear)")
        out.println("  -f <foods.csv>        read custom foods file (default: ${config.foodCsvPath}")
        out.println("  -s <servings.csv>     read custom servings file (default: ${config.servingCsvPath}")
        out.println("  -r <recipes.csv>      read custom recipes file (default: ${config.recipeCsvPath}")
        out.println("  -i <ingredients.csv>  read custom ingredients file (default: ${config.ingredientsCsvPath}")
        out.println()
        out.println("Note: If any custom CSV files are specified, no default paths will be used.")

    }



    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 0
        }
        val doClear = args.contains("--clear")
        val noRecipes = args.contains("--norecipes")
        val noFoodsServings = args.contains("--nofoods")
        val foodCsvArg = ArgParsing.findArgumentFromFlag(args, "-f")
        val servingCsvArg = ArgParsing.findArgumentFromFlag(args, "-s")
        val recipeCsvArg = ArgParsing.findArgumentFromFlag(args, "-r")
        val ingredientCsvArg = ArgParsing.findArgumentFromFlag(args, "-i")
        val foodCsvOverride: String?
        when (foodCsvArg) {
            is ArgParsing.Result.KeyValFound -> foodCsvOverride = foodCsvArg.argument
            is ArgParsing.Result.ValNotFound -> {
                err.println("Error - '-f' flag given but no food.csv specified")
                return 1
            }
            else -> {
                foodCsvOverride = null
            }
        }
        // TODO servings, etc


        val foodCsvFile = foodCsvOverride ?: config.foodCsvPath
        val servingCsvFile = config.servingCsvPath
        val recipeCsvFile = config.recipeCsvPath
        val ingredientsCsvFile = config.ingredientsCsvPath
        val ds = config.dataSourceInstance
        try {
            if (doClear) {
                if (!noFoodsServings) {
                    out.println("Clearing existing foods, servings, nutrition data and ingredients...")
                    // have to clear in reverse order
                    // TODO Ingredients, servings, NutrientValues cleared by cascade?
                    FoodPortionQueries.deleteAllIngredients(ds)
                    ds.clearTable(Serving.table)
                    ds.clearTable(FoodNutrientValue.table)
                    ds.clearTable(Food.table)
                } else if (!noRecipes) {
                    out.println("Clearing existing recipes and ingredients...")
                    // nutrition data deleted by cascade
                    FoodPortionQueries.deleteAllIngredients(ds)
                    FoodQueries.deleteAllCompositeFoods(ds)
                } else {
                    out.println("Warning: nothing was cleared because both --nofoods and --norecipes were used")
                }
            }
            if (!noFoodsServings) {
                out.println("Importing foods and nutrition data into database...")
                FileReader(foodCsvFile).use { importFoodData(ds, it, false) }
                out.println("Saved foods and nutrition data")
                FileReader(servingCsvFile).use { importServings(ds, it, false) }
                out.println("Saved servings")
                out.println()
            }
            if (!noRecipes) {
                out.println("Importing recipes and ingredients into database...")
                FileReader(recipeCsvFile).use { recipeCsv ->
                    FileReader(ingredientsCsvFile).use { ingredientsCsv ->
                        importRecipes(ds, recipeCsv, ingredientsCsv)
                    }
                }
                out.println("Saved recipes and ingredients")
                out.println()
            }
        } catch (e1: SQLException) {
            out.println()
            err.println("SQL Exception occurred: ${e1.message}")
            return 1
        } catch (e2: IOException) {
            out.println()
            err.println("IO exception occurred: ${e2.message}")
            return 1
        } catch (e3: TypeCastException) {
            out.println()
            err.println("Type cast exception occurred: ${e3.message}")
            err.println("Please check the format of the CSV files")
            return 1
        } catch (e4: CsvException) {
            out.println()
            err.println("CSV import exception occurred: ${e4.message}")
            err.println("Please check the format of the CSV files")
        }

        out.println()
        out.println("Import completed successfully")
        return 0
    }
}
