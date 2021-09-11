package com.machfour.macros.linux

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.datatype.TypeCastException
import com.machfour.macros.sql.rethrowAsSqlException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

@Throws(SQLException::class)
internal fun <M> bindData(p: PreparedStatement, values: RowData<M>, orderedColumns: List<Column<M, *>>) {
    bindData(p, values, orderedColumns, null)
}

@Throws(SQLException::class)
internal fun <M> bindData(p: PreparedStatement, values: RowData<M>, orderedColumns: List<Column<M, *>>, extra: Any?) {
    var colIndex = 1 // parameters are 1 indexed!
    for (col in orderedColumns) {
        // Internally, setObject() relies on a ladder of instanceof checks
        p.setObject(colIndex, values.getAsRaw(col))
        colIndex++
    }
    if (extra != null) {
        p.setObject(colIndex, extra)
    }
}

@Throws(SQLException::class)
internal fun <E> bindObjects(p: PreparedStatement, objects: Collection<E>) {
    var colIndex = 1 // parameters are 1 indexed!
    for (o in objects) {
        p.setObject(colIndex, o)
        colIndex++
    }
}

@Throws(SQLException::class)
internal fun <J> ResultSet.getColumn(column: Column<*, J>) : J? {
    val resultValue = getObject(column.sqlName)
    return try {
        column.type.fromRaw(resultValue)
    } catch (e: TypeCastException) {
        // this line throws an exception, so code won't reach here
        rethrowAsSqlException(resultValue, column)
        null
    }
}

@Throws(SQLException::class)
internal fun <M> fillRowData(data: RowData<M>, rs: ResultSet, columns: Collection<Column<M, *>> = data.table.columns) {
    for (col in columns) {
        val rawValue = rs.getObject(col.sqlName)
        try {
            data.putFromRaw(col, rawValue)
        } catch (e: TypeCastException) {
            rethrowAsSqlException(rawValue, col)
        }
    }
}

@Throws(SQLException::class)
internal fun <M> ResultSet.toRowData(table: Table<M>, columns: Collection<Column<M, *>> = table.columns) : RowData<M> {
    val data = RowData(table, columns)
    fillRowData(data, this, columns)
    return data
}

internal fun ResultSet.processResultSet(resultSetAction: (ResultSet) -> Unit) {
    this.use {
        it.next()
        while (!it.isAfterLast) {
            resultSetAction(it)
            it.next()
        }
    }
}

internal fun withDisabledAutoCommit(c: Connection, block: () -> Unit) {
    val prevAutoCommit = c.autoCommit
    c.autoCommit = false
    block()
    if (prevAutoCommit) {
        c.commit()
        c.autoCommit = true
    }
}