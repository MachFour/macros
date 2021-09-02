package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.printFood
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.entities.Food
import com.machfour.macros.queries.FoodQueries
import java.sql.SQLException


class ShowFood(config: MacrosConfig) : CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "show"
        private const val USAGE = "Usage: $programName $NAME <index_name>"

    }

    override fun doAction(args: List<String>) : Int {
        if (args.size == 1 || args.contains("--help")) {
            printHelp()
            if (args.size == 1) {
                println("Please enter the index name of the food to show")
            }
            return -1
        }
        val verbose = args.contains("-v") || args.contains("--verbose")

        val ds = config.database
        val indexName = args[1]
        val foodToList: Food?
        try {
            foodToList = FoodQueries.getFoodByIndexName(ds, indexName)
        } catch (e: SQLException) {
            print("SQL exception occurred: ")
            println(e.errorCode)
            return 1
        }

        if (foodToList == null) {
            println("No food found with index name $indexName")
            return 1
        }

        printFood(foodToList, verbose)
        return 0
    }

}
