package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.csv.exportTableToCsv
import com.machfour.macros.entities.*
import com.machfour.macros.entities.Unit
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.Table
import com.machfour.macros.util.joinFilePath
import java.io.FileWriter
import java.io.IOException
import java.sql.SQLException

class Export(config: MacrosConfig): CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "export"
        private const val USAGE = "Usage: $programName $NAME [output dir]"
    }

    override fun printHelp() {
        println("Exports complete CSV data (foods and servings) from the database.")
        println("Please specify the containing directory. It will be created if it doesn't exist.")
    }

    @Throws(SQLException::class, IOException::class)
    private fun <M : MacrosEntity<M>> SqlDatabase.doExport(outDir: String, t: Table<M>) {
        println("Exporting ${t.name} table...")
        val outCsvPath = joinFilePath(outDir, t.name + ".csv")
        FileWriter(outCsvPath).use { exportTableToCsv(this, t, it) }
    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 0
        }
        val outputDir = if (args.size >= 2) args[1] else config.defaultCsvOutputDir
        try {
            with(config.database) {
                doExport(outputDir, Food.table)
                doExport(outputDir, FoodNutrientValue.table)
                doExport(outputDir, Serving.table)
                doExport(outputDir, FoodPortion.table)
                doExport(outputDir, Ingredient.table)
                doExport(outputDir, Meal.table)
                doExport(outputDir, Unit.table)
                doExport(outputDir, Nutrient.table)
            }
        } catch (e: SQLException) {
            return handleException(e)
        } catch (e: IOException) {
            return handleException(e)
        }
        println()
        println("Export completed successfully")
        return 0
    }

    private fun handleException(e: Exception): Int {
        println()
        printlnErr("Exception occurred (${e.javaClass}). Message: ${e.message}")
        return 1
    }

}