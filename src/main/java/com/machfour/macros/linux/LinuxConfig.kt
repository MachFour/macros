package com.machfour.macros.linux

import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.core.SqlConfig
import com.machfour.macros.storage.MacrosDataSource
import com.machfour.macros.storage.MacrosDatabase
import com.machfour.macros.util.FileUtils.joinPath
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.PrintStream

class LinuxConfig : MacrosConfig {
    companion object {
        internal const val PROJECT_DIR = "/home/max/devel/macros"

        private const val programName = "macros"
        private const val dbName = "macros.sqlite"
        private const val DATA_DIR = "/home/max/devel/macros-data"
        private const val DB_DIR = PROJECT_DIR
        private const val FOOD_CSV_NAME = "foods.csv"
        private const val SERVING_CSV_NAME = "servings.csv"
        private const val RECIPE_CSV_NAME = "recipes.csv"
        private const val INGREDIENTS_CSV_NAME = "ingredients.csv"

        private val LIB_DIR = joinPath(PROJECT_DIR, "libs")

        val SQLITE_NATIVE_LIB_NAME = "libsqlitejdbc-3.30.1.so"

        val SQLITE_NATIVE_LIB_DIR = LIB_DIR

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

    override val sqlConfig: SqlConfig = LinuxSqlConfig()

    override val foodCsvPath = joinPath(DATA_DIR, FOOD_CSV_NAME)
    override val servingCsvPath = joinPath(DATA_DIR, SERVING_CSV_NAME)
    override val recipeCsvPath = joinPath(DATA_DIR, RECIPE_CSV_NAME)
    override val ingredientsCsvPath = joinPath(DATA_DIR, INGREDIENTS_CSV_NAME)
    override val defaultCsvOutputDir = joinPath(PROJECT_DIR, "csv-out")

}