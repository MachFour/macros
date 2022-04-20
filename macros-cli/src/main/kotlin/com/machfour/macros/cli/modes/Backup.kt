package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CliConfig
import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.csv.createZipBackup
import com.machfour.macros.jvm.currentTimeString
import com.machfour.macros.sql.SqlException
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipException

class Backup(config: CliConfig): CommandImpl(config) {
    override val name: String = "backup"
    override val usage: String = "Usage: ${config.programName} $name [output.zip]"

    override fun printHelp() {
        println("Backs up the database into CSV files which can be used to create a new copy of the database.")
    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 0
        }
        val outputZipName = if (args.size >= 2) args[1] else "macros-csv-backup-${currentTimeString()}.zip"

        println("Backing up to $outputZipName")

        try {

            FileOutputStream(outputZipName).use {
                config.database.createZipBackup(it)
            }
        } catch (e: SqlException) {
            return handleException(e)
        } catch (e: IOException) {
            return handleException(e)
        } catch (e: ZipException) {
            return handleException(e)
        }

        println()
        println("Backup completed successfully")
        return 0
    }

    private fun handleException(e: Exception): Int {
        println()
        printlnErr("Exception occurred (${e.javaClass}). Message: ${e.message}")
        return 1
    }

}