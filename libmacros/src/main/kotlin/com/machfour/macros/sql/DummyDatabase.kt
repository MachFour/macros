package com.machfour.macros.sql

import com.machfour.macros.core.EntityId
import com.machfour.macros.sql.generator.*

private class DummyDatabase: SqlDatabase {
    override fun openConnection(getGeneratedKeys: Boolean) {}

    override fun closeConnection() {}

    override fun beginTransaction() {}

    override fun endTransaction() {}

    override fun <M, I: Any> selectColumn(query: SingleColumnSelect<M, I>): List<I?> {
        return emptyList()
    }

    override fun <M, I: Any> selectNonNullColumn(query: SingleColumnSelect<M, I>): List<I> {
        return emptyList()
    }

    override fun <M, I: Any, J: Any> selectTwoColumns(query: TwoColumnSelect<M, I, J>): List<Pair<I?, J?>> {
        return emptyList()
    }

    override fun <M> selectMultipleColumns(query: MultiColumnSelect<M>): List<RowData<M>> {
        return emptyList()
    }

    override fun <M> selectAllColumns(query: AllColumnSelect<M>): List<RowData<M>> {
        return emptyList()
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

    override fun <M> deleteFromTable(delete: SimpleDelete<M>): Int {
        return 0
    }

    override fun <M, J: Any> updateColumn(t: Table<M>, update: SingleColumnUpdate<M, J>): Int {
        return 0
    }

    override fun <M> insertRows(data: Collection<RowData<M>>, withId: Boolean): Int {
        return 0
    }

    override fun <M> insertRowsReturningIds(data: Collection<RowData<M>>, useDataIds: Boolean): List<EntityId> {
        return emptyList()
    }

    override fun executeRawStatement(sql: String) {
    }
}
