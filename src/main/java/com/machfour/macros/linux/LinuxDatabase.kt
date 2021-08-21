package com.machfour.macros.linux

import com.machfour.macros.linux.LinuxDatabaseUtils.getColumn
import com.machfour.macros.linux.LinuxDatabaseUtils.processResultSet
import com.machfour.macros.linux.LinuxDatabaseUtils.toRowData
import com.machfour.macros.orm.schema.Tables
import com.machfour.macros.sql.*
import com.machfour.macros.sql.generator.*
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
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

    private val dataSource: SQLiteDataSource = SQLiteDataSource().apply {
        val dbPath = Paths.get(dbFile).toAbsolutePath()
        url = "jdbc:sqlite:${dbPath}"
        config = SQLiteConfig().apply {
            enableRecursiveTriggers(true)
            enforceForeignKeys(true)
        }
    }

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
        this.cachedConnection = null
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
        val getSqlFromFile : (File) -> String = { FileReader(it).use { reader -> SqlUtils.createStatements(reader) } }
        val c = connection
        try {
            c.createStatement().use { s ->
                println("Create schema...")
                val createSchemaSql = getSqlFromFile(config.initSqlFile)
                s.executeUpdate(createSchemaSql)

                println("Add timestamp triggers...")
                Tables.all
                    .flatMap { SqlUtils.createInitTimestampTriggers(it) }
                    .forEach { s.executeUpdate(it) }

                println("Add other triggers...")
                val createTriggersSql = config.trigSqlFiles.map { getSqlFromFile(it) }
                createTriggersSql.forEach { s.executeUpdate(it) }

                println("Add data...")
                val initialDataSql = getSqlFromFile(config.dataSqlFile)
                s.executeUpdate(initialDataSql)
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
        val column1 = query.column1
        val column2 = query.column2
        val resultData = ArrayList<Pair<I?, J?>>()
        executeSelectQuery(query) {
            resultData.add(Pair(it.getColumn(column1), it.getColumn(column2)))
        }
        return resultData
    }

    @Throws(SQLException::class)
    override fun <M> selectMultipleColumns(t: Table<M>, query: MultiColumnSelect<M>): List<RowData<M>> {
        val resultData = ArrayList<RowData<M>>()
        val columns = query.columns
        executeSelectQuery(query) {
            resultData.add(it.toRowData(t, columns))
        }
        return resultData
    }

    @Throws(SQLException::class)
    override fun <M> selectAllColumns(t: Table<M>, query: AllColumnSelect<M>): List<RowData<M>> {
        val resultData = ArrayList<RowData<M>>()
        executeSelectQuery(query) {
            resultData.add(it.toRowData(t))
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
                            LinuxDatabaseUtils.bindObjects(it, listOf(arg))
                            it.executeQuery().processResultSet(resultSetAction)
                            it.clearParameters()
                        }
                    } else {
                        LinuxDatabaseUtils.bindObjects(it, query.getBindArguments())
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
        val prevAutoCommit = c.autoCommit
        c.autoCommit = false
        val statement = SqlUtils.insertTemplate(table, columnsToInsert)
        var currentRow: RowData<M>? = null
        try {
            c.prepareStatement(statement).use { p ->
                for (row in data) {
                    currentRow = row
                    LinuxDatabaseUtils.bindData(p, row, columnsToInsert)
                    saved += p.executeUpdate()
                    p.clearParameters()
                }
                if (prevAutoCommit) {
                    c.commit()
                    c.autoCommit = true
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
        val prevAutoCommit = c.autoCommit
        c.autoCommit = false
        try {
            c.prepareStatement(SqlUtils.updateTemplate(table, table.columns, table.idColumn)).use { p ->
                for (row in data) {
                    LinuxDatabaseUtils.bindData(p, row, table.columns, row[table.idColumn])
                    saved += p.executeUpdate()
                    p.clearParameters()
                }
                if (prevAutoCommit) {
                    c.commit()
                    c.autoCommit = true
                }
            }
        } finally {
            closeIfNecessary(c)
        }
        return saved
    }

    @Throws(SQLException::class)
    override fun <M> deleteFromTable(delete: SimpleDelete<M>): Int {
        var removed: Int
        val c = connection
        try {
            val sql = delete.toSql()
            if (delete.hasBindArguments) {
                c.prepareStatement(sql).use {
                    if (delete.shouldIterateBindArguments) {
                        removed = 0
                        for (arg in delete.getBindArguments()) {
                            LinuxDatabaseUtils.bindObjects(it, listOf(arg))
                            removed += it.executeUpdate()
                            it.clearParameters()
                        }
                    } else {
                        LinuxDatabaseUtils.bindObjects(it, delete.getBindArguments())
                        removed = it.executeUpdate()
                    }
                }
            } else {
                removed = c.createStatement().executeUpdate(sql)
            }
        } finally {
            closeIfNecessary(c)
        }

        return removed
    }
}