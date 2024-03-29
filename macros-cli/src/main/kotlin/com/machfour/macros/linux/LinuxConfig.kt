package com.machfour.macros.linux

import com.machfour.macros.cli.CliConfig
import com.machfour.macros.jvm.joinFilePath
import com.machfour.macros.sql.SqlConfig
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlDatabaseImpl

internal class LinuxConfig : CliConfig {
    companion object {
        internal const val PROJECT_DIR = "/home/max/devel/macros"

        private const val programName = "macros"
        private const val programVersion = "2023.1"

        private const val DEFAULT_DB_NAME = "macros.sqlite"
        private const val DATA_DIR = "/home/max/devel/macros-data"
        private const val DB_DIR = PROJECT_DIR
        private const val FOOD_CSV_NAME = "foods.csv"
        private const val SERVING_CSV_NAME = "servings.csv"
        private const val RECIPE_CSV_NAME = "recipes.csv"
        private const val INGREDIENTS_CSV_NAME = "ingredients.csv"

        private val LIB_DIR = joinFilePath(PROJECT_DIR, "libs")

        private const val SQLITE_JDBC_VERSION = "3.44.0.0"
        const val SQLITE_NATIVE_LIB_NAME = "libsqlitejdbc-${SQLITE_JDBC_VERSION}.so"

        val SQLITE_NATIVE_LIB_DIR = LIB_DIR

        private val dbDefaultLocation = joinFilePath(DB_DIR, DEFAULT_DB_NAME)

    }

    override val programName = Companion.programName
    override val programVersion = Companion.programVersion

    override val dbName = DEFAULT_DB_NAME

    // allows setting up until the database is requested for the first time
    override var dbLocation = dbDefaultLocation

    private lateinit var databaseInstance: SqlDatabaseImpl

    override val databaseImpl: SqlDatabaseImpl
        get() {
            if (!::databaseInstance.isInitialized) {
                databaseInstance = LinuxDatabase.getInstance(dbLocation)
            }
            return databaseInstance
        }

    override val database: SqlDatabase
        get() = databaseImpl

    override val sqlConfig: SqlConfig = LinuxSqlConfig()

    override val foodCsvPath = joinFilePath(DATA_DIR, FOOD_CSV_NAME)
    override val servingCsvPath = joinFilePath(DATA_DIR, SERVING_CSV_NAME)
    override val recipeCsvPath = joinFilePath(DATA_DIR, RECIPE_CSV_NAME)
    override val ingredientsCsvPath = joinFilePath(DATA_DIR, INGREDIENTS_CSV_NAME)

}