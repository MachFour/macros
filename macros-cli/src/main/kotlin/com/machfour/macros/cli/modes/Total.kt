package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CliConfig
import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.makeMealSpec
import com.machfour.macros.cli.utils.printMeal
import com.machfour.macros.cli.utils.printMeals
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.parsing.MealSpec
import com.machfour.macros.queries.getMealsForDay
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException

/*
 * Prints out totals for all DB recorded meals in a day
 */
class Total(config: CliConfig) : CommandImpl(config) {
    override val name = "total"
    override val usage = "Usage: ${config.programName} $name (<meal name>|--all) [<day>] [-v|--verbose] [--per100]"

    companion object {
        private fun makeMealSpec(args: List<String>, isAllMeals: Boolean): MealSpec {
            /* logic:
             * if isAllMeals is true:
             *     day is the first thing that doesn't start with -- or -
             * if isAllMeals is false:
             *     meal name is the first thing that doesn't start with -- or -
             *     day is the second such thing
             */
            val nonOptionArgs = args.filter { it != args.firstOrNull() && !it.startsWith("-") }
            val mealName = if (nonOptionArgs.isNotEmpty() && !isAllMeals) nonOptionArgs[0] else null
            val dayString = when {
                nonOptionArgs.isEmpty() -> null
                isAllMeals -> nonOptionArgs[0] // just look for day
                nonOptionArgs.size >= 2 -> nonOptionArgs[1] // look for meal and day
                else -> null
            }

            return makeMealSpec(mealName, dayString)
        }
    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 0
        }
        val verbose = args.contains("--verbose") || args.contains("-v")
        val per100 = args.contains("--per100")
        val allMeals = args.contains("--all")

        val ds = config.database
        val spec = makeMealSpec(args, allMeals)
        return process(spec, ds, allMeals, verbose, per100)

    }

    private fun process(mealSpec: MealSpec, ds: SqlDatabase, allMeals: Boolean, verbose: Boolean, per100: Boolean): Int {
        if (!allMeals) {
            // total for specific meal
            mealSpec.process(ds, false)
            if (mealSpec.error != null) {
                printlnErr(mealSpec.error)
                return 1
            }
            println()
            printMeal(mealSpec.processedObject!!, verbose)

        } else {
            try {
                val day = mealSpec.day!!
                val mealsForDay = getMealsForDay(ds, day)
                if (mealsForDay.isEmpty()) {
                    println("No meals recorded on " + day.prettyPrint())
                } else {
                    printMeals(mealsForDay.values, verbose, per100, true)
                }
            } catch (e: SqlException) {
                println()
                printlnErr("Error retrieving meals: " + e.message)
                return 1
            }

        }
        return 0
    }


}
