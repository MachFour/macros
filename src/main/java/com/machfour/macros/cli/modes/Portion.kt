package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.FileParser.Companion.makefoodPortionSpecFromLine
import com.machfour.macros.cli.utils.FileParser.Companion.processFpSpec
import com.machfour.macros.cli.utils.MealPrinter.printMeal
import com.machfour.macros.cli.utils.MealSpec
import com.machfour.macros.cli.utils.MealSpec.Companion.makeMealSpec
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.objects.Food
import com.machfour.macros.objects.Meal
import com.machfour.macros.queries.FoodQueries.getFoodByIndexName
import com.machfour.macros.queries.MealQueries.saveFoodPortions
import com.machfour.macros.persistence.MacrosDataSource
import com.machfour.macros.util.FoodPortionSpec
import java.io.PrintStream
import java.sql.SQLException

class Portion(config: MacrosConfig): CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "portion"
        private val USAGE = "Usage: $programName $NAME [ <meal name> [<day>] -s ] <portion spec> [<portion spec> ... ]"

        fun process(toAddTo: Meal, specs: List<FoodPortionSpec>, ds: MacrosDataSource, out: PrintStream, err: PrintStream): Int {
            if (specs.isEmpty()) {
                out.println("No food portions specified, nothing to do")
                return 0
            }
            val spec = specs[0]
            val f: Food? = try {
                getFoodByIndexName(ds, spec.foodIndexName)
            } catch (e: SQLException) {
                err.println("Could not retrieve food. Reason: " + e.message)
                return 1
            }
            if (f == null) {
                err.println("Unrecognised food with index name '${spec.foodIndexName}'")
                return 1
            }
            processFpSpec(spec, toAddTo, f)
            if (spec.error != null) {
                err.println(spec.error)
                return 1
            }
            assert(spec.createdObject != null) { "No object created but no error message either" }
            toAddTo.addFoodPortion(spec.createdObject!!)
            try {
                saveFoodPortions(ds, toAddTo)
            } catch (e: SQLException) {
                err.println("Error saving food portion. Reason: " + e.message)
                return 1
            }
            out.println()
            printMeal(toAddTo, false, out)
            return 0
        }
    }

    override fun doAction(args: List<String>): Int {
        if (args.size == 1 || args.contains("--help")) {
            printHelp()
            return 0
        }
        val ds = config.dataSourceInstance
        // MealSpec mealSpec = MealSpec.makeMealSpec(args);

        // TODO use argParsing here
        // check position of -s to decide how to parse arguments
        val mealSpec: MealSpec
        val separator = args.indexOf("-s")
        mealSpec = when (separator) {
            1, -1 -> {
                // no meal info specified
                makeMealSpec()
            }
            2 -> {
                // args looks like ["portion", "<meal name>", "-s", ...]
                makeMealSpec(args[1])
            }
            3 -> {
                // args looks like ["portion", "<meal name>", "<day>", "-s", ...]
                makeMealSpec(args[1], args[2])
            }
            0 -> {
                assert(false) { "'-s' is where the command name should be!" }
                out.println("There can only be at most two arguments before '-s'")
                return 1
            }
            else -> {
                out.println("There can only be at most two arguments before '-s'")
                return 1
            }
        }
        if (!mealSpec.isMealSpecified) {
            val name = mealSpec.name
            val dayString = if (mealSpec.day != null) mealSpec.day.prettyPrint() else "(invalid day)"
            out.println("No meal specified, assuming $name on $dayString")
        }
        mealSpec.process(ds, true)
        if (mealSpec.error != null) {
            err.println(mealSpec.error)
            return 1
        }

        // now read everything after the '-s' as food portion specs
        val specs: MutableList<FoodPortionSpec> = ArrayList(args.size - 1 - separator)
        for (index in separator + 1 until args.size) {
            specs.add(makefoodPortionSpecFromLine(args[index]))
        }
        assert (mealSpec.processedObject != null) { "mealspec did not correctly process object but no error was given "}
        return process(mealSpec.processedObject!!, specs, ds, out, err)
    }

}