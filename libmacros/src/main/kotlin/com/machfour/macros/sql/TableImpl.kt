package com.machfour.macros.sql

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.schema.CREATE_TIME_COLUMN_NAME
import com.machfour.macros.schema.ID_COLUMN_NAME
import com.machfour.macros.schema.MODIFY_TIME_COLUMN_NAME
import com.machfour.macros.sql.entities.Factory

abstract class TableImpl<I: MacrosEntity, M: I>(
    final override val sqlName: String,
    private val factory: Factory<I, M>,
    cols: List<Column<M, out Any>>
) : Table<I, M>, Factory<I, M> by factory {
    final override val columns: List<Column<M, out Any>> = cols

    // TODO make these better
    @Suppress("UNCHECKED_CAST")
    final override val idColumn: Column<M, EntityId> = cols[0] as Column<M, EntityId>
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

}