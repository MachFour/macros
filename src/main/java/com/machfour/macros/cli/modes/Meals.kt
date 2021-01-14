package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.ArgParsing
import com.machfour.macros.cli.utils.ArgParsing.dayStringParse
import com.machfour.macros.cli.utils.ArgParsing.findArgument
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.queries.MealQueries.getMealsForDay
import com.machfour.macros.storage.MacrosDataSource
import com.machfour.macros.util.DateStamp
import java.sql.SQLException
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class Meals(config: MacrosConfig) : CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "meals"
        private val USAGE = "Usage: $programName $NAME [day]"
    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return -1
        }

        // cases: day not specified vs day specified
        val ds = config.dataSourceInstance
        val (_, argument, status) = findArgument(args, 1)
        val d : DateStamp = when (status) {
            ArgParsing.Status.NOT_FOUND -> {
                DateStamp.currentDate
            }
            ArgParsing.Status.OPT_ARG_MISSING -> {
                err.println("-d option requires a day specified")
                return 1
            }
            ArgParsing.Status.ARG_FOUND -> {
                dayStringParse(argument) ?: run {
                    err.println("Invalid date format: '$argument'.")
                    return 1
                }
            }
        }
        return printMealList(ds, d)
        //Meal toEdit = mealSpec.processedObject();
    }

    private fun printMealList(db: MacrosDataSource, d: DateStamp): Int {
        try {
            val meals = getMealsForDay(db, d)
            if (meals.isEmpty()) {
                out.println("No meals recorded on " + d.prettyPrint())
            } else {
                out.println("Meals recorded on " + d.prettyPrint() + ":")
                out.println()
                out.println(String.format("%-16s %-16s", "Name", "Last Modified"))
                out.println("=================================")
                val dateFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.LONG)
                for (m in meals.values) {
                    val mealDate = m.modifyInstant.atZone(ZoneId.systemDefault())
                    out.println(String.format("%-16s %16s", m.name, dateFormat.format(mealDate)))
                }
            }
        } catch (e: SQLException) {
            out.println("SQL Exception: " + e.message)
            return 1
        }
        return 0
    }

}