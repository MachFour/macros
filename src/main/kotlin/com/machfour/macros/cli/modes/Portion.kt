package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.FileParser.Companion.makefoodPortionSpecFromLine
import com.machfour.macros.cli.utils.FileParser.Companion.processFpSpec
import com.machfour.macros.cli.utils.MealSpec
import com.machfour.macros.cli.utils.MealSpec.Companion.makeMealSpec
import com.machfour.macros.cli.utils.printMeal
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.Meal
import com.machfour.macros.queries.getFoodByIndexName
import com.machfour.macros.queries.saveObject
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.util.FoodPortionSpec
import java.sql.SQLException

fun processPortions(toAddTo: Meal, specs: List<FoodPortionSpec>, db: SqlDatabase): Int {
    if (specs.isEmpty()) {
        println("No food portions specified, nothing to do")
        return 0
    }
    val spec = specs[0]
    val f: Food? = try {
        getFoodByIndexName(db, spec.foodIndexName)
    } catch (e: SQLException) {
        printlnErr("Could not retrieve food. Reason: " + e.message)
        return 1
    }
    if (f == null) {
        printlnErr("Unrecognised food with index name '${spec.foodIndexName}'")
        return 1
    }
    processFpSpec(spec, toAddTo, f)
    if (spec.error.isNotEmpty()) {
        printlnErr(spec.error)
        return 1
    }
    val createdObject = checkNotNull(spec.createdObject) {
        "No object created but no error message either"
    }
    toAddTo.addFoodPortion(createdObject)
    try {
        saveFoodPortions(db, toAddTo)
    } catch (e: SQLException) {
        printlnErr("Error saving food portion. Reason: " + e.message)
        return 1
    }
    println()
    printMeal(toAddTo, false)
    return 0
}

@Throws(SQLException::class)
private fun saveFoodPortions(ds: SqlDatabase, meal: Meal) {
    for (fp in meal.getFoodPortions()) {
        if (fp.source != ObjectSource.DATABASE) {
            saveObject(ds, fp)
        }
    }
}

class Portion(config: MacrosConfig): CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "portion"
        private const val USAGE = "Usage: $programName $NAME [ <meal name> [<day>] -s ] <portion spec> [<portion spec> ... ]"

    }

    override fun doAction(args: List<String>): Int {
        if (args.size == 1 || args.contains("--help")) {
            printHelp()
            return 0
        }
        val ds = config.database
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
                println("There can only be at most two arguments before '-s'")
                return 1
            }
            else -> {
                println("There can only be at most two arguments before '-s'")
                return 1
            }
        }
        if (!mealSpec.isMealSpecified) {
            val name = mealSpec.name
            val dayString = if (mealSpec.day != null) mealSpec.day.prettyPrint() else "(invalid day)"
            println("No meal specified, assuming $name on $dayString")
        }
        mealSpec.process(ds, true)
        if (mealSpec.error != null) {
            printlnErr(mealSpec.error)
            return 1
        }

        // now read everything after the '-s' as food portion specs
        val specs = ArrayList<FoodPortionSpec>(args.size - 1 - separator).apply {
            for (index in separator + 1 until args.size) {
                add(makefoodPortionSpecFromLine(args[index]))
            }
        }

        val processedObject = checkNotNull(mealSpec.processedObject) {
            "mealspec did not correctly process object but no error was given "
        }

        return processPortions(processedObject, specs, ds)
    }

}