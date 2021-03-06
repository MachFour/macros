package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.linux.LinuxDatabase
import java.io.IOException
import java.sql.SQLException

class Init(config: MacrosConfig) : CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "init"
        private val USAGE = "Usage: $programName $NAME"
    }

    override fun printHelp() {
        out.println("Recreates and initialises the database. All previous data is deleted!")
    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 0
        }
        val db = config.databaseInstance
        try {
            LinuxDatabase.deleteIfExists(config.dbLocation)
            out.println("Deleted database at ${config.dbLocation}")
        } catch (e: IOException) {
            return handleDeleteException(e)
        }
        try {
            db.initDb(config.sqlConfig)
        } catch (e: SQLException) {
            return handleInitException(e)
        } catch (e: IOException) {
            return handleInitException(e)
        }

        out.println("Database re-initialised at ${config.dbLocation}")
        return 0
    }

    private fun printExceptionMessage(e: Exception, message: String) {
        err.println(message + ": " + e.message)

    }
    private fun handleDeleteException(e: Exception): Int {
        printExceptionMessage(e, "Error deleting the database")
        return 1
    }
    private fun handleInitException(e: Exception): Int {
        printExceptionMessage(e, "Error initialising the database")
        return 1
    }

}