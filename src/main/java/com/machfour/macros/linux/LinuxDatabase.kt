package com.machfour.macros.linux

import com.machfour.macros.orm.schema.AllTables
import com.machfour.macros.sql.*
import com.machfour.macros.sql.generator.*
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

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

        @Throws(IOException::class)
        @Suppress("NewApi")
        fun deleteIfExists(dbFile: String): Boolean {
            val dbPath = Paths.get(dbFile)
            return if (Files.exists(dbPath)) {
                Files.delete(dbPath)
                true
            } else {
                false
            }
        }

    }

    private val dataSource = makeSQLiteDataSource(dbFile)

    // records how much time spent in database transactions
    private var cachedConnection: Connection? = null

    // returns persistent connection if there is one, otherwise a new temporary one.
    @get:Throws(SQLException::class)
    private val connection: Connection
        get() {
            return cachedConnection ?: dataSource.connection
        }

    @Throws(SQLException::class)
    override fun openConnection() {
        check(cachedConnection == null) { "A persistent connection is open already" }
        cachedConnection = dataSource.connection
    }

    @Throws(SQLException::class)
    override fun closeConnection() {
        val c = cachedConnection
        requireNotNull(c) { "A connection hasn't been opened" }
        // set instance variable to null first in case exception is thrown?
        // or is this shooting oneself in the foot?
        c.close()
        cachedConnection = null
    }

    // true if getConnection() returns a temporary connection that needs to be closed immediately after use
    // or a user-managed persistent connection
    private val isTempConnection: Boolean
        get() = cachedConnection == null

    @Throws(SQLException::class)
    private fun closeIfNecessary(c: Connection) {
        if (isTempConnection) {
            c.close()
        }
    }

    @Throws(SQLException::class)
    override fun beginTransaction() {
        val cc = cachedConnection
        checkNotNull(cc) { "Connection not open, call openConnection() first" }
        cc.autoCommit = false
    }

    @Throws(SQLException::class)
    override fun endTransaction() {
        val cc = cachedConnection
        checkNotNull(cc) { "Connection not open, call openConnection first() (and beginTransaction())" }
        cc.commit()
        cc.autoCommit = true
    }

    @Throws(SQLException::class, IOException::class)
    override fun initDb(config: SqlConfig) {
        // TODO insert data from Units and Nutrients classes instead of initial data
        val getSqlFromFile: (File) -> String = { FileReader(it).use { r -> createSqlStatements(r) } }
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
        } finally {
            closeIfNecessary(c)
        }
    }

    @Throws(SQLException::class)
    override fun execRawSQLString(sql: String) {
        val c = connection
        try {
            c.createStatement().use {
                it.execute(sql)
            }

        } finally {
            closeIfNecessary(c)
        }
    }

    @Throws(SQLException::class)
    override fun <M, I> selectColumn(query: SingleColumnSelect<M, I>): List<I?> {
        val selectColumn = query.selectColumn
        val resultList = ArrayList<I?>()
        executeSelectQuery(query) {
            val value = it.getColumn(selectColumn)
            resultList.add(value)
        }
        return resultList
    }

    @Throws(SQLException::class)
    override fun <M, I, J> selectTwoColumns(query: TwoColumnSelect<M, I, J>): List<Pair<I?, J?>> {
        val resultData = ArrayList<Pair<I?, J?>>()
        executeSelectQuery(query) {
            resultData.add(Pair(it.getColumn(query.column1), it.getColumn(query.column2)))
        }
        return resultData
    }

    @Throws(SQLException::class)
    override fun <M> selectMultipleColumns(query: MultiColumnSelect<M>): List<RowData<M>> {
        val resultData = ArrayList<RowData<M>>()
        executeSelectQuery(query) {
            resultData.add(it.toRowData(query.table, query.columns))
        }
        return resultData
    }

    @Throws(SQLException::class)
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
        } finally {
            closeIfNecessary(c)
        }
    }

    @Throws(SQLException::class)
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
        } catch (e: SQLException) {
            val msg = "${e.message} thrown by insertObjectData() on table ${table.name} and object data: $currentRow"
            throw SQLException(msg, e.cause)
        } finally {
            closeIfNecessary(c)
        }
        return saved
    }

    override fun executeRawStatement(sql: String) {
        val c = connection
        try {
            c.createStatement().use { it.executeUpdate(sql) }
        } finally {
            closeIfNecessary(c)
        }
    }

    @Throws(SQLException::class)
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
        } finally {
            closeIfNecessary(c)
        }
        return saved
    }

    @Throws(SQLException::class)
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
        } finally {
            closeIfNecessary(c)
        }
    }
}