package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CliConfig
import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.entities.Food
import com.machfour.macros.json.JsonFood
import com.machfour.macros.json.serialiseFoodsToZipFile
import com.machfour.macros.queries.StaticDataSource
import com.machfour.macros.sql.SqlException
import kotlinx.coroutines.runBlocking
import java.io.IOException

class Json(config: CliConfig): CommandImpl(config) {
    override val name = "json"
    override val usage = "Usage: ${config.programName} $name <output.zip>"

    override fun printHelp() {
        println(usage)
        println(
            "Exports foods and servings to a .zip file containing one JSON file for each food. " +
            "Some information such as meal records and creation/modification times is not preserved."
        )
    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 0
        }
        if (args.size < 2) {
            println(usage)
            return 2
        }

        val destZip = args[1]

        println("Exporting foods to $destZip")

        val dataSource = StaticDataSource(config.database)
        try {
            var foods: Map<Long, Food> = HashMap()
            runBlocking {
                dataSource.getAllFoods().collect { foods = it }
            }
            serialiseFoodsToZipFile(foods.values.map { JsonFood(it) }, destZip)
        } catch (e: SqlException) {
            return handleException(e)
        } catch (e: IOException) {
            return handleException(e)
        }

        println()
        println("JSON export completed successfully")
        return 0
    }

    private fun handleException(e: Exception): Int {
        println()
        printlnErr("Exception occurred (${e.javaClass}). Message: ${e.message}")
        return 1
    }

}
