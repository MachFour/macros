package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.MealPrinter
import com.machfour.macros.cli.utils.MealSpec
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.queries.MealQueries
import com.machfour.macros.persistence.MacrosDatabase

import java.sql.SQLException


/*
 * Prints out totals for all DB recorded meals in a day
 */
class Total(config: MacrosConfig) : CommandImpl(NAME, USAGE, config) {
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
            var mealName: String? = null
            var dayString: String? = null
            if (nonOptionArgs.size >= 1) {
                if (isAllMeals) {
                    // just look for day
                    dayString = nonOptionArgs[0]
                } else {
                    // look for day and meal
                    mealName = nonOptionArgs[0]
                    if (nonOptionArgs.size >= 2) {
                        dayString = nonOptionArgs[1]
                    }
                }
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

    internal fun process(mealSpec: MealSpec, ds: MacrosDatabase, allMeals: Boolean, verbose: Boolean, per100: Boolean): Int {
        if (!allMeals) {
            // total for specific meal
            mealSpec.process(ds, false)
            if (mealSpec.error != null) {
                err.println(mealSpec.error)
                return 1
            }
            out.println()
            MealPrinter.printMeal(mealSpec.processedObject!!, verbose, out)

        } else {
            try {
                val mealsForDay = MealQueries.getMealsForDay(ds, mealSpec.day!!)
                if (mealsForDay.isEmpty()) {
                    out.println("No meals recorded on " + mealSpec.day.prettyPrint())
                } else {
                    MealPrinter.printMeals(mealsForDay.values, out, verbose, per100, true)
                }
            } catch (e: SQLException) {
                out.println()
                err.println("Error retrieving meals: " + e.message)
                return 1
            }

        }
        return 0
    }


}
