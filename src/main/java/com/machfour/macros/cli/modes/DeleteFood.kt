package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.cliGetChar
import com.machfour.macros.cli.utils.printFoodSummary
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.entities.Food
import com.machfour.macros.queries.FoodQueries
import com.machfour.macros.queries.WriteQueries

import java.sql.SQLException


class DeleteFood(config: MacrosConfig) : CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "deletefood"
        private const val USAGE = "Usage: $programName $NAME <index name 1> [<index name 2>] [...]"
    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return -1
        } else if (args.size < 2) {
            println(usage)
            return -1
        }

        val ds = config.database

        println("Retrieving foods...")
        println()

        val indexNamesToDelete = args.subList(1, args.size)
        val foodsToDelete: List<Food>
        try {
            val retrievedFoods = FoodQueries.getFoodsByIndexName(ds, indexNamesToDelete)
            foodsToDelete = ArrayList(retrievedFoods.values)
        } catch (e: SQLException) {
            println("SQL Exception while retrieving foods: $e")
            return 1
        }

        when (foodsToDelete.size) {
            0 -> {
                println("No matching foods found!")
                return 2
            }
            1 -> {
                println("===== Food to delete =====")
                println()
            }
            else -> {
                println("===== Foods to delete =====")
                println()
            }
        }

        for (f in foodsToDelete) {
            printFoodSummary(f)
        }

        println()
        val plural = if (foodsToDelete.size != 1) "s" else ""
        print("Confirm delete " + foodsToDelete.size + " food" + plural + "? [y/N] ")
        val response = cliGetChar()
        println()
        if (response == 'y' || response == 'Y') {
            println("Deleting foods...")
            println()
            try {
                ds.openConnection()
                ds.beginTransaction()
                for (f in foodsToDelete) {
                    // XXX will ON DELETE CASCADE just do what we want here?
                    WriteQueries.deleteObject(ds, f)
                    println("Deleted " + f.indexName)
                }
                ds.endTransaction()
            } catch (e: SQLException) {
                printlnErr("SQL Exception occurred while deleting foods: $e")
                printlnErr("No foods deleted")
                return 1
            } finally {
                try {
                    ds.closeConnection()
                } catch (e: SQLException) {
                    printlnErr("Warning: SQL exception occurred when closing the DB")
                }

            }
        } else {
            println("No action performed")
        }
        return 0

    }
}
