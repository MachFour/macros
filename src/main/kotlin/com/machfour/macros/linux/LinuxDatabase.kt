package com.machfour.macros.linux

import com.machfour.macros.jvm.readSqlStatements
import com.machfour.macros.jvm.wrapAsNativeException
import com.machfour.macros.schema.AllTables
import com.machfour.macros.sql.*
import com.machfour.macros.sql.datatype.TypeCastException
import com.machfour.macros.sql.generator.*
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

// data source provided by Xerial library
class LinuxDatabase private constructor(dbFile: String) : SqlDatabaseImpl(), SqlDatabase {
    companion object {
        // singleton
        private lateinit var instance: LinuxDatabase
        private lateinit var dbPath: String

        fun getInstance(dbFile: String): LinuxDatabase {
            if (!Companion::instance.isInitialized || dbFile != dbPath) {
                instance = LinuxDatabase(dbFile)
                dbPath = dbFile
            }
            return instance
        }

        @Suppress("NewApi")
        @Throws(SqlException::class)
        fun deleteIfExists(dbFile: String): Boolean {
            try {
                val dbPath = Paths.get(dbFile)
                return if (Files.exists(dbPath)) {
                    Files.delete(dbPath)
                    true
                } else {
                    false
                }
            } catch (e: IOException) {
                throw e.wrapAsNativeException()
            }
        }

        // Helper SQL methods
        private fun <M> bindData(p: PreparedStatement, values: RowData<M>, orderedColumns: List<Column<M, *>>) {
            bindData(p, values, orderedColumns, null)
        }

        private fun <M> bindData(p: PreparedStatement, values: RowData<M>, orderedColumns: List<Column<M, *>>, extra: Any?) {
            var colIndex = 1 // parameters are 1 indexed!
            for (col in orderedColumns) {
                // Internally, setObject() relies on a ladder of instanceof checks
                p.setObject(colIndex, values.getAsRaw(col))
                colIndex++
            }
            if (extra != null) {
                p.setObject(colIndex, extra)
            }
        }

        private fun <E> bindObjects(p: PreparedStatement, objects: Collection<E>) {
            var colIndex = 1 // parameters are 1 indexed!
            for (o in objects) {
                p.setObject(colIndex, o)
                colIndex++
            }
        }

        private fun <J: Any> ResultSet.getColumn(column: Column<*, J>) : J? {
            val resultValue = getObject(column.sqlName)
            return try {
                column.type.fromRaw(resultValue)
            } catch (e: TypeCastException) {
                // this line throws an exception, so code won't reach here
                throw SqlException.forTypeCastError(resultValue, column)
            }
        }

        private fun <M> fillRowData(data: RowData<M>, rs: ResultSet, columns: Collection<Column<M, *>> = data.table.columns) {
            for (col in columns) {
                val rawValue = rs.getObject(col.sqlName)
                try {
                    data.putFromRaw(col, rawValue)
                } catch (e: TypeCastException) {
                    throw SqlException.forTypeCastError(rawValue, col)
                }
            }
        }

        private fun <M> ResultSet.toRowData(table: Table<M>, columns: Collection<Column<M, *>> = table.columns) : RowData<M> {
            val data = RowData(table, columns)
            fillRowData(data, this, columns)
            return data
        }

        private fun ResultSet.processResultSet(resultSetAction: (ResultSet) -> Unit) {
            use {
                it.next()
                while (!it.isAfterLast) {
                    resultSetAction(it)
                    it.next()
                }
            }
        }

        private fun withDisabledAutoCommit(c: Connection, block: () -> Unit) {
            val prevAutoCommit = c.autoCommit
            c.autoCommit = false
            block()
            if (prevAutoCommit) {
                c.commit()
                c.autoCommit = true
            }
        }

    }

    private val dataSource = makeSQLiteDataSource(dbFile)

    // records how much time spent in database transactions
    private var cachedConnection: Connection? = null

    // returns persistent connection if there is one, otherwise a new temporary one.
    private val connection: Connection
        @Throws(SqlException::class)
        get() {
            try {
                return cachedConnection ?: dataSource.connection
            } catch (e: java.sql.SQLException) {
                throw e.wrapAsNativeException()
            }
        }

    @Throws(SqlException::class)
    override fun openConnection() {
        try {
            check(cachedConnection == null) { "A persistent connection is open already" }
            cachedConnection = dataSource.connection
        } catch (e: java.sql.SQLException) {
            throw e.wrapAsNativeException()
        }
    }

    @Throws(SqlException::class)
    override fun closeConnection() {
        try {
            val c = cachedConnection
            requireNotNull(c) { "A connection hasn't been opened" }
            // set instance variable to null first in case exception is thrown?
            // or is this shooting oneself in the foot?
            c.close()
            cachedConnection = null
        } catch (e: java.sql.SQLException) {
            throw e.wrapAsNativeException()
        }
}

    // true if getConnection() returns a temporary connection that needs to be closed immediately after use
    // or a user-managed persistent connection
    private val isTempConnection: Boolean
        get() = cachedConnection == null

    @Throws(SqlException::class)
    private fun closeIfNecessary(c: Connection) {
        if (isTempConnection) {
            try {
                c.close()
            } catch (e: java.sql.SQLException) {
                throw e.wrapAsNativeException()
            }
        }
    }

    @Throws(SqlException::class)
    override fun beginTransaction() {
        try {
            val cc = cachedConnection
            checkNotNull(cc) { "Connection not open, call openConnection() first" }
            cc.autoCommit = false
        } catch (e: java.sql.SQLException) {
            throw e.wrapAsNativeException()
        }
    }

    @Throws(SqlException::class)
    override fun endTransaction() {
        try {
            val cc = cachedConnection
            checkNotNull(cc) { "Connection not open, call openConnection() first (and beginTransaction())" }
            cc.commit()
            cc.autoCommit = true
        } catch (e: java.sql.SQLException) {
            throw e.wrapAsNativeException()
        }
    }

    @Throws(SqlException::class)
    override fun initDb(config: SqlConfig) {
        // TODO insert data from Units and Nutrients classes instead of initial data
        val getSqlFromFile: (File) -> String = { FileReader(it).use { r -> readSqlStatements(r) } }
        val c = connection
        try {
            c.createStatement().use { s ->
                println("Create schema...")
                s.executeUpdate(getSqlFromFile(config.initSqlFile))

                println("Add timestamp triggers...")
                AllTables
                    .flatMap { createInitTimestampTriggers(it) }
                    .forEach { s.executeUpdate(it) }

                println("Add other triggers...")
                // val createTriggersSql = ...
                config.trigSqlFiles
                    .map { getSqlFromFile(it) }
                    .forEach { s.executeUpdate(it) }

                println("Add data...")
                // val initialDataSql = ...
                s.executeUpdate(getSqlFromFile(config.dataSqlFile))
            }
        } catch (e: java.sql.SQLException) {
            throw e.wrapAsNativeException()
        } catch (e: IOException) {
            throw e.wrapAsNativeException()
        } finally {
            closeIfNecessary(c)
        }
    }

    @Throws(SqlException::class)
    override fun execRawSQLString(sql: String) {
        val c = connection
        try {
            c.createStatement().use {
                it.execute(sql)
            }
        } catch (e: java.sql.SQLException) {
            throw e.wrapAsNativeException()
        } finally {
            closeIfNecessary(c)
        }
    }

    @Throws(SqlException::class)
    override fun <M, I: Any> selectColumn(query: SingleColumnSelect<M, I>): List<I?> {
        val selectColumn = query.selectColumn
        val resultList = ArrayList<I?>()
        executeSelectQuery(query) {
            val value = it.getColumn(selectColumn)
            resultList.add(value)
        }
        return resultList
    }

    @Throws(SqlException::class)
    override fun <M, I: Any, J: Any> selectTwoColumns(query: TwoColumnSelect<M, I, J>): List<Pair<I?, J?>> {
        val resultData = ArrayList<Pair<I?, J?>>()
        executeSelectQuery(query) {
            resultData.add(it.getColumn(query.column1) to it.getColumn(query.column2))
        }
        return resultData
    }

    @Throws(SqlException::class)
    override fun <M> selectMultipleColumns(query: MultiColumnSelect<M>): List<RowData<M>> {
        val resultData = ArrayList<RowData<M>>()
        executeSelectQuery(query) {
            resultData.add(it.toRowData(query.table, query.columns))
        }
        return resultData
    }

    @Throws(SqlException::class)
    override fun <M> selectAllColumns(query: AllColumnSelect<M>): List<RowData<M>> {
        val resultData = ArrayList<RowData<M>>()
        executeSelectQuery(query) {
            resultData.add(it.toRowData(query.table))
        }
        return resultData
    }

    private fun executeSelectQuery(query: SelectQuery<*>, resultSetAction: (ResultSet) -> Unit) {
        val c = connection
        try {
            val sql = query.toSql()
            if (query.hasBindArguments) {
                c.prepareStatement(sql).use {
                    if (query.shouldIterateBindArguments) {
                        for (arg in query.getBindArguments()) {
                            bindObjects(it, listOf(arg))
                            it.executeQuery().processResultSet(resultSetAction)
                            it.clearParameters()
                        }
                    } else {
                        bindObjects(it, query.getBindArguments())
                        it.executeQuery().processResultSet(resultSetAction)
                    }
                }
            } else {
                c.createStatement().executeQuery(sql).processResultSet(resultSetAction)
            }
        } catch (e: java.sql.SQLException) {
            throw e.wrapAsNativeException()
        } finally {
            closeIfNecessary(c)
        }
    }

    @Throws(SqlException::class)
    override fun <M> insertRows(data: Collection<RowData<M>>, withId: Boolean): Int {
        if (data.isEmpty()) {
            return 0
        }
        var saved = 0
        val table = data.first().table
        // even if we are inserting for the first time, there may be it has an ID that we want to keep intact
        val columnsToInsert = if (withId) {
            table.columns
        } else {
            table.columns.filter { it != table.idColumn }
        }
        val c = connection
        val statement = sqlInsertTemplate(table, columnsToInsert)
        var currentRow: RowData<M>? = null
        try {
            withDisabledAutoCommit(c) {
                c.prepareStatement(statement).use { p ->
                    for (row in data) {
                        currentRow = row
                        bindData(p, row, columnsToInsert)
                        saved += p.executeUpdate()
                        p.clearParameters()
                    }
                }
            }
        } catch (e: SqlException) {
            val msg = "${e.message} thrown by insertObjectData() on table ${table.name} and object data: $currentRow"
            throw SqlException(msg, e.cause)
        } finally {
            closeIfNecessary(c)
        }
        return saved
    }

    override fun executeRawStatement(sql: String) {
        val c = connection
        try {
            c.createStatement().use { it.executeUpdate(sql) }
        } catch (e: java.sql.SQLException) {
            throw e.wrapAsNativeException()
        } finally {
            closeIfNecessary(c)
        }
    }

    @Throws(SqlException::class)
    override fun <M> updateRows(data: Collection<RowData<M>>): Int {
        if (data.isEmpty()) {
            return 0
        }
        var saved = 0
        val table = data.first().table
        val c = connection
        try {
            withDisabledAutoCommit(c) {
                c.prepareStatement(sqlUpdateTemplate(table, table.columns, table.idColumn)).use { p ->
                    for (row in data) {
                        bindData(p, row, table.columns, row[table.idColumn])
                        saved += p.executeUpdate()
                        p.clearParameters()
                    }
                }
            }
        } catch (e: java.sql.SQLException) {
            throw e.wrapAsNativeException()
        } finally {
            closeIfNecessary(c)
        }
        return saved
    }

    @Throws(SqlException::class)
    override fun <M> deleteFromTable(delete: SimpleDelete<M>): Int {
        val sql = delete.toSql()
        val c = connection
        try {
            if (delete.hasBindArguments) {
                c.prepareStatement(sql).use {
                    return if (delete.shouldIterateBindArguments) {
                        var removed = 0
                        withDisabledAutoCommit(c) {
                            for (arg in delete.getBindArguments()) {
                                bindObjects(it, listOf(arg))
                                removed += it.executeUpdate()
                                it.clearParameters()
                            }
                        }
                        removed
                    } else {
                        bindObjects(it, delete.getBindArguments())
                        it.executeUpdate()
                    }
                }
            } else {
                return c.createStatement().executeUpdate(sql)
            }
        } catch (e: java.sql.SQLException) {
            throw e.wrapAsNativeException()
        } finally {
            closeIfNecessary(c)
        }
    }
}