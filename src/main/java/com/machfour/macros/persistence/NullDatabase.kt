package com.machfour.macros.persistence

import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.generator.*

// Implementation of MacrosDataSource for testing which does nothing and returns empty data
class NullDatabase: SqlDatabase {
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

    override fun <M> selectMultipleColumns(query: MultiColumnSelect<M>): List<RowData<M>> {
        return emptyList()
    }

    override fun <M> selectAllColumns(query: AllColumnSelect<M>): List<RowData<M>> {
        return emptyList()
    }

    override fun <M> updateRows(data: Collection<RowData<M>>): Int {
        return 0
    }

    override fun <M> deleteFromTable(delete: SimpleDelete<M>): Int {
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