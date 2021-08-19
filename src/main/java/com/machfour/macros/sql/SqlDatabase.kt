package com.machfour.macros.sql

import com.machfour.macros.sql.generator.*
import java.sql.SQLException

interface SqlDatabase {
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
    fun <M> selectMultipleColumns(t: Table<M>, query: MultiColumnSelect<M>): List<RowData<M>>

    @Throws(SQLException::class)
    fun <M> selectAllColumns(t: Table<M>, query: AllColumnSelect<M>): List<RowData<M>>

    @Throws(SQLException::class)
    fun <M> updateRows(data: Collection<RowData<M>>): Int

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
    fun <M> deleteFromTable(t: Table<M>, delete: SimpleDelete<M>): Int

    @Throws(SQLException::class)
    fun <M, J> updateColumn(t: Table<M>, update: SingleColumnUpdate<M, J>): Int

    @Throws(SQLException::class)
    fun <M> insertRows(data: Collection<RowData<M>>, withId: Boolean): Int

    // TODO add insert/update template

    @Throws(SQLException::class)
    fun executeRawStatement(sql: String)

}