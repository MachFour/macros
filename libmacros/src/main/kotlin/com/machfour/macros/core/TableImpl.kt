package com.machfour.macros.core

import com.machfour.macros.schema.CREATE_TIME_COLUMN_NAME
import com.machfour.macros.schema.ID_COLUMN_NAME
import com.machfour.macros.schema.MODIFY_TIME_COLUMN_NAME
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.ColumnImpl
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table

abstract class TableImpl<M>(
    final override val sqlName: String,
    private val factory: Factory<M>,
    cols: List<Column<M, out Any>>
) : Table<M> {
    final override val columns: List<Column<M, out Any>> = cols

    // TODO make these better
    @Suppress("UNCHECKED_CAST")
    final override val idColumn: Column<M, Long> = cols[0] as Column<M, Long>
    @Suppress("UNCHECKED_CAST")
    final override val createTimeColumn: Column<M, Long> = cols[1] as Column<M, Long>
    @Suppress("UNCHECKED_CAST")
    final override val modifyTimeColumn: Column<M, Long> = cols[2] as Column<M, Long>

    final override val columnsByName: Map<String, Column<M, *>> = cols.associateBy { it.sqlName }

    // first three columns must be ID, create time, modify time
    init {
        require(ID_COLUMN_NAME == idColumn.sqlName)
        require(CREATE_TIME_COLUMN_NAME == createTimeColumn.sqlName)
        require(MODIFY_TIME_COLUMN_NAME == modifyTimeColumn.sqlName)

        initTableCols()
    }

    // make separate method to avoid leaking 'this' in constructor
    private fun initTableCols() {
        // This package only uses ColumnImpl objects so we're good
        for (c in columns) {
            require(c is ColumnImpl<M, *>) { "Columns must be of ColumnImpl type" }
            c.table = this
        }
    }

    override fun construct(data: RowData<M>, source: ObjectSource): M {
        return factory.construct(data, source)
    }
}
