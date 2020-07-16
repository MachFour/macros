package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.linux.LinuxDatabase
import java.io.IOException
import java.sql.SQLException

class Init : CommandImpl(NAME, USAGE) {
    companion object {
        private const val NAME = "init"
        private val USAGE = "Usage: $programName $NAME"
    }

    override fun printHelp() {
        out.println("Recreates and initialises the database. All previous data is deleted!")
    }

    override fun doActionNoExitCode(args: List<String>) {
        if (args.contains("--help")) {
            printHelp()
            return
        }
        val db = config.databaseInstance
        try {
            LinuxDatabase.deleteIfExists(config.dbLocation)
            out.println("Deleted database at ${config.dbLocation}")
        } catch (e: IOException) {
            out.println()
            out.println("Error deleting the database: " + e.message)
            return
        }
        try {
            db.initDb()
        } catch (e: SQLException) {
            out.println()
            out.println("Error initialising the database: " + e.message)
        } catch (e: IOException) {
            out.println()
            out.println("Error initialising the database: " + e.message)
        }
        out.println("Database re-initialised at ${config.dbLocation}")
    }
}