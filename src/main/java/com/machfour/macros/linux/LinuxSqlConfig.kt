package com.machfour.macros.linux

import com.machfour.macros.sql.SqlConfig
import com.machfour.macros.util.FileUtils
import java.io.File

class LinuxSqlConfig: SqlConfig {
    companion object {
        private const val initSqlName = "schema.sql"
        private const val dataSqlName = "initial-data.sql"
        private val trigSqlNames = listOf(
            "food-quantity-triggers.sql",
            "nutrient-value-triggers.sql",
            "inbuilt-units-nutrients-triggers.sql",
            "timestamp-triggers.sql"
        )


        private val SQL_DIR = FileUtils.joinPath(LinuxConfig.PROJECT_DIR, "src/main/resources/sql")
    }

    override val initSqlName = Companion.initSqlName
    override val trigSqlNames = Companion.trigSqlNames
    override val dataSqlName = Companion.dataSqlName

    override val initSqlFile = File(FileUtils.joinPath(SQL_DIR, initSqlName))
    override val trigSqlFiles = trigSqlNames.map { File(FileUtils.joinPath(SQL_DIR, it)) }
    override val dataSqlFile = File(FileUtils.joinPath(SQL_DIR, dataSqlName))

}