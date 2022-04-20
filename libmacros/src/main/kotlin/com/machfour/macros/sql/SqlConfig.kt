package com.machfour.macros.sql

import java.io.File

interface SqlConfig {

    val initSqlName: String
    val trigSqlNames: List<String>
    val dataSqlName: String

    val initSqlFilePath: String
    val trigSqlFilePaths: List<String>
    val dataSqlFilePath: String

    val initSqlFile: File
        get() = File(initSqlFilePath)

    val trigSqlFiles: List<File>
        get() = trigSqlFilePaths.map { File(it) }

    val dataSqlFile: File
        get() = File(dataSqlFilePath)

}