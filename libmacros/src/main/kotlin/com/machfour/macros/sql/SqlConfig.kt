package com.machfour.macros.sql

interface SqlConfig {

    val initSqlName: String
    val trigSqlNames: List<String>
    val dataSqlName: String

    val initSqlFilePath: String
    val trigSqlFilePaths: List<String>
    val dataSqlFilePath: String

}