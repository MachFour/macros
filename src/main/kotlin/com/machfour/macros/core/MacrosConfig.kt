package com.machfour.macros.core

import com.machfour.macros.sql.SqlConfig
import com.machfour.macros.sql.SqlDatabaseImpl

interface MacrosConfig {
    val dbName: String
    val foodCsvPath: String
    val servingCsvPath: String
    val recipeCsvPath: String
    val ingredientsCsvPath: String
    val defaultCsvOutputDir: String
    var dbLocation: String
    val programName: String

    val sqlConfig: SqlConfig

    val database: com.machfour.macros.sql.SqlDatabase
    // for not-usual operations on the database
    val databaseImpl: SqlDatabaseImpl
    val dataSource: com.machfour.macros.queries.MacrosDataSource

}