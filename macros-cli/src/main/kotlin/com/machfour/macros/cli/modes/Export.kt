package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CliConfig
import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.csv.exportFoodData
import com.machfour.macros.csv.exportServings
import com.machfour.macros.jvm.currentTimeString
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.ServingTable
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.Table
import java.io.FileWriter
import java.io.IOException

class Export(config: CliConfig): CommandImpl(config) {
    override val name = "export"
    override val usage = "Usage: ${config.programName} $name [output.zip]"

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
                it.write(exportFoodData(config.database, foodKeyCol))
            }

            println("Exporting servings to $outputServingCsvName")

            FileWriter(outputServingCsvName).use {
                it.write(exportServings(config.database, foodKeyCol, emptySet()))
            }

        } catch (e: SqlException) {
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