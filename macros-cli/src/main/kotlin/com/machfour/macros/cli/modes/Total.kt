package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.MealSpec
import com.machfour.macros.cli.utils.printMeal
import com.machfour.macros.cli.utils.printMeals
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.queries.getMealsForDay
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException

/*
 * Prints out totals for all DB recorded meals in a day
 */
class Total(config: MacrosConfig) : com.machfour.macros.cli.CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "total"
        private const val USAGE = "Usage: $programName $NAME (<meal name>|--all) [<day>] [-v|--verbose] [--per100]"

        private fun makeMealSpec(args: List<String>, isAllMeals: Boolean): MealSpec {
            /* logic:
             * if isAllMeals is true:
             *     day is the first thing that doesn't start with -- or -
             * if isAllMeals is false:
             *     meal name is the first thing that doesn't start with -- or -
             *     day is the second such thing
             */
            val nonOptionArgs = ArrayList<String>(args.size - 1)
            for (arg in args) {
                // add everything after the first arg (which is the mode name) which doesn't start with a -
                if (!(arg == args[0] || arg.startsWith("-"))) {
                    nonOptionArgs.add(arg)
                }
            }
            val mealName = if (nonOptionArgs.size >= 1 && !isAllMeals) nonOptionArgs[0] else null
            val dayString = when {
                nonOptionArgs.size < 1 -> null
                isAllMeals -> nonOptionArgs[0] // just look for day
                nonOptionArgs.size >= 2 -> nonOptionArgs[1] // look for meal and day
                else -> null
            }

            return MealSpec.makeMealSpec(mealName, dayString)
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
                val mealsForDay = getMealsForDay(ds, mealSpec.day!!)
                if (mealsForDay.isEmpty()) {
                    println("No meals recorded on " + mealSpec.day.prettyPrint())
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
