package com.machfour.macros.linux

import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.sql.SqlConfig
import com.machfour.macros.persistence.MacrosDatabase
import com.machfour.macros.persistence.MacrosDatabaseImpl
import com.machfour.macros.queries.MacrosDataSource
import com.machfour.macros.queries.StaticDataSource
import com.machfour.macros.util.FileUtils.joinPath
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream

class LinuxConfig : MacrosConfig {
    companion object {
        internal const val PROJECT_DIR = "/home/max/devel/macros"

        private const val programName = "macros"
        private const val DEFAULT_DB_NAME = "macros.sqlite"
        private const val DATA_DIR = "/home/max/devel/macros-data"
        private const val DB_DIR = PROJECT_DIR
        private const val FOOD_CSV_NAME = "foods.csv"
        private const val SERVING_CSV_NAME = "servings.csv"
        private const val RECIPE_CSV_NAME = "recipes.csv"
        private const val INGREDIENTS_CSV_NAME = "ingredients.csv"

        private val LIB_DIR = joinPath(PROJECT_DIR, "libs")

        private const val SQLITE_JDBC_VERSION = "3.34.0"
        const val SQLITE_NATIVE_LIB_NAME = "libsqlitejdbc-${SQLITE_JDBC_VERSION}.so"

        val SQLITE_NATIVE_LIB_DIR = LIB_DIR

        private val inputReader = BufferedReader(InputStreamReader(System.`in`))

        private val dbDefaultLocation = joinPath(DB_DIR, DEFAULT_DB_NAME)

    }

    override val outStream: PrintStream = System.out
    override val errStream: PrintStream = System.err
    override val inputReader: BufferedReader = Companion.inputReader

    override val programName = Companion.programName
    override val dbName = DEFAULT_DB_NAME

    // allows setting up until the database is requested for the first time
    override var dbLocation = dbDefaultLocation

    private lateinit var databaseInstance: MacrosDatabaseImpl
    private lateinit var dataSourceInstance: MacrosDataSource

    override val databaseImpl: MacrosDatabaseImpl
        get() {
            if (!::databaseInstance.isInitialized) {
                databaseInstance = LinuxDatabase.getInstance(dbLocation)
            }
            return databaseInstance
        }

    override val database: MacrosDatabase
        get() = databaseImpl

    override val dataSource: MacrosDataSource
        get() {
            if (!::dataSourceInstance.isInitialized) {
                dataSourceInstance = StaticDataSource(databaseImpl)
            }
            return dataSourceInstance
        }

    override val sqlConfig: SqlConfig = LinuxSqlConfig()

    override val foodCsvPath = joinPath(DATA_DIR, FOOD_CSV_NAME)
    override val servingCsvPath = joinPath(DATA_DIR, SERVING_CSV_NAME)
    override val recipeCsvPath = joinPath(DATA_DIR, RECIPE_CSV_NAME)
    override val ingredientsCsvPath = joinPath(DATA_DIR, INGREDIENTS_CSV_NAME)
    override val defaultCsvOutputDir = joinPath(PROJECT_DIR, "csv-out")

}