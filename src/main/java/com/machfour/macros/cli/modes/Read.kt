package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.ArgParsing
import com.machfour.macros.cli.utils.FileParser
import com.machfour.macros.cli.utils.printMeals
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.entities.Meal
import com.machfour.macros.insulin.InsulinCmdlineUtils
import java.io.FileReader
import java.io.IOException
import java.sql.SQLException


class Read(config: MacrosConfig) : CommandImpl(NAME, USAGE, config) {
    data class Flag(val name: String,
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
        private const val NAME = "read"
        private val helpFlag = Flag("help", "h")
        private val verboseFlag = Flag("verbose", "v")
        private val per100Flag = Flag("per100")
        private val insulinFlag = Flag("insulin")
        private val USAGE = "Usage: $programName $NAME <file> " +
            "[ ${verboseFlag.usage} ] [${per100Flag.usage}] [${insulinFlag.full} <ic ratio>[:<protein factor]]"
    }

    override fun doAction(args: List<String>): Int {
        if (args.size < 2) {
            printHelp()
            out.println()
            out.println("Please specify a file to read")
            return -1
        } else if (helpFlag.containedIn(args)) {
            printHelp()
            return -1
        }
        val filename = args[1]
        val verbose = verboseFlag.containedIn(args)
        val per100 = per100Flag.containedIn(args)

        val insulinArg = ArgParsing.findArgumentFromFlag(args, insulinFlag.full)
        val icRatio: Double?
        val proteinFactor: Double?

        when (insulinArg) {
            is ArgParsing.Result.KeyValFound -> {
                try {
                    InsulinCmdlineUtils.parseArgument(insulinArg.argument).let {
                        icRatio = it.first
                        proteinFactor = it.second
                    }
                } catch (e: NumberFormatException) {
                    err.println("I:C ratio and protein factor in ${insulinFlag.full} argument " +
                            "must be numeric and separated by a colon with no space")
                    return 1
                }
            }
            is ArgParsing.Result.ValNotFound -> {
                err.println("${insulinFlag.full} requires an argument")
                return 1
            }
            else -> {
                icRatio = null
                proteinFactor = null
            }
        }

        val ds = config.dataSource


        val fileParser = FileParser()
        val meals: List<Meal>
        try {
            FileReader(filename).use { meals = fileParser.parseFile(ds, it) }
        } catch (e1: IOException) {
            err.println("IO exception occurred: " + e1.message)
            return 1
        } catch (e2: SQLException) {
            err.println("SQL exception occurred: " + e2.message)
            return 1
        }

        out.printMeals(meals, verbose, per100, true)
        val errors = fileParser.getErrorLines()
        if (errors.isNotEmpty()) {
            out.println()
            out.println("Warning: the following lines were skipped")
            for ((key, value) in errors) {
                out.println("'$key' - $value")
            }
            return 2
        }

        if (icRatio != null) {
            InsulinCmdlineUtils.printInsulin(out, Meal.sumNutrientData(meals), icRatio, proteinFactor)
        }

        return 0
    }
}

