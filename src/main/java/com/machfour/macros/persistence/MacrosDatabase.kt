package com.machfour.macros.persistence

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.ColumnData
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.generator.AllColumnSelect
import com.machfour.macros.sql.generator.MultiColumnSelect
import com.machfour.macros.sql.generator.SingleColumnSelect
import com.machfour.macros.sql.generator.TwoColumnSelect
import java.sql.SQLException

interface MacrosDatabase {
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
    fun <M, I> selectColumn(query: SingleColumnSelect<M, I>): List<I?>

    @Throws(SQLException::class)
    fun <M, I> selectNonNullColumn(query: SingleColumnSelect<M, I>): List<I>

    @Throws(SQLException::class)
    fun <M, I, J> selectTwoColumns(query: TwoColumnSelect<M, I, J>): List<Pair<I?, J?>>

    @Throws(SQLException::class)
    fun <M> selectMultipleColumns(t: Table<M>, query: MultiColumnSelect<M>): List<ColumnData<M>>

    @Throws(SQLException::class)
    fun <M> selectAllColumns(t: Table<M>, query: AllColumnSelect<M>): List<ColumnData<M>>

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> updateObjects(objects: Collection<M>): Int

    // TODO replace with delete template
    // does DELETE FROM (t) WHERE (whereColumn) = (whereValue)
    // or DELETE FROM (t) WHERE (whereColumn) IN (whereValue1, whereValue2, ...)
    @Throws(SQLException::class)
    fun <M, J> deleteByColumn(t: Table<M>, whereColumn: Column<M, J>, whereValues: Collection<J>): Int

    // TODO replace with delete template
    // does DELETE FROM (t) WHERE (whereColumn) IS (NOT) NULL
    @Throws(SQLException::class)
    fun <M, J> deleteByNullStatus(t: Table<M>, whereColumn: Column<M, J>, trueForNotNulls: Boolean): Int

    // TODO replace with delete template
    @Throws(SQLException::class)
    fun <M> deleteById(t: Table<M>, id: Long): Int

    // TODO replace with delete template
    @Throws(SQLException::class)
    fun <M> clearTable(t: Table<M>): Int

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> insertObjectData(objectData: List<ColumnData<M>>, withId: Boolean): Int

    // XXX
    @Throws(SQLException::class)
    fun executeRawStatement(sql: String)

}