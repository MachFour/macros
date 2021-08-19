package com.machfour.macros.persistence

import com.machfour.macros.sql.Column
import com.machfour.macros.orm.ColumnData
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.generator.AllColumnSelect
import com.machfour.macros.sql.generator.MultiColumnSelect
import com.machfour.macros.sql.generator.SingleColumnSelect
import com.machfour.macros.sql.generator.TwoColumnSelect

// Implementation of MacrosDataSource for testing which does nothing and returns empty data
class NullDatabase: MacrosDatabase {
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

    override fun <M> selectMultipleColumns(t: Table<M>, query: MultiColumnSelect<M>): List<ColumnData<M>> {
        return emptyList()
    }

    override fun <M> selectAllColumns(t: Table<M>, query: AllColumnSelect<M>): List<ColumnData<M>> {
        return emptyList()
    }

    override fun <M : MacrosEntity<M>> updateObjects(objects: Collection<M>): Int {
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

    override fun <M : MacrosEntity<M>> insertObjectData(objectData: List<ColumnData<M>>, withId: Boolean): Int {
        return 0
    }

    override fun executeRawStatement(sql: String) {
    }
}