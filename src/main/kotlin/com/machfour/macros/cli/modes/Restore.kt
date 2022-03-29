package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.csv.restoreFromZip
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.datatype.TypeCastException
import java.io.FileInputStream
import java.io.IOException

class Restore(config: MacrosConfig): CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "restore"
        private const val USAGE = "Usage: $programName $NAME [--clear] <backup.zip>"

    }

    override fun printHelp() {
        println("Restores the database using a CSV zip file saved using the 'export' command.")
        println("Pass --clear to clear existing data first. (If not done, the operation may fail due to ID conflicts)")
    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 0
        }
        if (args.size < 2) {
            printHelp()
            return -1
        }

        // default output dir
        val csvZipPath = args[1]

        try {
            FileInputStream(csvZipPath).use {
                config.database.restoreFromZip(it)
            }
        } catch (e: SqlException) {
            return handleException(e)
        } catch (e: IOException) {
            return handleException(e)
        } catch (e: TypeCastException) {
            return handleException(e)
        }
        println()
        println("Database successfully restored from CSV data in $csvZipPath")
        return 0
    }

    private fun handleException(e: Exception) : Int {
        println()
        printlnErr("Exception occurred (${e.javaClass}). Message: ${e.message}")
        return 1
    }
}