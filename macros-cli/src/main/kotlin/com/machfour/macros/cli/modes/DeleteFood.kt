package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CliConfig
import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.cliGetChar
import com.machfour.macros.cli.utils.printFoodSummary
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.entities.Food
import com.machfour.macros.queries.deleteObject
import com.machfour.macros.queries.getFoodsByIndexName
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.sql.SqlException


class DeleteFood(config: CliConfig) : CommandImpl(config) {
    override val name = "deletefood"
    override val usage = "Usage: ${config.programName} $name <index name 1> [<index name 2>] [...]"

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 2
        } else if (args.size < 2) {
            println(usage)
            return 2
        }

        val db = config.database

        println("Retrieving foods...")
        println()

        val indexNamesToDelete = args.subList(1, args.size)
        val foodsToDelete: List<Food>
        try {
            val retrievedFoods = getFoodsByIndexName(db, indexNamesToDelete)
            foodsToDelete = ArrayList(retrievedFoods.values)
        } catch (e: SqlException) {
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
                db.openConnection()
                db.beginTransaction()
                for (f in foodsToDelete) {
                    // XXX will ON DELETE CASCADE just do what we want here?
                    deleteObject(db, FoodTable, f)
                    println("Deleted " + f.indexName)
                }
                db.endTransaction()
            } catch (e: SqlException) {
                db.rollbackTransaction()
                printlnErr("SQL Exception occurred while deleting foods: $e")
                printlnErr("No foods deleted")
                return 1
            } finally {
                try {
                    db.closeConnection()
                } catch (e: SqlException) {
                    printlnErr("Warning: SQL exception occurred when closing the DB")
                }

            }
        } else {
            println("No action performed")
        }
        return 0

    }
}
