package com.machfour.macros.sql

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.sql.datatype.MacrosType
import com.machfour.macros.sql.datatype.Types
import com.machfour.macros.sql.generator.ColumnExpr
import com.machfour.macros.sql.generator.Conjuction
import com.machfour.macros.util.stringJoin
import java.io.BufferedReader
import java.io.IOException
import java.io.Reader
import java.sql.SQLException

object SqlUtils {
    internal fun <M> joinColumns(columns: Iterable<Column<M, *>>, suffix: String = ""): String {
        return stringJoin(columns, sep = ", ", itemSuffix = suffix) { it.sqlName }
    }

    // usually, an SQL placeholder is just a question mark. But, for example a DateStamp
    // needs to be entered as DATE('date_string'), so that logic is implemented here
    private fun getSqlPlaceholder(columnType: MacrosType<*>): String {
        // for now, DateStamp is the only special one
        return when (columnType) {
            // need the space here in order for the JDBC binding code to work properly
            Types.DATESTAMP -> "DATE( ? )"
            else -> "?"
        }
    }

    private fun <M> makePlaceholders(columns: List<Column<M, *>>, placeholderFormat: (c: Column<M, *>) -> String): String {
        return stringJoin(columns, sep = ", ") { placeholderFormat(it) }
    }

    // creates SQL placeholders for the given (ordered) list of columns
    private fun <M> makeInsertPlaceholders(columns: List<Column<M, *>>): String {
        return makePlaceholders(columns) { getSqlPlaceholder(it.type) }
    }

    // creates SQL placeholders for the given (ordered) list of columns
    internal fun <M> makeUpdatePlaceholders(columns: List<Column<M, *>>): String {
        return makePlaceholders(columns) { it.sqlName + " = " + getSqlPlaceholder(it.type) }
    }

    // " WHERE column1 = ?"
    // " WHERE column1 IN (?, ?, ?, ...?)"
    // " WHERE column1 IS [NOT] NULL
    // if nkeys is 0, no where string will be formed, so all objects will be returned.
    // exception thrown if nValues < 0
    internal fun makeWhereString(whereColumnExpr: ColumnExpr<*, *>, nValues: Int? = null, isNotNull: Boolean? = null): String {
        val colName = whereColumnExpr.sql
        require((nValues != null) xor (isNotNull != null)) { "Must specify one of nValues or isNull" }

        if (isNotNull != null) {
            val not = if (isNotNull) "NOT" else ""
            return " WHERE $colName IS $not NULL"
        }

        check(nValues != null && nValues >= 0)
        if (nValues == 0) {
            return ""
        }

        val placeholder = getSqlPlaceholder(whereColumnExpr.type)

        //if (whereColumn.type().equals(DATESTAMP)) {
        //    sb.append("DATE(").append(whereColumn.sqlName()).append(")");
        //} else {
        //sb.append(whereColumn.sqlName());
        //}
        return if (nValues == 1) {
            " WHERE $colName = $placeholder"
        } else {
            val placeholders = stringJoin(listOf(placeholder), sep = ", ", copies = nValues)
            " WHERE $colName IN (${placeholders})"
        }
    }

    fun <M> deleteWhereTemplate(table: Table<M>, whereColumn: Column<M, *>, nValues: Int): String {
        return deleteAllTemplate(table) + makeWhereString(whereColumn, nValues)
    }

    fun <M> deleteWhereNullTemplate(table: Table<M>, whereColumn: Column<M, *>, isNotNull: Boolean): String {
        return deleteAllTemplate(table) + makeWhereString(whereColumn, isNotNull = isNotNull)
    }

    // delete all!
    fun <M> deleteAllTemplate(table: Table<M>): String {
        return "DELETE FROM " + table.name
    }

    // " WHERE (likeColumn[0] LIKE likeValue[0]) OR (likeColumn[1] LIKE likeValue[1]) OR ..."
    internal fun <M> makeWhereLikeString(
        likeColumns: Collection<Column<M, String>>,
        conjunction: Conjuction = Conjuction.OR,
    ): String {
        return when (likeColumns.size) {
            0 -> ""
            1 -> " WHERE " + likeColumns.first().sqlName + " LIKE ?"
            else -> {
                val bracketedWhereClauses: MutableList<String> = ArrayList(likeColumns.size)
                for (c in likeColumns) {
                    bracketedWhereClauses.add("(" + c.sqlName + " LIKE ?)")
                }
                " WHERE " + stringJoin(bracketedWhereClauses, sep = " ${conjunction.sql} ")
            }
        }
    }

    // columns must be a subset of table.columns()
    fun <M> insertTemplate(t: Table<M>, orderedColumns: List<Column<M, *>>): String {
        val placeholders = makeInsertPlaceholders(orderedColumns)
        val columnSql = joinColumns(orderedColumns)
        return "INSERT INTO ${t.name} ($columnSql) VALUES ($placeholders)"
    }

    fun <M, J> updateTemplate(t: Table<M>, orderedColumns: List<Column<M, *>>, keyCol: Column<M, J>): String {
        val columnPlaceholders = makeUpdatePlaceholders(orderedColumns)
        val whereString = makeWhereString(keyCol, 1)
        // TODO dynamic placeholders
        return "UPDATE ${t.name} SET $columnPlaceholders $whereString"
    }


    fun <M : MacrosEntity<M>> makeIdMap(objects: Collection<M>): Map<Long, M> {
        val idMap: MutableMap<Long, M> = LinkedHashMap(objects.size, 1.0f)
        objects.forEach {
            val id : Long = it.id
            require(!idMap.containsKey(id)) { "Two objects had the same ID" }
            idMap[id] = it
        }
        return idMap
    }

    // Returns a list of strings such that each one is a complete SQL statement in the SQL
    // file represented by reader r.
    // IMPORTANT DETAIL:
    // It's not enough to do this just by splitting on semicolons, since there are things
    // like nested statements with 'END;' and strings may contain semicolons.
    // So the split token is the semicolon followed by two blank lines, i.e. statements
    // in the SQL file must be terminated by a semicolon immediately followed by \n\n
    // XXX Possible bug if the newline character is different on different platforms.
    @Throws(IOException::class)
    fun createSplitStatements(r: Reader): List<String> {
        return createStatements(r, "\n")
            .split(";\n\n")
            // a double newline after the last statement will cause an empty final element
            // in the split list, which turns into a null statement ';' after the map.
            // The Android SQL library seems to spew at this (throws SQL exception despite the
            // SQL code being 0 (NOT_AN_ERROR).
            // Anyway let's just filter everything to ensure we don't end up with any null statements
            .filter { it.isNotBlank() }
            // replace remaining newline characters by spaces and add the semicolon back
            .map { it.replace("\n".toRegex(), " ") + ";" }
    }

    @Throws(IOException::class)
    fun createStatements(r: Reader, lineSep: String = " "): String {
        val trimmedDecommentedLines = ArrayList<String>()
        BufferedReader(r).use { reader ->
            // steps: remove all comment lines, trim, join, split on semicolon
            while (reader.ready()) {
                var line = reader.readLine()
                val commentIndex = line.indexOf("--")
                if (commentIndex == 0) {
                    continue // skip comment lines completely
                } else if (commentIndex != -1) {
                    line = line.substring(0, commentIndex)
                }
                line = line.replace("\\s+".toRegex(), " ")
                if (line != " ") {
                    trimmedDecommentedLines.add(line)
                }
            }
        }
        return stringJoin(trimmedDecommentedLines, sep = lineSep)
    }

    // Returns triggers that update the create and modify times of the given table.
    // Columns are determined using the Schema object so ensure it is updated.

    fun <M> createInitTimestampTriggers(t: Table<M>): List<String> {
        val table = t.name
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
            |AFTER UPDATE OF ${stringJoin(columnNames, sep = ", ")} ON $table
            |BEGIN UPDATE $table
            |SET $modifyTime = $currentUnixTime
            |WHERE $id = NEW.${id};
            |END;
        """.trimMargin("|")
        return listOf(initTimeStampTrigger, updateTimestampTrigger)
    }

    @Throws(SQLException::class)
    fun <M, J> rethrowAsSqlException(rawValue: Any?, c: Column<M, J>) {
        throw SQLException("Could not convert value '$rawValue' for column ${c.table}.${c.sqlName} (type ${c.type})")
    }
}