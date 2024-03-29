package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CliConfig
import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.utils.printlnErr
import com.machfour.macros.linux.LinuxDatabase
import com.machfour.macros.sql.SqlException

class Init(config: CliConfig): CommandImpl(config) {
    override val name = "init"
    override val usage = noArgsUsage

    override fun printHelp() {
        println("Recreates and initialises the database. All previous data is deleted!")
    }

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 0
        }
        val db = config.databaseImpl
        try {
            LinuxDatabase.deleteIfExists(config.dbLocation)
            println("Deleted database at ${config.dbLocation}")
        } catch (e: SqlException) {
            return handleDeleteException(e)
        }
        try {
            db.initDb(config.sqlConfig)
        } catch (e: SqlException) {
            return handleInitException(e)
        }

        println("Database re-initialised at ${config.dbLocation}")
        return 0
    }

    private fun printExceptionMessage(e: Exception, message: String) {
        printlnErr(message + ": " + e.message)

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