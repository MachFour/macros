package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.Table
import com.machfour.macros.entities.*
import com.machfour.macros.entities.Unit
import com.machfour.macros.persistence.CsvBackup
import com.machfour.macros.persistence.MacrosDataSource
import com.machfour.macros.util.FileUtils.joinPath
import java.io.FileWriter
import java.io.IOException
import java.sql.SQLException

class Export(config: MacrosConfig): CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "export"
        private const val USAGE = "Usage: $programName $NAME [output dir]"
    }

    override fun printHelp() {
        out.println("Exports complete CSV data (foods and servings) from the database.")
        out.println("Please specify the containing directory. It will be created if it doesn't exist.")
    }

    @Throws(SQLException::class, IOException::class)
    private fun <M : MacrosEntity<M>> exportTable(db: MacrosDataSource, outDir: String, t: Table<M>) {
        out.println("Exporting ${t.name} table...")
        val outCsvPath = joinPath(outDir, t.name + ".csv")
        FileWriter(outCsvPath).use { CsvBackup.exportTable(db, t, it) }
    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 0
        }
        val outputDir = if (args.size >= 2) args[1] else config.defaultCsvOutputDir
        val ds = config.dataSourceInstance
        try {
            exportTable(ds, outputDir, Food.table)
            exportTable(ds, outputDir, FoodNutrientValue.table)
            exportTable(ds, outputDir, Serving.table)
            exportTable(ds, outputDir, FoodPortion.table)
            exportTable(ds, outputDir, Ingredient.table)
            exportTable(ds, outputDir, Meal.table)
            exportTable(ds, outputDir, Unit.table)
            exportTable(ds, outputDir, Nutrient.table)
        } catch (e: SQLException) {
            return handleException(e)
        } catch (e: IOException) {
            return handleException(e)
        }
        out.println()
        out.println("Export completed successfully")
        return 0
    }

    private fun handleException(e: Exception): Int {
        out.println()
        err.println("Exception occurred (${e.javaClass}). Message: ${e.message}")
        return 1
    }

}