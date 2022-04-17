package com.machfour.macros.linux

import com.machfour.macros.sql.SqlConfig
import com.machfour.macros.util.joinFilePath

class LinuxSqlConfig: SqlConfig {
    companion object {
        private const val initSqlName = "schema.sql"
        private const val dataSqlName = "initial-data.sql"
        private val trigSqlNames = listOf(
            "food-quantity-triggers.sql",
            "nutrient-value-triggers.sql",
            "inbuilt-units-nutrients-triggers.sql",
        )

        private val SQL_DIR = joinFilePath(LinuxConfig.PROJECT_DIR, "src/main/resources/sql")
    }

    override val initSqlName = Companion.initSqlName
    override val trigSqlNames = Companion.trigSqlNames
    override val dataSqlName = Companion.dataSqlName

    override val initSqlFilePath = joinFilePath(SQL_DIR, initSqlName)
    override val trigSqlFilePaths = trigSqlNames.map { joinFilePath(SQL_DIR, it) }
    override val dataSqlFilePath = joinFilePath(SQL_DIR, dataSqlName)
}