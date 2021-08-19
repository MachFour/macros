package com.machfour.macros.linux

import com.machfour.macros.core.*
import com.machfour.macros.orm.schema.Tables
import com.machfour.macros.linux.LinuxDatabaseUtils.getColumn
import com.machfour.macros.linux.LinuxDatabaseUtils.processResultSet
import com.machfour.macros.linux.LinuxDatabaseUtils.toColumnData
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.ColumnData
import com.machfour.macros.sql.Table
import com.machfour.macros.persistence.DatabaseUtils
import com.machfour.macros.persistence.MacrosDatabase
import com.machfour.macros.persistence.MacrosDatabaseImpl
import com.machfour.macros.sql.SqlConfig
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
class LinuxDatabase private constructor(dbFile: String) : MacrosDatabaseImpl(), MacrosDatabase {
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
        val getSqlFromFile : (File) -> String = { FileReader(it).use { reader -> DatabaseUtils.createStatements(reader) } }
        val c = connection
        try {
            c.createStatement().use { s ->
                println("Create schema...")
                val createSchemaSql = getSqlFromFile(config.initSqlFile)
                s.executeUpdate(createSchemaSql)

                println("Add timestamp triggers...")
                Tables.all
                    .flatMap { DatabaseUtils.createInitTimestampTriggers(it) }
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
    override fun <M> deleteById(t: Table<M>, id: Long): Int {
        val c = connection
        try {
            val sqlTemplate = DatabaseUtils.deleteWhereTemplate(t, t.idColumn, 1)
            c.prepareStatement(sqlTemplate).use { statement ->
                LinuxDatabaseUtils.bindObjects(statement, listOf(id))
                statement.executeUpdate()
                return 1
            }
        } finally {
            closeIfNecessary(c)
        }
    }

    @Throws(SQLException::class)
    override fun <M, I> selectColumn(query: SingleColumnSelect<M, I>): List<I?> {
        val selectColumn = query.selectColumn
        val resultList = ArrayList<I?>()
        executeQuery(query) {
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
        executeQuery(query) {
            resultData.add(Pair(it.getColumn(column1), it.getColumn(column2)))
        }
        return resultData
    }

    @Throws(SQLException::class)
    override fun <M> selectMultipleColumns(t: Table<M>, query: MultiColumnSelect<M>): List<ColumnData<M>> {
        val resultData = ArrayList<ColumnData<M>>()
        val columns = query.columns
        executeQuery(query) {
            resultData.add(it.toColumnData(t, columns))
        }
        return resultData
    }

    @Throws(SQLException::class)
    override fun <M> selectAllColumns(t: Table<M>, query: AllColumnSelect<M>): List<ColumnData<M>> {
        val resultData = ArrayList<ColumnData<M>>()
        executeQuery(query) {
            resultData.add(it.toColumnData(t))
        }
        return resultData
    }

    private fun executeQuery(query: SelectQuery<*>, resultSetAction: (ResultSet) -> Unit) {
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
    override fun <M : MacrosEntity<M>> insertObjectData(objectData: List<ColumnData<M>>, withId: Boolean): Int {
        if (objectData.isEmpty()) {
            return 0
        }
        var saved = 0
        val table = objectData[0].table
        // even if we are inserting for the first time, there may be it has an ID that we want to keep intact
        val columnsToInsert = if (withId) {
            table.columns
        } else {
            table.columns.filter { it != table.idColumn }
        }
        val c = connection
        val prevAutoCommit = c.autoCommit
        c.autoCommit = false
        val statement = DatabaseUtils.insertTemplate(table, columnsToInsert)
        try {
            c.prepareStatement(statement).use { p ->
                for (row in objectData) {
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
            val msg = "${e.message} thrown by insertObjectData() on table ${table.name} and object data: ${objectData[saved]}"
            throw SQLException(msg, e.cause)
        } finally {
            closeIfNecessary(c)
        }
        return saved
    }

    override fun executeRawStatement(sql: String) {
        val c = connection

        try {
            c.createStatement().use {
                return it.executeUpdate(sql)
            }
        } finally {
            closeIfNecessary(c)
        }
    }

    // Note that if the id is not found in the database, nothing will be inserted
    @Throws(SQLException::class)
    override fun <M : MacrosEntity<M>> updateObjects(objects: Collection<M>): Int {
        if (objects.isEmpty()) {
            return 0
        }
        var saved = 0
        val table = objects.first().table
        val c = connection
        val prevAutoCommit = c.autoCommit
        c.autoCommit = false
        try {
            c.prepareStatement(DatabaseUtils.updateTemplate(table, table.columns, table.idColumn)).use { p ->
                for (obj in objects) {
                    LinuxDatabaseUtils.bindData(p, obj.data, table.columns, obj.id)
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
    override fun <M> clearTable(t: Table<M>): Int {
        val removed: Int
        val c = connection
        try {
            c.prepareStatement(DatabaseUtils.deleteAllTemplate(t)).use { p -> removed = p.executeUpdate() }
        } finally {
            closeIfNecessary(c)
        }
        return removed
    }

    @Throws(SQLException::class)
    override fun <M, J> deleteByColumn(t: Table<M>, whereColumn: Column<M, J>, whereValues: Collection<J>): Int {
        val removed: Int
        val c = connection
        try {
            c.prepareStatement(DatabaseUtils.deleteWhereTemplate(t, whereColumn, whereValues.size)).use { p ->
                LinuxDatabaseUtils.bindObjects(p, whereValues)
                removed = p.executeUpdate()
            }
        } finally {
            closeIfNecessary(c)
        }
        return removed
    }

    @Throws(SQLException::class)
    override fun <M, J> deleteByNullStatus(t: Table<M>, whereColumn: Column<M, J>, trueForNotNulls: Boolean): Int {
        val removed: Int
        val c = connection
        val template = DatabaseUtils.deleteWhereNullTemplate(t, whereColumn, isNotNull = trueForNotNulls)
        try {
            c.prepareStatement(template).use {
                removed = it.executeUpdate()
            }
        } finally {
            closeIfNecessary(c)
        }
        return removed
    }

}