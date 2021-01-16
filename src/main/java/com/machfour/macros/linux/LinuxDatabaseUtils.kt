package com.machfour.macros.linux

import com.machfour.macros.core.Column
import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.Table
import com.machfour.macros.core.datatype.TypeCastException
import com.machfour.macros.persistence.DatabaseUtils
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

internal object LinuxDatabaseUtils {
    @Throws(SQLException::class)
    fun <M> bindData(p: PreparedStatement, values: ColumnData<M>, orderedColumns: List<Column<M, *>>) {
        bindData(p, values, orderedColumns, null)
    }

    @Throws(SQLException::class)
    fun <M> bindData(p: PreparedStatement, values: ColumnData<M>, orderedColumns: List<Column<M, *>>, extra: Any?) {
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
    fun <E> bindObjects(p: PreparedStatement, objects: Collection<E>) {
        var colIndex = 1 // parameters are 1 indexed!
        for (o in objects) {
            p.setObject(colIndex, o)
            colIndex++
        }
    }

    @Throws(SQLException::class)
    fun <J> ResultSet.getColumn(column: Column<*, J>) : J? {
        val resultValue = getObject(column.sqlName)
        return try {
            column.type.fromRaw(resultValue)
        } catch (e: TypeCastException) {
            // this line throws an exception, so code won't reach here
            DatabaseUtils.rethrowAsSqlException(resultValue, column)
            null
        }
    }

    @Throws(SQLException::class)
    fun <M> fillColumnData(data: ColumnData<M>, rs: ResultSet, columns: Collection<Column<M, *>> = data.table.columns) {
        for (col in columns) {
            val rawValue = rs.getObject(col.sqlName)
            try {
                data.putFromRaw(col, rawValue)
            } catch (e: TypeCastException) {
                DatabaseUtils.rethrowAsSqlException(rawValue, col)
            }
        }
    }

    @Throws(SQLException::class)
    fun <M> ResultSet.toColumnData(table: Table<M>, columns: Collection<Column<M, *>> = table.columns) : ColumnData<M> {
        val data = ColumnData(table, columns)
        fillColumnData(data, this, columns)
        return data
    }

}