package com.machfour.macros.cli.modes

import com.machfour.datestamp.DateStamp
import com.machfour.datestamp.currentDateStamp
import com.machfour.macros.cli.CliConfig
import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.ArgParsingResult
import com.machfour.macros.cli.utils.dayStringParse
import com.machfour.macros.cli.utils.findArgument
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.jvm.modifyInstant
import com.machfour.macros.queries.getMealsForDay
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import com.machfour.macros.util.fmt
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class Meals(config: CliConfig) : CommandImpl(config) {
    override val name = "meals"
    override val usage = "Usage: ${config.programName} $name [day]"

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 2
        }

        // cases: day not specified vs day specified
        val ds = config.database
        val date = when (val dateArg = findArgument(args, 1)) {
            is ArgParsingResult.KeyValFound -> {
                dayStringParse(dateArg.argument) ?: run {
                    printlnErr("Invalid date format: '${dateArg.argument}'.")
                    return 1
                }
            }
            is ArgParsingResult.ValNotFound -> {
                printlnErr("-d option requires a day specified")
                return 1
            }
            else -> currentDateStamp()
        }
        return printMealList(ds, date)
        //Meal toEdit = mealSpec.processedObject();
    }

    private fun printMealList(db: SqlDatabase, d: DateStamp): Int {
        try {
            val meals = getMealsForDay(db, d)
            if (meals.isEmpty()) {
                println("No meals recorded on " + d.prettyPrint())
            } else {
                println("Meals recorded on " + d.prettyPrint() + ":")
                println()
                println("Name".fmt(16, true) + " " + "Last Modified".fmt(16, true))
                println("=================================")
                val dateFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.LONG)
                for (m in meals.values) {
                    val mealDate = m.modifyInstant.atZone(ZoneId.systemDefault())
                    println(m.name.fmt(16, true) + " " + dateFormat.format(mealDate).fmt(16))
                }
            }
        } catch (e: SqlException) {
            printlnErr("SQL Exception: ${e.message}")
            return 1
        }
        return 0
    }

}