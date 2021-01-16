package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.Table
import com.machfour.macros.core.datatype.TypeCastException
import com.machfour.macros.objects.*
import com.machfour.macros.objects.Unit
import com.machfour.macros.persistence.CsvRestore
import com.machfour.macros.persistence.MacrosDataSource
import com.machfour.macros.util.FileUtils.joinPath
import java.io.FileReader
import java.io.IOException
import java.sql.SQLException

class Restore(config: MacrosConfig): CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "restore"
        private val USAGE = "Usage: $programName $NAME [backup dir]"

    }

    override fun printHelp() {
        out.println("Restores the database using CSV data saved using the 'export' command.")
        out.println("Warning: this will overwrite all data in the database!")
    }

    @Throws(SQLException::class, IOException::class, TypeCastException::class)
    private fun <M : MacrosEntity<M>> restoreTable(db: MacrosDataSource, exportDir: String, t: Table<M>) {
        out.println("Restoring " + t.name + " table...")
        val csvPath = joinPath(exportDir, t.name + ".csv")
        FileReader(csvPath).use { csvData -> CsvRestore.restoreTable(db, t, csvData, out) }
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
        val ds = config.dataSourceInstance
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
        out.println()
        out.println("Database successfully restored from CSV data in $csvDir")
        return 0
    }

    private fun handleException(e: Exception) : Int {
        out.println()
        err.println("Exception occurred (${e.javaClass}). Message: ${e.message}")
        return 1
    }
}