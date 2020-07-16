package com.machfour.macros.linux

import com.machfour.macros.core.Column
import com.machfour.macros.core.ColumnData
import java.sql.PreparedStatement
import java.sql.SQLException

internal object LinuxDatabaseUtils {
    @JvmStatic
    @Throws(SQLException::class)
    fun <M> bindData(p: PreparedStatement, values: ColumnData<M>, orderedColumns: List<Column<M, *>>) {
        bindData(p, values, orderedColumns, null)
    }

    @JvmStatic
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

    @JvmStatic
    @Throws(SQLException::class)
    fun <E> bindObjects(p: PreparedStatement, objects: Collection<E>) {
        var colIndex = 1 // parameters are 1 indexed!
        for (o in objects) {
            p.setObject(colIndex, o)
            colIndex++
        }
    }
}