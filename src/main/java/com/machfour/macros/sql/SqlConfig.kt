package com.machfour.macros.sql

import java.io.File

interface SqlConfig {

    val initSqlName: String
    val trigSqlNames: List<String>
    val dataSqlName: String

    val initSqlFile: File
    val trigSqlFiles: List<File>
    val dataSqlFile: File

}