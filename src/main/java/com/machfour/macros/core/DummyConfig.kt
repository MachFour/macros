package com.machfour.macros.core

import com.machfour.macros.storage.MacrosDataSource
import com.machfour.macros.storage.MacrosDatabase
import java.io.BufferedReader
import java.io.PrintStream

class DummyConfig : MacrosConfig {

    override val outStream: PrintStream = throw IllegalStateException("Dummy config")
    override val errStream: PrintStream = throw IllegalStateException("Dummy config")
    override val inputReader: BufferedReader = throw IllegalStateException("Dummy config")
    override val initSqlName: String = throw IllegalStateException("Dummy config")
    override val trigSqlName: String = throw IllegalStateException("Dummy config")
    override val dataSqlName: String = throw IllegalStateException("Dummy config")
    override val dbName: String = throw IllegalStateException("Dummy config")
    override val foodCsvPath: String = throw IllegalStateException("Dummy config")
    override val servingCsvPath: String = throw IllegalStateException("Dummy config")
    override val recipeCsvPath: String = throw IllegalStateException("Dummy config")
    override val ingredientsCsvPath: String = throw IllegalStateException("Dummy config")
    override val defaultCsvOutputDir: String = throw IllegalStateException("Dummy config")
    override var dbLocation: String = throw IllegalStateException("Dummy config")
    override val programName: String = throw IllegalStateException("Dummy config")
    override val databaseInstance: MacrosDatabase = throw IllegalStateException("Dummy config")
    override val dataSourceInstance: MacrosDataSource = throw IllegalStateException("Dummy config")

}