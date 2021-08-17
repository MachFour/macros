package com.machfour.macros.storage

import com.machfour.macros.orm.Column
import com.machfour.macros.orm.ColumnData
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.orm.Table
import com.machfour.macros.persistence.MacrosDatabase
import com.machfour.macros.sql.*

class DummyDatabase: MacrosDatabase {
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
        return makeDummyColumnData(t, query)
    }

    override fun <M> selectAllColumns(t: Table<M>, query: AllColumnSelect<M>): List<ColumnData<M>> {
        return makeDummyColumnData(t, query)
    }

    private fun <M> makeDummyColumnData(t: Table<M>, query: SelectQuery<M>): List<ColumnData<M>> {
        return query.whereExpression.getBindObjects().map {
            ColumnData(t).apply {
                put(t.idColumn, it.hashCode().toLong())
                put(t.createTimeColumn, System.currentTimeMillis()/1000)
                put(t.modifyTimeColumn, System.currentTimeMillis()/1000)
            }
        }
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

    override fun executeRawStatement(sql: String): Int {
        return 0
    }
}
