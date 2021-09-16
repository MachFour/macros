package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.csv.restoreTable
import com.machfour.macros.entities.*
import com.machfour.macros.entities.Unit
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.datatype.TypeCastException
import com.machfour.macros.util.joinFilePath
import java.io.FileReader
import java.io.IOException
import java.sql.SQLException

class Restore(config: MacrosConfig): CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "restore"
        private const val USAGE = "Usage: $programName $NAME [backup dir]"

    }

    override fun printHelp() {
        println("Restores the database using CSV data saved using the 'export' command.")
        println("Warning: this will overwrite all data in the database!")
    }

    @Throws(SQLException::class, IOException::class, TypeCastException::class)
    private fun <M : MacrosEntity<M>> SqlDatabase.restoreTable(exportDir: String, t: Table<M>) {
        println("Restoring " + t.name + " table...")
        val csvPath = joinFilePath(exportDir, t.name + ".csv")
        FileReader(csvPath).use { csvData -> restoreTable(this, t, csvData) }
    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 0
        }

        // default output dir
        val csvDir = if (args.size >= 2) args[1] else config.defaultCsvOutputDir

        try {
            with(config.database) {
                restoreTable(csvDir, Unit.table)
                restoreTable(csvDir, Nutrient.table)
                restoreTable(csvDir, Food.table)
                restoreTable(csvDir, FoodNutrientValue.table)
                restoreTable(csvDir, Serving.table)
                restoreTable(csvDir, Meal.table)
                restoreTable(csvDir, FoodPortion.table)
                restoreTable(csvDir, Ingredient.table)
            }
        } catch (e: SQLException) {
            return handleException(e)
        } catch (e: IOException) {
            return handleException(e)
        } catch (e: TypeCastException) {
            return handleException(e)
        }
        println()
        println("Database successfully restored from CSV data in $csvDir")
        return 0
    }

    private fun handleException(e: Exception) : Int {
        println()
        printlnErr("Exception occurred (${e.javaClass}). Message: ${e.message}")
        return 1
    }
}