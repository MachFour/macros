package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.CliUtils
import com.machfour.macros.objects.Food
import com.machfour.macros.queries.FoodQueries
import com.machfour.macros.queries.Queries

import java.sql.SQLException


class DeleteFood : CommandImpl(NAME, USAGE) {
    companion object {
        private const val NAME = "deletefood"
        private val USAGE = "Usage: $programName $NAME <index name 1> [<index name 2>] [...]"
    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return -1
        } else if (args.size < 2) {
            out.println(usage)
            return -1
        }

        val ds = config.dataSourceInstance

        out.println("Retrieving foods...")
        out.println()

        val indexNamesToDelete = args.subList(1, args.size)
        val foodsToDelete: List<Food>
        try {
            val retrievedFoods = FoodQueries.getFoodsByIndexName(ds, indexNamesToDelete)
            foodsToDelete = ArrayList(retrievedFoods.values)
        } catch (e: SQLException) {
            out.println("SQL Exception while retrieving foods: $e")
            return 1
        }

        when (foodsToDelete.size) {
            0 -> {
                out.println("No matching foods found!")
                return 2
            }
            1 -> {
                out.println("===== Food to delete =====")
                out.println()
            }
            else -> {
                out.println("===== Foods to delete =====")
                out.println()
            }
        }

        for (f in foodsToDelete) {
            ShowFood.printFoodSummary(f, out)
        }

        out.println()
        val plural = if (foodsToDelete.size != 1) "s" else ""
        out.print("Confirm delete " + foodsToDelete.size + " food" + plural + "? [y/N] ")
        val response = CliUtils.getChar(input, out)
        out.println()
        if (response == 'y' || response == 'Y') {
            out.println("Deleting foods...")
            out.println()
            try {
                ds.openConnection()
                ds.beginTransaction()
                for (f in foodsToDelete) {
                    // XXX will ON DELETE CASCADE just do what we want here?
                    Queries.deleteObject(ds, f)
                    out.println("Deleted " + f.indexName)
                }
                ds.endTransaction()
            } catch (e: SQLException) {
                err.println("SQL Exception occurred while deleting foods: $e")
                err.println("No foods deleted")
                return 1
            } finally {
                try {
                    ds.closeConnection()
                } catch (e: SQLException) {
                    err.println("Warning: SQL exception occurred when closing the DB")
                }

            }
        } else {
            out.println("No action performed")
        }
        return 0

    }
}
