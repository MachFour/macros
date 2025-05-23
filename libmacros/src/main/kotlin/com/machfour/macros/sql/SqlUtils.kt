package com.machfour.macros.sql

import com.machfour.macros.core.EntityId
import com.machfour.macros.sql.datatype.SqlType
import com.machfour.macros.sql.datatype.Types
import com.machfour.macros.sql.entities.MacrosSqlEntity
import com.machfour.macros.sql.generator.ColumnExpr
import com.machfour.macros.sql.generator.Conjunction

// usually, an SQL placeholder is just a question mark. But, for example a DateStamp
// needs to be entered as DATE('date_string'), so that logic is implemented here
private fun SqlType<*>.getSqlPlaceholder(): String {
    // for now, DateStamp is the only special one
    return when (this) {
        // need the space here in order for the JDBC binding code to work properly
        Types.DATESTAMP -> "DATE( ? )"
        else -> "?"
    }
}

// creates SQL placeholders for the given (ordered) list of columns
private fun <M> makeInsertPlaceholders(columns: List<Column<M, *>>): String {
    return columns.joinToString(separator = ", ") { it.type.getSqlPlaceholder() }
}

// creates SQL placeholders for the given (ordered) list of columns
internal fun <M> makeSqlUpdatePlaceholders(columns: List<Column<M, *>>): String {
    return columns.joinToString(separator = ", ") { it.sqlName + " = " + it.type.getSqlPlaceholder() }
}

// " WHERE column1 IS NULL"
// " WHERE column1 IS NOT NULL"
internal fun makeSqlWhereNullString(whereColumnExpr: ColumnExpr<*, *>, negate: Boolean = false): String {
    val colName = whereColumnExpr.sql
    val not = if (negate) "NOT" else ""
    return " WHERE $colName IS $not NULL"
}

// " WHERE column1 = ?"
// " WHERE column1 IN (?, ?, ?, ...?)"
// if nkeys is 0, no where string will be formed, so all objects will be returned.
// exception thrown if nValues < 0
internal fun makeSqlWhereString(whereColumnExpr: ColumnExpr<*, *>, nValues: Int): String {
    val colName = whereColumnExpr.sql
    require(nValues >= 0) { "nValues cannot negative" }

    if (nValues == 0) {
        return ""
    }

    val placeholder = whereColumnExpr.type.getSqlPlaceholder()

    //if (whereColumn.type().equals(DATESTAMP)) {
    //    sb.append("DATE(").append(whereColumn.sqlName()).append(")");
    //} else {
    //sb.append(whereColumn.sqlName());
    //}
    return if (nValues == 1) {
        " WHERE $colName = $placeholder"
    } else {
        " WHERE $colName IN (" + "$placeholder, ".repeat(nValues - 1) + placeholder + ")"
    }
}

// " WHERE ((likeColumn[0] LIKE likeValue[0]) OR (likeColumn[1] LIKE likeValue[1]) OR ...)"
internal fun <M> makeSqlWhereLikeString(
    likeCols: Collection<Column<M, String>>,
    conjunction: Conjunction = Conjunction.OR,
): String {
    return when (likeCols.size) {
        0 -> ""
        1 -> "WHERE (" + likeCols.single().sqlName + " LIKE ?)"
        else -> {
            val conjunctionSeparator = " ${conjunction.sql} "
            "WHERE (" + likeCols.joinToString(conjunctionSeparator) { "(${it.sqlName} LIKE ?)" } + ")"
        }
    }
}

// columns must be a subset of table.columns()
fun <M> sqlInsertTemplate(t: Table<*, M>, orderedColumns: List<Column<M, *>>): String {
    val placeholders = makeInsertPlaceholders(orderedColumns)
    val columnSql = orderedColumns.joinToString(separator = ", ") { it.sqlName }
    return "INSERT INTO ${t.sqlName} ($columnSql) VALUES ($placeholders)"
}

fun <M, J: Any> sqlUpdateTemplate(t: Table<*, M>, orderedColumns: List<Column<M, *>>, keyCol: Column<M, J>): String {
    val columnPlaceholders = makeSqlUpdatePlaceholders(orderedColumns)
    val whereString = makeSqlWhereString(keyCol, 1)
    // TODO dynamic placeholders
    return "UPDATE ${t.sqlName} SET $columnPlaceholders $whereString"
}

fun <M : MacrosSqlEntity<M>> makeIdMap(objects: Collection<M>): Map<EntityId, M> {
    return buildMap {
        objects.forEach {
            require(!containsKey(it.id)) { "Objects have shared id (${it.id}): $it, ${this[it.id]}" }
            this[it.id] = it
        }
    }
}

private val newline = Regex("\n")

// Returns a list of strings such that each one is a complete SQL statement in the SQL
// file represented by reader r.
// IMPORTANT DETAIL:
// It's not enough to do this just by splitting on semicolons, since there are things
// like nested statements with 'END;' and strings may contain semicolons.
// So the split token is the semicolon followed by two blank lines, i.e. statements
// in the SQL file must be terminated by a semicolon immediately followed by \n\n
// XXX Possible bug if the newline character is different on different platforms.
fun createSplitSqlStatements(lines: Sequence<String>): List<String> {
    return createSqlStatements(lines = lines, lineSep = "\n")
        .split(";\n\n")
        // a double newline after the last statement will cause an empty final element
        // in the split list, which turns into a null statement ';' after the map.
        // The Android SQL library seems to spew at this (throws SQL exception despite the
        // SQL code being 0 (NOT_AN_ERROR)).
        // Anyway let's just filter everything to ensure we don't end up with any null statements
        .filter { it.isNotBlank() }
        // condense remaining whitespace and add the semicolon back
        .map { it.replace(newlineAndSpace, " ").trim() + ";" }
}


private fun String.trimComment(commentMarker: String): String {
    return when (val idx = indexOf(commentMarker)) {
        -1 -> this
        0 -> ""
        else -> substring(0, idx)
    }
}

val newlineAndSpace = Regex("[\n\\s]+")
private val multiSpace = Regex("\\s+")

fun createSqlStatements(
    lines: Sequence<String>,
    lineSep: String = " ",
    commentMarker: String = "--"
): String {
    // 1. if a line is entirely a comment, ignore it
    // 2. else line contains SQL and possibly a comment. strip the comment
    // 3. if what's left is entirely space, ignore it
    // 4. otherwise, add it to the list.
    return lines.mapNotNull { line ->
        if (line.isEmpty()) {
            ""
        } else {
            line.trimComment(commentMarker).replace(multiSpace, " ").takeIf { it.isNotBlank() }
        }
    }.joinToString(separator = lineSep)
}

// Returns triggers that update the create and modify times of the given table.
// Columns are determined using the Schema object so ensure it is updated.
fun <M> createInitTimestampTriggers(t: Table<*, M>): List<String> {
    val table = t.sqlName
    val createTime = t.createTimeColumn.sqlName
    val modifyTime = t.modifyTimeColumn.sqlName
    val id = t.idColumn.sqlName
    val currentUnixTime = "CAST(strftime('%s', 'now') AS INTEGER)"
    val timestampCols = setOf(t.createTimeColumn, t.modifyTimeColumn)
    val columnNames = t.columns.filterNot { it in timestampCols }.map { it.sqlName }

    val initTimeStampTrigger = """
            |CREATE TRIGGER init_${table.lowercase()}_timestamp
            |AFTER INSERT ON $table
            |WHEN (NEW.${createTime} = 0)
            |BEGIN UPDATE $table
            |SET $createTime = $currentUnixTime, $modifyTime = $currentUnixTime 
            |WHERE $id = NEW.${id};
            |END;
        """.trimMargin("|")

    val updateTimestampTrigger = """
            |CREATE TRIGGER update_${table.lowercase()}_timestamp
            |AFTER UPDATE OF ${columnNames.joinToString(separator = ", ")} ON $table
            |BEGIN UPDATE $table
            |SET $modifyTime = $currentUnixTime
            |WHERE $id = NEW.${id};
            |END;
        """.trimMargin("|")
    return listOf(initTimeStampTrigger, updateTimestampTrigger)
}