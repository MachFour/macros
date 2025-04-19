package com.machfour.macros.csv

import com.machfour.ksv.CsvConfig
import com.machfour.ksv.CsvWriter
import com.machfour.macros.sql.entities.MacrosSqlEntity
import com.machfour.macros.queries.getAllRawObjects
import com.machfour.macros.schema.*
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.rowdata.RowData

// don't edit keyset!
private fun <M> prepareDataForBackup(data: RowData<M>): Map<String, String> {
    return buildMap {
        data.table.columns.forEach { col ->
            // null data gets mapped to empty string
            put(col.sqlName, data.getAsRawString(col))
        }
    }
}

internal fun getCsvWriter() = CsvWriter(CsvConfig.DEFAULT)

fun <M : MacrosSqlEntity<M>> writeObjectsToCsv(table: Table<M>, objects: Collection<M>): String {
    val header = table.columnsByName.keys.toList()

    val rows = buildList {
        add(header)
        objects.forEach {
            val dataStrings = prepareDataForBackup(it.data)
            add(header.map { column -> dataStrings[column] ?: "" })
        }
    }

    return getCsvWriter().write(rows)
}

// listed in dependency order (later tables have foreign keys referring to previous ones)
internal val csvZipBackupTables: List<Table<out MacrosSqlEntity<*>>> = listOf(
    UnitTable,
    NutrientTable,
    FoodTable,
    FoodNutrientValueTable,
    ServingTable,
    MealTable,
    FoodPortionTable,
    IngredientTable,
)

internal const val zipEntryComment = "MacrosDBBackup"

// returns app specific data directory
internal val <M> Table<M>.backupName: String
    get() = "$sqlName.csv"

@Throws(SqlException::class)
fun <M : MacrosSqlEntity<M>> SqlDatabase.exportTableToCsv(t: Table<M>): String {
    val rawObjectMap = getAllRawObjects(this, t)
    return writeObjectsToCsv(t, rawObjectMap.values)
}
