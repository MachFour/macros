package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.csv.createZipBackup
import com.machfour.macros.util.currentTimeString
import java.io.FileOutputStream
import java.io.IOException
import java.sql.SQLException
import java.util.zip.ZipException

class Export(config: MacrosConfig): CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "export"
        private const val USAGE = "Usage: $programName $NAME [output.zip]"
    }

    override fun printHelp() {
        println("Exports complete CSV data (foods and servings) from the database to a zip file.")
    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 0
        }
        val outputZipName = if (args.size >= 2) args[1] else "macros-csv-backup-${currentTimeString()}.zip"

        println("Exporting to $outputZipName")

        try {
            FileOutputStream(outputZipName).use {
                config.database.createZipBackup(it)
            }
        } catch (e: SQLException) {
            return handleException(e)
        } catch (e: IOException) {
            return handleException(e)
        } catch (e: ZipException) {
            return handleException(e)
        }

        println()
        println("Export completed successfully")
        return 0
    }

    private fun handleException(e: Exception): Int {
        println()
        printlnErr("Exception occurred (${e.javaClass}). Message: ${e.message}")
        return 1
    }

}