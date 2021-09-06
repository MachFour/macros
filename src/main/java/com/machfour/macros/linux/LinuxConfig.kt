package com.machfour.macros.linux

import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.queries.MacrosDataSource
import com.machfour.macros.queries.StaticDataSource
import com.machfour.macros.sql.SqlConfig
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlDatabaseImpl

internal class LinuxConfig : MacrosConfig {
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

        private val LIB_DIR = com.machfour.macros.util.joinFilePath(PROJECT_DIR, "libs")

        private const val SQLITE_JDBC_VERSION = "3.34.0"
        const val SQLITE_NATIVE_LIB_NAME = "libsqlitejdbc-${SQLITE_JDBC_VERSION}.so"

        val SQLITE_NATIVE_LIB_DIR = LIB_DIR

        private val dbDefaultLocation = com.machfour.macros.util.joinFilePath(DB_DIR, DEFAULT_DB_NAME)

    }

    override val programName = Companion.programName
    override val dbName = DEFAULT_DB_NAME

    // allows setting up until the database is requested for the first time
    override var dbLocation = dbDefaultLocation

    private lateinit var databaseInstance: SqlDatabaseImpl
    private lateinit var dataSourceInstance: MacrosDataSource

    override val databaseImpl: SqlDatabaseImpl
        get() {
            if (!::databaseInstance.isInitialized) {
                databaseInstance = LinuxDatabase.getInstance(dbLocation)
            }
            return databaseInstance
        }

    override val database: SqlDatabase
        get() = databaseImpl

    override val dataSource: MacrosDataSource
        get() {
            if (!::dataSourceInstance.isInitialized) {
                dataSourceInstance = StaticDataSource(databaseImpl)
            }
            return dataSourceInstance
        }

    override val sqlConfig: SqlConfig = LinuxSqlConfig()

    override val foodCsvPath = com.machfour.macros.util.joinFilePath(DATA_DIR, FOOD_CSV_NAME)
    override val servingCsvPath = com.machfour.macros.util.joinFilePath(DATA_DIR, SERVING_CSV_NAME)
    override val recipeCsvPath = com.machfour.macros.util.joinFilePath(DATA_DIR, RECIPE_CSV_NAME)
    override val ingredientsCsvPath = com.machfour.macros.util.joinFilePath(DATA_DIR, INGREDIENTS_CSV_NAME)
    override val defaultCsvOutputDir = com.machfour.macros.util.joinFilePath(PROJECT_DIR, "csv-out")

}