package com.machfour.macros.core

import com.machfour.macros.persistence.MacrosDatabase
import com.machfour.macros.persistence.MacrosDatabaseImpl
import com.machfour.macros.queries.MacrosDataSource
import com.machfour.macros.sql.SqlConfig
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

    val databaseInstance: MacrosDatabase
    // for not-usual operations on the database
    val databaseImplInstance: MacrosDatabaseImpl
    val dataSource: MacrosDataSource

}