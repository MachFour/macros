package com.machfour.macros.persistence

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.generator.*

class DummyDatabase: SqlDatabase {
    override fun openConnection() {}

    override fun closeConnection() {}

    override fun beginTransaction() {}

    override fun endTransaction() {}

    override fun <M, I> selectColumn(query: SingleColumnSelect<M, I>): List<I?> {
        return emptyList()
    }

    override fun <M, I> selectNonNullColumn(query: SingleColumnSelect<M, I>): List<I> {
        return emptyList()
    }

    override fun <M, I, J> selectTwoColumns(query: TwoColumnSelect<M, I, J>): List<Pair<I?, J?>> {
        return emptyList()
    }

    override fun <M> selectMultipleColumns(t: Table<M>, query: MultiColumnSelect<M>): List<RowData<M>> {
        return makeDummyRowData(t, query)
    }

    override fun <M> selectAllColumns(t: Table<M>, query: AllColumnSelect<M>): List<RowData<M>> {
        return makeDummyRowData(t, query)
    }

    private fun <M> makeDummyRowData(t: Table<M>, query: SelectQuery<M>): List<RowData<M>> {
        return query.getBindArguments().map {
            RowData(t).apply {
                put(t.idColumn, it.hashCode().toLong())
                put(t.createTimeColumn, System.currentTimeMillis()/1000)
                put(t.modifyTimeColumn, System.currentTimeMillis()/1000)
            }
        }
    }

    override fun <M> updateRows(data: Collection<RowData<M>>): Int {
        return 0
    }

    override fun <M, J> deleteByColumn(t: Table<M>, whereColumn: Column<M, J>, whereValues: Collection<J>): Int {
        return 0
    }

    override fun <M, J> deleteByNullStatus(t: Table<M>, whereColumn: Column<M, J>, trueForNotNulls: Boolean): Int {
        return 0
    }

    override fun <M> deleteById(t: Table<M>, id: Long): Int {
        return 0
    }

    override fun <M> clearTable(t: Table<M>): Int {
        return 0
    }

    override fun <M> deleteFromTable(t: Table<M>, delete: SimpleDelete<M>): Int {
        return 0
    }

    override fun <M, J> updateColumn(t: Table<M>, update: SingleColumnUpdate<M, J>): Int {
        return 0
    }

    override fun <M> insertRows(data: Collection<RowData<M>>, withId: Boolean): Int {
        return 0
    }

    override fun executeRawStatement(sql: String) {
    }
}
