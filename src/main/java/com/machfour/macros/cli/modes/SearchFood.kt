package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.printErr
import com.machfour.macros.cli.utils.printFoodList
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.queries.foodSearch
import com.machfour.macros.queries.getFoodsById
import java.sql.SQLException

class SearchFood(config: MacrosConfig) : CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "search"
        private const val USAGE = "Usage: $programName $NAME <keyword>"
    }

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
        } catch (e: SQLException) {
            printErr("SQL exception occurred: ")
            printlnErr(e.message)
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