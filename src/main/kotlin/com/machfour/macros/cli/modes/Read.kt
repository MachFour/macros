package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.FileParser
import com.machfour.macros.cli.utils.MealPrinter
import com.machfour.macros.objects.Meal

import java.io.FileReader
import java.io.IOException
import java.sql.SQLException


class Read : CommandImpl(NAME, USAGE) {
    companion object {
        private const val NAME = "read"
        private val USAGE = "Usage: $programName $NAME <file> [-v | --verbose] [--per100]"
    }

    override fun doAction(args: List<String>): Int {
        if (args.size < 2) {
            printHelp()
            out.println()
            out.println("Please specify a file to read")
            return -1
        } else if (args.contains("--help")) {
            printHelp()
            return -1
        }
        val filename = args[1]
        val verbose = args.contains("--verbose") || args.contains("-v")
        val per100 = args.contains("--per100")

        val ds = config.dataSourceInstance


        val fileParser = FileParser()
        var meals: List<Meal>
        try {
            FileReader(filename).use { meals = fileParser.parseFile(ds, it) }
        } catch (e1: IOException) {
            err.println("IO exception occurred: " + e1.message)
            return 1
        } catch (e2: SQLException) {
            err.println("SQL exception occurred: " + e2.message)
            return 1
        }

        MealPrinter.printMeals(meals, out, verbose, per100, true)
        val errors = fileParser.getErrorLines()
        if (errors.isNotEmpty()) {
            out.println()
            out.println("Warning: the following lines were skipped")
            for ((key, value) in errors) {
                out.println("'$key' - $value")
            }
            return 2
        }
        return 0
    }
}

