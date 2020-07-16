package com.machfour.macros.storage

import com.machfour.macros.core.Column
import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.Table
import com.machfour.macros.core.datatype.MacrosType
import com.machfour.macros.core.datatype.Types
import com.machfour.macros.util.StringJoiner
import java.io.BufferedReader
import java.io.IOException
import java.io.Reader
import java.sql.SQLException

object DatabaseUtils {
    private fun <M> joinColumns(columns: Iterable<Column<M, *>>, suffix: String = ""): String {
        val stringFunc : (Column<M, *>) -> String = { it.sqlName }
        return StringJoiner.of(columns).sep(", ").stringFunc(stringFunc).suffix(suffix).join()
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
        return StringJoiner.of(columns.map { placeholderFormat(it) }).sep(", ").join()
    }

    // creates SQL placeholders for the given (ordered) list of columns
    private fun <M> makeInsertPlaceholders(columns: List<Column<M, *>>): String {
        return makePlaceholders(columns) { getSqlPlaceholder(it.type) }
    }

    // creates SQL placeholders for the given (ordered) list of columns
    private fun <M> makeUpdatePlaceholders(columns: List<Column<M, *>>): String {
        return makePlaceholders(columns) { it.sqlName + " = " + getSqlPlaceholder(it.type) }
    }

    // " WHERE column1 = ?"
    // " WHERE column1 IN (?, ?, ?, ...?)"
    // if nkeys is <= 0, no where string will be formed, so all objects will be returned.
    private fun makeWhereString(whereColumn: Column<*, *>, nValues: Int): String {
        assert(nValues >= 0)
        if (nValues == 0) {
            return ""
        }
        val colName = whereColumn.sqlName
        val placeholder = getSqlPlaceholder(whereColumn.type)

        //if (whereColumn.type().equals(DATESTAMP)) {
        //    sb.append("DATE(").append(whereColumn.sqlName()).append(")");
        //} else {
        //sb.append(whereColumn.sqlName());
        //}
        return if (nValues == 1) {
            " WHERE $colName = $placeholder"
        } else {
            val placeholders = StringJoiner.of(placeholder).sep(", ").copies(nValues).join()
            " WHERE $colName IN (${placeholders})"
        }
    }

    @JvmStatic
    fun <M> deleteWhereTemplate(table: Table<M>, whereColumn: Column<M, *>, nValues: Int): String {
        return deleteAllTemplate(table) + makeWhereString(whereColumn, nValues)
    }

    // delete all!
    @JvmStatic
    fun <M> deleteAllTemplate(table: Table<M>): String {
        return "DELETE FROM " + table.name
    }

    // " WHERE (likeColumn[0] LIKE likeValue[0]) OR (likeColumn[1] LIKE likeValue[1]) OR ..."
    private fun <M> makeWhereLikeString(likeColumns: List<Column<M, String>>): String {
        return when (likeColumns.size) {
            0 -> ""
            1 -> " WHERE " + likeColumns[0].sqlName + " LIKE ?"
            else -> {
                val bracketedWhereClauses: MutableList<String> = ArrayList(likeColumns.size)
                for (c in likeColumns) {
                    bracketedWhereClauses.add("(" + c.sqlName + " LIKE ?)")
                }
                " WHERE " + StringJoiner.of(bracketedWhereClauses).sep(" OR ").join()
            }
        }
    }

    @JvmStatic
    fun <M> selectLikeTemplate(t: Table<M>, selectColumn: Column<M, *>, likeColumns: List<Column<M, String>>): String {
        return selectTemplate(t, listOf(selectColumn), makeWhereLikeString(likeColumns), false)
    }

    @JvmStatic
    fun <M> selectTemplate(t: Table<M>, selectColumn: Column<M, *>, whereColumn: Column<M, *>, nValues: Int, distinct: Boolean): String {
        return selectTemplate(t, listOf(selectColumn), whereColumn, nValues, distinct)
    }

    @JvmStatic
    fun <M> selectTemplate(t: Table<M>, selectColumn: Column<M, *>, whereColumn: Column<M, *>, nValues: Int): String {
        return selectTemplate(t, listOf(selectColumn), whereColumn, nValues, false)
    }

    @JvmStatic
    fun <M> selectTemplate(t: Table<M>, orderedColumns: List<Column<M, *>>, whereColumn: Column<M, *>, nValues: Int, distinct: Boolean): String {
        return selectTemplate(t, orderedColumns, makeWhereString(whereColumn, nValues), distinct)
    }

    @JvmStatic
    fun <M> selectTemplate(t: Table<M>, orderedColumns: List<Column<M, *>>, whereString: String, distinct: Boolean): String {
        val words: MutableList<String> = ArrayList(6)
        words.add("SELECT")
        if (distinct) {
            words.add("DISTINCT")
        }
        words.add(joinColumns(orderedColumns))
        words.add("FROM")
        words.add(t.name)
        words.add(whereString)
        return StringJoiner.of(words).sep(" ").join()
    }

    // columns must be a subset of table.columns()
    @JvmStatic
    fun <M> insertTemplate(t: Table<M>, orderedColumns: List<Column<M, *>>): String {
        val placeholders = makeInsertPlaceholders(orderedColumns)
        return String.format("INSERT INTO %s (%s) VALUES (%s)", t.name, joinColumns(orderedColumns), placeholders)
    }

    @JvmStatic
    fun <M, J> updateTemplate(t: Table<M>, orderedColumns: List<Column<M, *>>, keyCol: Column<M, J>): String {
        val columnPlaceholders = makeUpdatePlaceholders(orderedColumns)
        val whereString = makeWhereString(keyCol, 1)
        // TODO dynamic placeholders
        return "UPDATE ${t.name} SET $columnPlaceholders $whereString"
    }

    @JvmStatic
    fun <J> makeBindableStrings(objects: Collection<J>, type: MacrosType<J>): Array<String> {
        return objects.map { type.toSqlString(it) }.toTypedArray<String>()
    }

    // for use with Android SQLite implementation
    // The Object array is only allowed to contain String, Long, Double, byte[] and null
    @JvmStatic
    fun <M> makeBindableObjects(data: ColumnData<M>, orderedColumns: List<Column<M, *>>): Array<Any?> {
        return orderedColumns.map { data.getAsRaw(it) }.toTypedArray<Any?>()
    }

    @JvmStatic
    fun <M : MacrosEntity<M>> makeIdMap(objects: Collection<M>): Map<Long, M> {
        val idMap: MutableMap<Long, M> = HashMap(objects.size, 1.0f)
        for (m in objects) {
            assert(!idMap.containsKey(m.id)) { "Two objects had the same ID" }
            idMap[m.id] = m
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
        val splitStatements = createStatements(r, "\n").split(";\n\n").toTypedArray()
        // replace remaining newline characters by spaces and add the semicolon back
        val statements: MutableList<String> = ArrayList(splitStatements.size)
        for (statement in splitStatements) {
            statements.add(statement.replace("\n".toRegex(), " ") + ";")
        }
        return statements
    }

    @JvmStatic
    @JvmOverloads
    @Throws(IOException::class)
    fun createStatements(r: Reader, linesep: String = " "): String {
        val trimmedAndDecommented: MutableList<String> = ArrayList(32)
        BufferedReader(r).use { reader ->
            // steps: remove all comment lines, trim, join, split on semicolon
            while (reader.ready()) {
                var line = reader.readLine()
                val commentIndex = line.indexOf("--")
                if (commentIndex == 0) {
                    // skip comment lines completely
                    continue
                } else if (commentIndex != -1) {
                    line = line.substring(0, commentIndex)
                }
                //line = line.trim();
                // if line was only space but not completely blank, then ignore
                // need to keep track of blank lines so Android can separate SQL statements
                line = line.replace("\\s+".toRegex(), " ")
                if (line != " ") {
                    trimmedAndDecommented.add(line)
                }
            }
        }
        return StringJoiner.of(trimmedAndDecommented).sep(linesep).join()
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun <M, J> rethrowAsSqlException(rawValue: Any?, c: Column<M, J>) {
        throw SQLException("Could not convert value '$rawValue' for column ${c.table}.${c.sqlName} (type ${c.type})")
    }
}