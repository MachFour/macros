package com.machfour.macros.sql

import com.machfour.macros.core.EntityId
import com.machfour.macros.sql.generator.*

interface SqlDatabase {
    // Used to create a persistent connection that lasts across calls to the DB.
    // Caller MUST call closeConnection in a finally block
    @Throws(SqlException::class)
    fun openConnection(getGeneratedKeys: Boolean = false)

    @Throws(SqlException::class)
    fun closeConnection()

    // By default, database functions will autocommit.
    // These functions can be used to temporarily disable autocommit and are useful to group multiple operations together
    @Throws(SqlException::class)
    fun beginTransaction()

    @Throws(SqlException::class)
    fun endTransaction()

    @Throws(SqlException::class)
    fun <M, I: Any> selectColumn(query: SingleColumnSelect<M, I>): List<I?>

    @Throws(SqlException::class)
    fun <M, I: Any> selectNonNullColumn(query: SingleColumnSelect<M, I>): List<I>

    @Throws(SqlException::class)
    fun <M, I: Any, J: Any> selectTwoColumns(query: TwoColumnSelect<M, I, J>): List<Pair<I?, J?>>

    @Throws(SqlException::class)
    fun <M> selectMultipleColumns(query: MultiColumnSelect<M>): List<RowData<M>>

    @Throws(SqlException::class)
    fun <M> selectAllColumns(query: AllColumnSelect<M>): List<RowData<M>>

    @Throws(SqlException::class)
    fun <M> updateRows(data: Collection<RowData<M>>): Int

    @Throws(SqlException::class)
    fun <M> deleteFromTable(delete: SimpleDelete<M>): Int

    @Throws(SqlException::class)
    fun <M, J: Any> updateColumn(t: Table<M>, update: SingleColumnUpdate<M, J>): Int

    @Throws(SqlException::class)
    fun <M> insertRows(data: Collection<RowData<M>>, withId: Boolean): Int

    @Throws(SqlException::class)
    fun <M> insertRowsReturningIds(data: Collection<RowData<M>>, useDataIds: Boolean): List<EntityId>

    // TODO add insert/update template

    @Throws(SqlException::class)
    fun executeRawStatement(sql: String)

}