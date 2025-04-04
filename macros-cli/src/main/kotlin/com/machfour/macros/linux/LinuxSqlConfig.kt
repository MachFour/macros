package com.machfour.macros.linux

import com.machfour.macros.jvm.joinFilePath
import com.machfour.macros.sql.SqlConfig

private const val INIT_SQL_NAME = "schema.sql"
private const val DATA_SQL_NAME = "initial-data.sql"
private val TRIG_SQL_NAMES = listOf(
    "food-quantity-triggers.sql",
    "nutrient-value-triggers.sql",
    "inbuilt-units-nutrients-triggers.sql",
)

private val SQL_DIR = joinFilePath(PROJECT_DIR, "libmacros/src/main/resources/sql")

class LinuxSqlConfig: SqlConfig {
    override val initSqlName = INIT_SQL_NAME
    override val trigSqlNames = TRIG_SQL_NAMES
    override val dataSqlName = DATA_SQL_NAME

    override val initSqlFilePath = joinFilePath(SQL_DIR, initSqlName)
    override val trigSqlFilePaths = trigSqlNames.map { joinFilePath(SQL_DIR, it) }
    override val dataSqlFilePath = joinFilePath(SQL_DIR, dataSqlName)
}