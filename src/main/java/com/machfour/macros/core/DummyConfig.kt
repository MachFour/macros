package com.machfour.macros.core

import com.machfour.macros.storage.MacrosDataSource
import com.machfour.macros.storage.MacrosDatabase

import java.io.PrintStream
import java.io.OutputStream
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class DummyConfig : MacrosConfig {
    companion object {
        val nullOutputStream = object : OutputStream() {
            override fun write(i: Int) {}
        }
        val nullInputStream = object : InputStream() {
            override fun read(): Int = 0
        }
    }

    // null streams
    override val outStream = PrintStream(nullOutputStream)
    override val errStream = PrintStream(nullOutputStream)
    override val inputReader = BufferedReader(InputStreamReader(nullInputStream))

    override val sqlConfig: SqlConfig
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