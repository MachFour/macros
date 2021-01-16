package com.machfour.macros.core

import com.machfour.macros.persistence.MacrosDataSource
import com.machfour.macros.persistence.MacrosDatabase
import java.io.BufferedReader
import java.io.PrintStream

interface MacrosConfig {
    // All of these methods return not null
    val outStream: PrintStream
    val errStream: PrintStream
    val inputReader: BufferedReader

    val dbName: String
    val foodCsvPath: String
    val servingCsvPath: String
    val recipeCsvPath: String
    val ingredientsCsvPath: String
    val defaultCsvOutputDir: String
    var dbLocation: String
    val programName: String

    val sqlConfig: SqlConfig

    // for not-usual operations on the database
    val databaseInstance: MacrosDatabase
    val dataSourceInstance: MacrosDataSource

}