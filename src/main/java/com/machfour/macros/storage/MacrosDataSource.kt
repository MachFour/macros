package com.machfour.macros.storage

import com.machfour.macros.core.Column
import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.Table
import java.sql.SQLException

interface MacrosDataSource {
    // Used to create a persistent connection that lasts across calls to the DB.
    // Caller MUST call closeConnection in a finally block
    @Throws(SQLException::class)
    fun openConnection()

    @Throws(SQLException::class)
    fun closeConnection()

    // By default, database functions will autocommit.
    // These functions can be used to temporarily disable autocommit and are useful to group multiple operations together
    @Throws(SQLException::class)
    fun beginTransaction()

    @Throws(SQLException::class)
    fun endTransaction()

    @Throws(SQLException::class)
    fun <M> stringSearch(t: Table<M>, cols: List<Column<M, String>>, keyword: String, globBefore: Boolean, globAfter: Boolean): List<Long>

    @Throws(SQLException::class)
    fun <M, I, J> selectColumn(t: Table<M>, selected: Column<M, I>, where: Column<M, J>, whereValues: Collection<J>, distinct: Boolean): List<I?>

    @Throws(SQLException::class)
    fun <M, I, J> selectColumnMap(t: Table<M>, keyColumn: Column<M, I>, valueColumn: Column<M, J>, keys: Set<I>): Map<I, J?>

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> updateObjects(objects: Collection<M>): Int

    // does DELETE FROM (t) WHERE (whereColumn) = (whereValue)
    // or DELETE FROM (t) WHERE (whereColumn) IN (whereValue1, whereValue2, ...)
    @Throws(SQLException::class)
    fun <M, J> deleteByColumn(t: Table<M>, whereColumn: Column<M, J>, whereValues: Collection<J>): Int

    // does DELETE FROM (t) WHERE (whereColumn) IS (NOT) NULL
    @Throws(SQLException::class)
    fun <M, J> deleteByNullStatus(t: Table<M>, whereColumn: Column<M, J>, trueForNotNulls: Boolean): Int

    @Throws(SQLException::class)
    fun <M> deleteById(t: Table<M>, id: Long): Int

    @Throws(SQLException::class)
    fun <M> clearTable(t: Table<M>): Int

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> idExistsInTable(table: Table<M>, id: Long): Boolean

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> idsExistInTable(table: Table<M>, ids: List<Long>): Map<Long, Boolean>

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> insertObjectData(objectData: List<ColumnData<M>>, withId: Boolean): Int

    @Throws(SQLException::class)
    fun <M, J> getIdsByKeys(t: Table<M>, keyCol: Column<M, J>, keys: Collection<J>): Map<J, Long>

    @Throws(SQLException::class)
    fun <M, J> getRawObjectsByKeys(t: Table<M>, keyCol: Column<M, J>, keys: Collection<J>): Map<J, M>

    @Throws(SQLException::class)
    fun <M> getAllRawObjects(t: Table<M>): Map<Long, M>
}