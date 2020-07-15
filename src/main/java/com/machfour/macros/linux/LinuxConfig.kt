package com.machfour.macros.linux

import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.storage.MacrosDataSource
import com.machfour.macros.storage.MacrosDatabase
import com.machfour.macros.util.FileUtils.joinPath
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.PrintStream

class LinuxConfig : MacrosConfig {
    companion object {
        private const val PROJECT_DIR = "/home/max/devel/macros-java"

        private const val programName = "macros"
        private const val dbName = "macros.sqlite"

        private const val DATA_DIR = "/home/max/devel/macros/macros-data"
        private const val DB_DIR = PROJECT_DIR
        private const val FOOD_CSV_NAME = "foods.csv"
        private const val SERVING_CSV_NAME = "servings.csv"
        private const val RECIPE_CSV_NAME = "recipes.csv"
        private const val INGREDIENTS_CSV_NAME = "ingredients.csv"

        private const val initSqlName = "macros-db-create.sql"
        private const val trigSqlName = "macros-db-triggers.sql"
        private const val dataSqlName = "macros-initial-data.sql"


        private val LIB_DIR = joinPath(PROJECT_DIR, "lib")
        private val SQL_DIR = joinPath(PROJECT_DIR, "src/sql")

        @JvmField
        val SQLITE_NATIVE_LIB_NAME = "libsqlitejdbc-3.30.1.so"

        @JvmField
        val SQLITE_NATIVE_LIB_DIR = LIB_DIR

        @JvmField
        val INIT_SQL = File(joinPath(SQL_DIR, initSqlName))
        @JvmField
        val TRIG_SQL = File(joinPath(SQL_DIR, trigSqlName))
        @JvmField
        val DATA_SQL = File(joinPath(SQL_DIR, dataSqlName))

        private val inputReader = BufferedReader(InputStreamReader(System.`in`))

    }

    override val outStream: PrintStream = System.out
    override val errStream: PrintStream = System.err
    override val inputReader: BufferedReader = Companion.inputReader

    override val programName = Companion.programName
    override val dbName = Companion.dbName
    override var dbLocation = joinPath(DB_DIR, dbName)

    override val databaseInstance: MacrosDatabase = LinuxDatabase.getInstance(dbLocation)
    override val dataSourceInstance: MacrosDataSource = databaseInstance

    override val initSqlName = Companion.initSqlName
    override val trigSqlName = Companion.trigSqlName
    override val dataSqlName = Companion.dataSqlName


    override val foodCsvPath = joinPath(DATA_DIR, FOOD_CSV_NAME)
    override val servingCsvPath = joinPath(DATA_DIR, SERVING_CSV_NAME)
    override val recipeCsvPath = joinPath(DATA_DIR, RECIPE_CSV_NAME)
    override val ingredientsCsvPath = joinPath(DATA_DIR, INGREDIENTS_CSV_NAME)
    override val defaultCsvOutputDir = joinPath(PROJECT_DIR, "csv-out")

}