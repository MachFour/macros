package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.CliUtils
import com.machfour.macros.objects.Food
import com.machfour.macros.queries.FoodQueries
import com.machfour.macros.util.StringJoiner
import com.machfour.macros.util.UnicodeUtils
import java.io.PrintStream
import java.sql.SQLException

class SearchFood : CommandImpl(NAME, USAGE) {
    companion object {
        private const val NAME = "search"
        private val USAGE = "Usage: $programName $NAME <keyword>"

        fun printFoodList(foods: Collection<Food>, out: PrintStream) {
            // work out how wide the column should be
            val nameLength = foods.maxOf { UnicodeUtils.displayLength(it.mediumName) }
            val space = "        "
            val formatStr = "%-${nameLength}s${space}%s\n"
            // horizontal line - extra spaces are for whitespace + index name length
            val hline = StringJoiner.of("=").copies(nameLength + 8 + 14).join()

            out.apply {
                printf(formatStr, "Food name", "index name")
                println(hline)
                for (f in foods) {
                    print(UnicodeUtils.formatString(f.mediumName, nameLength, true))
                    print(space)
                    println(f.indexName)
                }
            }
        }
    }

    override fun doActionNoExitCode(args: List<String>) {
        if (args.size == 1 || args.contains("--help")) {
            printHelp()
            if (args.size == 1) {
                out.println("Please enter a search keyword for the food database")
            }
            return
        }
        val ds = config.dataSourceInstance
        val keyword = args[1]
        var resultFoods: Map<Long, Food> = emptyMap()
        try {
            val resultIds = FoodQueries.foodSearch(ds, keyword)
            if (resultIds.isNotEmpty()) {
                resultFoods = FoodQueries.getFoodsById(ds, resultIds)
            }
        } catch (e: SQLException) {
            out.print("SQL exception occurred: ")
            out.println(e.message)
            return
        }
        if (resultFoods.isEmpty()) {
            out.println("No matches for keyword '${keyword}'")
        } else {
            out.println("Search results:")
            out.println()
            printFoodList(resultFoods.values, out)
        }
    }

}