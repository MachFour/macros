package com.machfour.macros.storage

import com.machfour.macros.core.Column
import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.Table
import java.io.IOException
import java.sql.SQLException

abstract class MacrosDatabase : MacrosDataSource {
    // caller-managed connection, useful to reduce number of calls to DB
    // caller needs to call closeConnection() after. Use with begin and end transaction
    @Throws(SQLException::class)
    abstract override fun openConnection()

    @Throws(SQLException::class)
    abstract override fun closeConnection()

    // By default, database functions will autocommit.
    // These functions can be used to temporarily disable autocommit and are useful to group multiple operations together
    @Throws(SQLException::class)
    abstract override fun beginTransaction()

    @Throws(SQLException::class)
    abstract override fun endTransaction()

    @Throws(SQLException::class, IOException::class)
    abstract fun initDb()

    @Throws(SQLException::class)
    abstract fun execRawSQLString(sql: String)

    @Throws(SQLException::class)
    abstract override fun <M> deleteById(id: Long, t: Table<M>): Int

    @Throws(SQLException::class)
    abstract override fun <M> stringSearch(
            t: Table<M>, cols: List<Column<M, String>>, keyword: String, globBefore: Boolean, globAfter: Boolean
    ): List<Long>

    @Throws(SQLException::class)
    abstract override fun <M, I, J> selectColumnMap(
            t: Table<M>, keyColumn: Column<M, I>, valueColumn: Column<M, J>, keys: Set<I>
    ): Map<I, J?>

    // does SELECT (selectColumn) FROM (t) WHERE (whereColumn) = (whereValue)
    // or SELECT (selectColumn) FROM (t) WHERE (whereColumn) IN (whereValue1, whereValue2, ...)
    @Throws(SQLException::class)
    abstract override fun <M, I, J> selectColumn(
            t: Table<M>, selected: Column<M, I>, where: Column<M, J>, whereValues: Collection<J>, distinct: Boolean
    ): List<I?>

    // does DELETE FROM (t) WHERE (whereColumn) = (whereValue)
    // or DELETE FROM (t) WHERE (whereColumn) IN (whereValue1, whereValue2, ...)
    @Throws(SQLException::class)
    abstract override fun <M, J> deleteByColumn(t: Table<M>, whereColumn: Column<M, J>, whereValues: Collection<J>): Int

    // Retrives an object by a key column, and constructs it without any FK object instances.
    // Returns null if no row in the corresponding table had a key with the given value
    @Throws(SQLException::class)
    abstract override fun <M, J> getRawObjectsByKeys(t: Table<M>, keyCol: Column<M, J>, keys: Collection<J>): Map<J, M>

    // TODO get rid of noEmpty
    @Throws(SQLException::class)
    abstract override fun <M, J> getIdsByKeys(t: Table<M>, keyCol: Column<M, J>, keys: Collection<J>): Map<J, Long>

    // returns map of all objects in table, by ID
    // TODO make protected -- but it's useful for CSV export
    @Throws(SQLException::class)
    abstract override fun <M> getAllRawObjects(t: Table<M>): Map<Long, M>

    @Throws(SQLException::class)
    abstract override fun <M : MacrosEntity<M>> insertObjectData(objectData: List<ColumnData<M>>, withId: Boolean): Int

    // Note that if the id is not found in the database, nothing will be inserted
    @Throws(SQLException::class)
    abstract override fun <M : MacrosEntity<M>> updateObjects(objects: Collection<M>): Int

    @Throws(SQLException::class)
    abstract override fun <M : MacrosEntity<M>> idExistsInTable(table: Table<M>, id: Long): Boolean

    @Throws(SQLException::class)
    abstract override fun <M : MacrosEntity<M>> idsExistInTable(table: Table<M>, ids: List<Long>): Map<Long, Boolean>

    @Throws(SQLException::class)
    abstract override fun <M> clearTable(t: Table<M>): Int
}