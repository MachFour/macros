package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CliConfig
import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.printFood
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.entities.Food
import com.machfour.macros.queries.getFoodByIndexName
import com.machfour.macros.sql.SqlException


class ShowFood(config: CliConfig) : CommandImpl(config) {
    override val name = "show"
    override val usage = "Usage: ${config.programName} $name <index_name>"

    override fun doAction(args: List<String>) : Int {
        if (args.size == 1 || args.contains("--help")) {
            printHelp()
            if (args.size == 1) {
                println("Please enter the index name of the food to show")
            }
            return 2
        }
        val verbose = args.contains("-v") || args.contains("--verbose")

        val ds = config.database
        val indexName = args[1]
        val foodToList: Food?
        try {
            foodToList = getFoodByIndexName(ds, indexName)
        } catch (e: SqlException) {
            printlnErr("SQL exception occurred: ${e.message}")
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
