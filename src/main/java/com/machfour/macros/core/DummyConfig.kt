package com.machfour.macros.core

import com.machfour.macros.storage.MacrosDataSource
import com.machfour.macros.storage.MacrosDatabase

import java.io.PrintStream
import java.io.OutputStream
import java.io.BufferedReader
import java.io.InputStreamReader

class DummyConfig : MacrosConfig {

    override val outStream = PrintStream(OutputStream.nullOutputStream())
    override val errStream = PrintStream(OutputStream.nullOutputStream())
    override val inputReader = BufferedReader(InputStreamReader.nullReader())

    override val initSqlName: String
            get() = throw IllegalStateException("Dummy config")
    override val trigSqlName: String
            get() = throw IllegalStateException("Dummy config")
    override val dataSqlName: String
            get() = throw IllegalStateException("Dummy config")
    override val dbName: String
            get() = throw IllegalStateException("Dummy config")
    override val foodCsvPath: String
            get() = throw IllegalStateException("Dummy config")
    override val servingCsvPath: String
            get() = throw IllegalStateException("Dummy config")
    override val recipeCsvPath: String
            get() = throw IllegalStateException("Dummy config")
    override val ingredientsCsvPath: String
            get() = throw IllegalStateException("Dummy config")
    override val defaultCsvOutputDir: String
            get() = throw IllegalStateException("Dummy config")
    override var dbLocation: String = ""
            get() = throw IllegalStateException("Dummy config")
    override val programName: String
            get() = throw IllegalStateException("Dummy config")
    override val databaseInstance: MacrosDatabase
            get() = throw IllegalStateException("Dummy config")
    override val dataSourceInstance: MacrosDataSource
            get() = throw IllegalStateException("Dummy config")

}