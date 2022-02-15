package com.machfour.macros.cli.modes
import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.csv.exportFoodData
import com.machfour.macros.csv.exportServings
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.ServingTable
import com.machfour.macros.sql.Table
import com.machfour.macros.util.currentTimeString
import java.io.FileWriter
import java.io.IOException
import java.sql.SQLException

class Export(config: MacrosConfig): CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "export"
        private const val USAGE = "Usage: $programName $NAME [output.zip]"
    }

    override fun printHelp() {
        println(
            "Exports user-added foods and servings to CSV files which can be re-imported when setting up" +
            "a new database. However, some information such as meal records and creation/modification times" +
            "is not preserved."
        )
    }

    private fun getExportCsvName(table: Table<*>) = "${table.name}-${currentTimeString()}.csv"

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 0
        }

        val foodKeyCol = FoodTable.INDEX_NAME
        val outputFoodCsvName = getExportCsvName(FoodTable)
        val outputServingCsvName = getExportCsvName(ServingTable)
        try {
            println("Exporting foods to $outputFoodCsvName")
            FileWriter(outputFoodCsvName).use {
                exportFoodData(config.database, it, foodKeyCol)
            }

            println("Exporting servings to $outputServingCsvName")

            FileWriter(outputServingCsvName).use {
                exportServings(config.database, it, foodKeyCol, emptySet())
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