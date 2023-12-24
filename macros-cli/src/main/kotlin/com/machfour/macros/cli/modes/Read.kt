package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CliConfig
import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.ArgParsingResult
import com.machfour.macros.cli.utils.findArgumentFromFlag
import com.machfour.macros.cli.utils.printMeals
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.entities.Meal
import com.machfour.macros.insulin.BolusCalculator
import com.machfour.macros.insulin.parseInsulinArgument
import com.machfour.macros.insulin.printInsulin
import com.machfour.macros.parsing.FileParser
import com.machfour.macros.sql.SqlException
import java.io.FileReader
import java.io.IOException


class Read(config: CliConfig) : CommandImpl(config) {
    override val name = "read"
    override val usage = "Usage: ${config.programName} $name <file> " +
            "[ ${verboseFlag.usage} ] [${per100Flag.usage}] [${insulinFlag.full} <ic ratio>[:<protein factor]]"

    data class Flag(
        val name: String,
        val mnemonic: String? = null,
        val hasArg: Boolean = false,
        val argOptional: Boolean = false
    ) {
        val full: String
            get() = "--$name"
        val abbr: String?
            get() = mnemonic?.let { "-$it" }
        val usage: String
            get() = (if (mnemonic != null) "$abbr | " else "") + full

        fun containedIn(cmdline: List<String>) : Boolean {
            return cmdline.contains(full) || cmdline.contains(abbr)
        }
    }

    companion object {
        private val helpFlag = Flag("help", "h")
        private val verboseFlag = Flag("verbose", "v")
        private val per100Flag = Flag("per100")
        private val insulinFlag = Flag("insulin")
    }

    override fun doAction(args: List<String>): Int {
        if (args.size < 2) {
            printHelp()
            println()
            println("Please specify a file to read")
            return 2
        } else if (helpFlag.containedIn(args)) {
            printHelp()
            return 2
        }
        val filename = args[1]
        val verbose = verboseFlag.containedIn(args)
        val per100 = per100Flag.containedIn(args)

        val insulinArg = findArgumentFromFlag(args, insulinFlag.full)
        val insulinCalc: BolusCalculator?

        when (insulinArg) {
            is ArgParsingResult.KeyValFound -> {
                insulinCalc = parseInsulinArgument(insulinArg.argument)
                if (insulinCalc == null) {
                    printlnErr(
                        "I:C ratio and fat/protein factors (if used) in ${insulinFlag.full} " +
                                " argument must be numeric, positive and separated by ':' characters"
                    )
                    return 1
                }
            }
            is ArgParsingResult.ValNotFound -> {
                printlnErr("${insulinFlag.full} requires an argument")
                return 1
            }
            else -> {
                insulinCalc = null
            }
        }

        val fileParser = FileParser()
        val meals: List<Meal>
        try {
            FileReader(filename).use { meals = fileParser.parseFile(config.database, it.readLines()) }
        } catch (e1: IOException) {
            printlnErr("IO exception occurred: " + e1.message)
            return 1
        } catch (e2: SqlException) {
            printlnErr("SQL exception occurred: " + e2.message)
            return 1
        }

        printMeals(meals, verbose, per100, true)
        val errors = fileParser.getErrorLines()
        if (errors.isNotEmpty()) {
            println()
            println("Warning: the following lines were skipped")
            for ((key, value) in errors) {
                println("'$key' - $value")
            }
            return 2
        }

        if (insulinCalc != null) {
            printInsulin(Meal.sumNutrientData(meals), insulinCalc)
        }

        return 0
    }
}

