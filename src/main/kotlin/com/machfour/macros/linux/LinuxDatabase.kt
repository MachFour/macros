package com.machfour.macros.linux

import com.machfour.macros.core.*
import com.machfour.macros.core.datatype.TypeCastException
import com.machfour.macros.storage.DatabaseUtils
import com.machfour.macros.storage.MacrosDataSource
import com.machfour.macros.storage.MacrosDatabase
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
import java.util.Collections;

// data source provided by Xerial library
class LinuxDatabase private constructor(dbFile: String) : MacrosDatabase(), MacrosDataSource {
    companion object {
        // singleton
        private lateinit var instance: LinuxDatabase
        private lateinit var dbPath: String

        @JvmStatic
        fun getInstance(dbFile: String): LinuxDatabase {
            if (!Companion::instance.isInitialized || dbFile != dbPath) {
                instance = LinuxDatabase(dbFile)
                dbPath = dbFile
            }
            return instance
        }

        @Throws(IOException::class)
        @JvmStatic
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
        requireNotNull(cachedConnection) { "A connection hasn't been opened" }
        // set instance variable to null first in case exception is thrown?
        // or is this shooting oneself in the foot?
        val c: Connection = cachedConnection!!
        this.cachedConnection = null
        c.close()
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
        checkNotNull(cachedConnection) { "Connection not open, call openConnection() first" }
        cachedConnection!!.autoCommit = false
    }

    @Throws(SQLException::class)
    override fun endTransaction() {
        // XXX messy variable access
        checkNotNull(cachedConnection) { "Connection not open, call openConnection first() (and beginTransaction())" }
        cachedConnection!!.commit()
        cachedConnection!!.autoCommit = true
    }

    @Throws(SQLException::class, IOException::class)
    override fun initDb() {
        val getSqlFromFile : (File) -> String = { FileReader(it).use { reader -> DatabaseUtils.createStatements(reader) } }
        val c = connection
        try {
            c.createStatement().use { s ->
                val createSchemaSql = getSqlFromFile(LinuxConfig.INIT_SQL)
                val createTriggersSql = getSqlFromFile(LinuxConfig.TRIG_SQL)
                val initialDataSql = getSqlFromFile(LinuxConfig.DATA_SQL)
                println("Create schema...")
                s.executeUpdate(createSchemaSql)
                println("Add triggers...")
                s.executeUpdate(createTriggersSql)
                println("Add data...")
                s.executeUpdate(initialDataSql)
            }
        } finally {
            closeIfNecessary(c)
        }
    }

    @Throws(SQLException::class)
    override fun <M> deleteById(id: Long, t: Table<M>): Int {
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

    /*
     * Returns empty list for either blank keyword or column list
     */
    @Throws(SQLException::class)
    override fun <M> stringSearch(t: Table<M>, cols: List<Column<M, String>>,
                                  keyword: String, globBefore: Boolean, globAfter: Boolean): List<Long> {
        val resultList: MutableList<Long> = ArrayList(0)
        if (keyword.isNotEmpty() && cols.isNotEmpty()) {
            // TODO copy-pasted from SelectColumn... probably needs refactoring
            val c = connection
            try {
                c.prepareStatement(DatabaseUtils.selectLikeTemplate(t, t.idColumn, cols)).use { p ->
                    // have to append the percent sign for LIKE globbing to the actual argument string
                    val keywordGlob = (if (globBefore) "%" else "") + keyword + if (globAfter) "%" else ""
                    val keywordCopies = Collections.nCopies(cols.size, keywordGlob)
                    LinuxDatabaseUtils.bindObjects(p, keywordCopies)
                    p.executeQuery().use { rs ->
                        rs.next()
                        while (!rs.isAfterLast) {
                            resultList.add(rs.getLong(1))
                            rs.next()
                        }
                    }
                }
            } finally {
                closeIfNecessary(c)
            }
        }
        return resultList
    }

    @Throws(SQLException::class)
    override fun <M, I, J> selectColumnMap(t: Table<M>, keyColumn: Column<M, I>,
                                           valueColumn: Column<M, J>, keys: Set<I>): Map<I, J?> {
        val resultMap: MutableMap<I, J?> = LinkedHashMap(keys.size, 1.0f)
        // for batch queries
        //List<Column<M, ?>> selectColumns = Arrays.asList(keyColumn, valueColumn);
        val selectColumns: List<Column<M, *>> = listOf(valueColumn)
        val c = connection
        try {
            c.prepareStatement(DatabaseUtils.selectTemplate(t, selectColumns, keyColumn, 1, false)).use { p ->
                // should be distinct by default: assert keyColumn.isUnique();
                for (key in keys) {
                    // do queries one by one so we don't send a huge number of parameters at once
                    LinuxDatabaseUtils.bindObjects(p, listOf(key))
                    p.executeQuery().use { rs ->
                        rs.next()
                        while (!rs.isAfterLast) {

                            //I key = keyColumn.getType().fromRaw(rs.getObject(keyColumn.sqlName()));
                            val rawValue = rs.getObject(valueColumn.sqlName)
                            try {
                                val value : J? = valueColumn.type.fromRaw(rawValue)
                                assert(!resultMap.containsKey(key)) { "Two rows in the DB contained the same data in the key column!" }
                                resultMap[key] = value
                            } catch (e: TypeCastException) {
                                DatabaseUtils.rethrowAsSqlException(rawValue, valueColumn)
                            }
                            rs.next()
                        }
                    }
                    p.clearParameters()
                }
            }
        } finally {
            closeIfNecessary(c)
        }
        return resultMap
    }

    // does SELECT (selectColumn) FROM (t) WHERE (whereColumn) = (whereValue)
    // or SELECT (selectColumn) FROM (t) WHERE (whereColumn) IN (whereValue1, whereValue2, ...)
    @Throws(SQLException::class)
    override fun <M, I, J> selectColumn(t: Table<M>, selected: Column<M, I>, where: Column<M, J>, whereValues: Collection<J>,
                                        distinct: Boolean): List<I?> {
        val resultList: MutableList<I?> = ArrayList(0)
        val c = connection
        try {
            c.prepareStatement(DatabaseUtils.selectTemplate(t, selected, where, whereValues.size, distinct)).use { p ->
                LinuxDatabaseUtils.bindObjects(p, whereValues)
                p.executeQuery().use { rs ->
                    rs.next()
                    while (!rs.isAfterLast) {
                        val resultValue = rs.getObject(selected.sqlName)
                        try {
                            resultList.add(selected.type.fromRaw(resultValue))
                        } catch (e: TypeCastException) {
                            DatabaseUtils.rethrowAsSqlException(resultValue, selected)
                        }
                        rs.next()
                    }
                }
            }
        } finally {
            closeIfNecessary(c)
        }
        return resultList
    }

    // Constructs a map of key column value to raw object data (i.e. no object references initialised
    // Keys that do not exist in the database will not be contained in the output map
    // The returned map is never null
    @Throws(SQLException::class)
    override fun <M, J> getRawObjectsByKeysNoEmpty(t: Table<M>, keyCol: Column<M, J>, keys: Collection<J>): Map<J, M> {
        // if the list of keys is empty, every row will be returned
        assert(!keys.isEmpty()) { "List of keys is empty" }
        assert(!keyCol.isNullable && keyCol.isUnique) { "Key column can't be nullable and must be unique" }
        val objects: MutableMap<J, M> = LinkedHashMap(keys.size, 1.0f)
        val c = connection
        try {
            c.prepareStatement(DatabaseUtils.selectTemplate(t, t.columns, keyCol, keys.size, false)).use { p ->
                LinuxDatabaseUtils.bindObjects(p, keys)
                p.executeQuery().use { rs ->
                    rs.next()
                    while (!rs.isAfterLast) {
                        val data = ColumnData(t)
                        LinuxDatabaseUtils.fillColumnData(data, rs)
                        val key = data[keyCol]!!
                        assert(!objects.containsKey(key)) { "Key $key already in returned objects map!" }
                        val newObject = t.factory.construct(data, ObjectSource.DATABASE)
                        objects[key] = newObject
                        rs.next()
                    }
                }
            }
        } finally {
            closeIfNecessary(c)
        }
        return objects
    }

    // Constructs a map of key column value to ID
    // Keys that do not exist in the database will not be contained in the output map
    // The returned map is never null
    @Throws(SQLException::class)
    override fun <M, J> getIdsByKeysNoEmpty(t: Table<M>, keyCol: Column<M, J>, keys: Collection<J>): Map<J, Long> {
        // if the list of keys is empty, every row will be returned
        assert(!keys.isEmpty()) { "List of keys is empty" }
        assert(!keyCol.isNullable && keyCol.isUnique) { "Key column can't be nullable and must be unique" }
        val keyAndId = listOf(keyCol, t.idColumn) // select the ID column plus the key
        val idMap: MutableMap<J, Long> = LinkedHashMap(keys.size, 1.0f)
        val c = connection
        try {
            c.prepareStatement(DatabaseUtils.selectTemplate(t, keyAndId, keyCol, keys.size, false)).use { p ->
                LinuxDatabaseUtils.bindObjects(p, keys)
                p.executeQuery().use { rs ->
                    rs.next()
                    while (!rs.isAfterLast) {
                        val id = rs.getLong(t.idColumn.sqlName)
                        val key = keyCol.type.cast(rs.getObject(keyCol.sqlName))!! //XXX wow
                        assert(!idMap.containsKey(key)) { "Key $key already in returned map!" }
                        idMap[key] = id
                        rs.next()
                    }
                }
            }
        } finally {
            closeIfNecessary(c)
        }
        return idMap
    }

    @Throws(SQLException::class)  // returns a map of objects by ID
    // TODO make protected
    override fun <M> getAllRawObjects(t: Table<M>): Map<Long, M> {
        val objects: MutableMap<Long, M> = LinkedHashMap()
        val c = connection
        try {
            c.createStatement().executeQuery("SELECT * FROM " + t.name).use { rs ->
                rs.next()
                while (!rs.isAfterLast) {
                    val data = ColumnData(t)
                    LinuxDatabaseUtils.fillColumnData(data, rs)
                    val id = data.get(t.idColumn)
                    assert(!objects.containsKey(id)) { "ID $id already in returned objects map!" }
                    val newObject = t.factory.construct(data, ObjectSource.DATABASE)
                    objects[id!!] = newObject
                    rs.next()
                }
            }
        } finally {
            closeIfNecessary(c)
        }
        return objects
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

    // Note that if the id is not found in the database, nothing will be inserted
    @Throws(SQLException::class)
    override fun <M : MacrosEntity<M>> updateObjects(objects: Collection<M>): Int {
        if (objects.isEmpty()) {
            return 0
        }
        var saved = 0
        val table = objects.iterator().next().table
        val c = connection
        val prevAutoCommit = c.autoCommit
        c.autoCommit = false
        try {
            c.prepareStatement(DatabaseUtils.updateTemplate(table, table.columns, table.idColumn)).use { p ->
                for (obj in objects) {
                    LinuxDatabaseUtils.bindData(p, obj.allData, table.columns, obj.id)
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
        var removed = 0
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
        var removed = 0
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
    override fun <M : MacrosEntity<M>> idExistsInTable(table: Table<M>, id: Long): Boolean {
        val idCol = table.idColumn.sqlName
        val query = "SELECT COUNT(" + idCol + ") AS count FROM " + table.name + " WHERE " + idCol + " = " + id
        var exists = false
        val c = connection
        try {
            c.createStatement().use { s ->
                val rs = s.executeQuery(query)
                rs.next()
                exists = rs.getInt("count") == 1
            }
        } finally {
            closeIfNecessary(c)
        }
        return exists
    }

    @Throws(SQLException::class)
    override fun <M : MacrosEntity<M>> idsExistInTable(table: Table<M>, ids: List<Long>): Map<Long, Boolean> {
        val idCol: Column<M, Long> = table.idColumn
        val idMap: MutableMap<Long, Boolean> = LinkedHashMap(ids.size, 1.0f)
        val c = connection
        try {
            c.prepareStatement(DatabaseUtils.selectTemplate(table, idCol, idCol, ids.size)).use { p ->
                LinuxDatabaseUtils.bindObjects(p, ids)
                val rs = p.executeQuery()
                rs.next()
                while (!rs.isAfterLast) {
                    val id = rs.getLong(idCol.sqlName)
                    idMap[id] = true
                    rs.next()
                }
                rs.next()
            }
        } finally {
            closeIfNecessary(c)
        }
        // check for missing IDs
        for (id in ids) {
            if (!idMap.containsKey(id)) {
                idMap[id] = false
            }
        }
        return idMap
    }
}