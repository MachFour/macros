package com.machfour.macros.core

import com.machfour.macros.sql.SqlConfig
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlDatabaseImpl

interface MacrosConfig {
    val dbName: String
    val foodCsvPath: String
    val servingCsvPath: String
    val recipeCsvPath: String
    val ingredientsCsvPath: String
    var dbLocation: String
    val programName: String

    val sqlConfig: SqlConfig

    val database: SqlDatabase
    // for unusual low-level operations on the database
    val databaseImpl: SqlDatabaseImpl

}