package com.machfour.macros.sql

import com.machfour.macros.sql.generator.*

abstract class SqlDatabaseImpl : SqlDatabase {
    // caller-managed connection, useful to reduce number of calls to DB
    // caller needs to call closeConnection() after. Use with begin and end transaction
    @Throws(SqlException::class)
    abstract override fun openConnection()

    @Throws(SqlException::class)
    abstract override fun closeConnection()

    // By default, database functions will autocommit.
    // These functions can be used to temporarily disable autocommit and are useful to group multiple operations together
    @Throws(SqlException::class)
    abstract override fun beginTransaction()

    @Throws(SqlException::class)
    abstract override fun endTransaction()

    @Throws(SqlException::class)
    abstract fun initDb(config: SqlConfig)

    @Throws(SqlException::class)
    abstract fun execRawSQLString(sql: String)

    @Throws(SqlException::class)
    abstract override fun <M, I: Any> selectColumn(query: SingleColumnSelect<M, I>): List<I?>

    @Throws(SqlException::class)
    override fun <M, I: Any> selectNonNullColumn(query: SingleColumnSelect<M, I>): List<I> {
        check(!query.selectColumn.isNullable) { "Select column is nullable: ${query.selectColumn}" }

        return selectColumn(query).mapNotNull { it }
    }

    @Throws(SqlException::class)
    abstract override fun <M, I: Any, J: Any> selectTwoColumns(query: TwoColumnSelect<M, I, J>): List<Pair<I?, J?>>

    @Throws(SqlException::class)
    abstract override fun <M> selectMultipleColumns(query: MultiColumnSelect<M>): List<RowData<M>>

    @Throws(SqlException::class)
    abstract override fun <M> selectAllColumns(query: AllColumnSelect<M>): List<RowData<M>>

    @Throws(SqlException::class)
    abstract override fun <M> insertRows(data: Collection<RowData<M>>, withId: Boolean): Int

    @Throws(SqlException::class)
    abstract override fun <M> updateRows(data: Collection<RowData<M>>): Int


    @Throws(SqlException::class)
    abstract override fun <M> deleteFromTable(delete: SimpleDelete<M>): Int

    @Throws(SqlException::class)
    override fun <M, J: Any> updateColumn(t: Table<M>, update: SingleColumnUpdate<M, J>): Int {
        TODO("Not yet implemented")
    }
}