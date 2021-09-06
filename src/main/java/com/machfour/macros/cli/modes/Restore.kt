package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.entities.*
import com.machfour.macros.entities.Unit
import com.machfour.macros.queries.MacrosDataSource
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.datatype.TypeCastException
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
    private fun <M : MacrosEntity<M>> restoreTable(ds: MacrosDataSource, exportDir: String, t: Table<M>) {
        println("Restoring " + t.name + " table...")
        val csvPath = com.machfour.macros.util.joinFilePath(exportDir, t.name + ".csv")
        FileReader(csvPath).use { csvData ->
            com.machfour.macros.persistence.restoreTable(
                ds,
                t,
                csvData
            )
        }
    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 0
        }

        // default output dir
        var csvDir = config.defaultCsvOutputDir
        if (args.size >= 2) {
            csvDir = args[1]
        }
        val ds = config.dataSource
        try {
            restoreTable(ds, csvDir, Unit.table)
            restoreTable(ds, csvDir, Nutrient.table)
            restoreTable(ds, csvDir, Food.table)
            restoreTable(ds, csvDir, FoodNutrientValue.table)
            restoreTable(ds, csvDir, Serving.table)
            restoreTable(ds, csvDir, Meal.table)
            restoreTable(ds, csvDir, FoodPortion.table)
            restoreTable(ds, csvDir, Ingredient.table)
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