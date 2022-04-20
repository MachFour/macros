package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CliConfig
import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.printFoodList
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.queries.foodSearch
import com.machfour.macros.queries.getFoodsById
import com.machfour.macros.sql.SqlException

class SearchFood(config: CliConfig) : CommandImpl(config) {
    override val name = "search"
    override val usage = "Usage: ${config.programName} $name <keyword>"

    override fun doAction(args: List<String>): Int {
        if (args.size == 1 || args.contains("--help")) {
            printHelp()
            if (args.size == 1) {
                println("Please enter a search keyword for the food database")
            }
            return 0
        }
        val ds = config.database
        val searchString = args[1]
        val keywords = searchString.split(Regex("\\s"))
        val resultFoods = try {
            val resultIds = foodSearch(ds, keywords)
            if (resultIds.isNotEmpty()) {
                getFoodsById(ds, resultIds)
            } else {
                emptyMap()
            }
        } catch (e: SqlException) {
            printlnErr("SQL exception occurred: ${e.message}")
            return 1
        }
        if (resultFoods.isEmpty()) {
            println("No matches for search string '${searchString}'")
        } else {
            println("Search results:")
            println()
            printFoodList(resultFoods.values)
        }
        return 0
    }

}