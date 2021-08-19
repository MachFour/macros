package com.machfour.macros.sql

import com.machfour.macros.sql.generator.*
import java.io.IOException
import java.sql.SQLException

abstract class SqlDatabaseImpl : SqlDatabase {
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
    abstract fun initDb(config: SqlConfig)

    @Throws(SQLException::class)
    abstract fun execRawSQLString(sql: String)

    @Throws(SQLException::class)
    abstract override fun <M> deleteById(t: Table<M>, id: Long): Int

    @Throws(SQLException::class)
    abstract override fun <M, I> selectColumn(query: SingleColumnSelect<M, I>): List<I?>

    @Throws(SQLException::class)
    override fun <M, I> selectNonNullColumn(query: SingleColumnSelect<M, I>): List<I> {
        val (table, selected) = Pair(query.table, query.selectColumn)
        require(!selected.isNullable) { "column is nullable" }

        val allValues = selectColumn(query)
        return ArrayList<I>(allValues.size).apply {
            for (value in allValues) {
                checkNotNull(value) { "Found null value for column $selected in table $table"}
                add(value)
            }
        }
    }

    @Throws(SQLException::class)
    abstract override fun <M, I, J> selectTwoColumns(query: TwoColumnSelect<M, I, J>): List<Pair<I?, J?>>

    @Throws(SQLException::class)
    abstract override fun <M> selectMultipleColumns(t: Table<M>, query: MultiColumnSelect<M>): List<RowData<M>>

    @Throws(SQLException::class)
    abstract override fun <M> selectAllColumns(t: Table<M>, query: AllColumnSelect<M>): List<RowData<M>>

    // does DELETE FROM (t) WHERE (whereColumn) = (whereValue)
    // or DELETE FROM (t) WHERE (whereColumn) IN (whereValue1, whereValue2, ...)
    @Throws(SQLException::class)
    abstract override fun <M, J> deleteByColumn(t: Table<M>, whereColumn: Column<M, J>, whereValues: Collection<J>): Int

    @Throws(SQLException::class)
    abstract override fun <M, J> deleteByNullStatus(t: Table<M>, whereColumn: Column<M, J>, trueForNotNulls: Boolean): Int

    @Throws(SQLException::class)
    abstract override fun <M> insertRows(data: Collection<RowData<M>>, withId: Boolean): Int

    @Throws(SQLException::class)
    abstract override fun <M> updateRows(data: Collection<RowData<M>>): Int

    @Throws(SQLException::class)
    abstract override fun <M> clearTable(t: Table<M>): Int

    @Throws(SQLException::class)
    override fun <M> deleteFromTable(t: Table<M>, delete: SimpleDelete<M>): Int {
        TODO("Not yet implemented")
    }

    @Throws(SQLException::class)
    override fun <M, J> updateColumn(t: Table<M>, update: SingleColumnUpdate<M, J>): Int {
        TODO("Not yet implemented")
    }
}