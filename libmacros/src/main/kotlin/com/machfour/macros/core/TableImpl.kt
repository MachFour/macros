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
    final override val columns: List<Column<M, out Any>>

    // TODO make these better
    @Suppress("UNCHECKED_CAST")
    final override val idColumn: Column<M, Long> = cols[0] as Column<M, Long>
    @Suppress("UNCHECKED_CAST")
    final override val createTimeColumn: Column<M, Long> = cols[1] as Column<M, Long>
    @Suppress("UNCHECKED_CAST")
    final override val modifyTimeColumn: Column<M, Long> = cols[2] as Column<M, Long>

    final override val fkColumns: List<Column.Fk<M, *, *>>
    final override val columnsByName: Map<String, Column<M, *>>
    final override val secondaryKeyCols: List<Column<M, *>>

    // first three columns must be ID, create time, modify time
    init {
        require(ID_COLUMN_NAME == idColumn.sqlName)
        require(CREATE_TIME_COLUMN_NAME == createTimeColumn.sqlName)
        require(MODIFY_TIME_COLUMN_NAME == modifyTimeColumn.sqlName)

        // make name map and secondary key cols list. Linked hash map keeps insertion order
        val tmpColumnsByName = LinkedHashMap<String, Column<M, *>>(cols.size, 1.0f)

        val tmpSecondaryKeyCols = ArrayList<Column<M, *>>(2)
        val tmpFkColumns = ArrayList<Column.Fk<M, *, *>>(2)

        for (c in cols) {
            tmpColumnsByName[c.sqlName] = c
            if (c.isInSecondaryKey) {
                tmpSecondaryKeyCols.add(c)
            }
            
            if (c is Column.Fk<*, *, *>) {
                tmpFkColumns.add(c as Column.Fk<M, *, *>)
            }
        }
        this.columns = cols // unmodifiable
        this.columnsByName = tmpColumnsByName // unmodifiable
        this.secondaryKeyCols = tmpSecondaryKeyCols // unmodifiable
        this.fkColumns = tmpFkColumns // unmodifiable

        initTableCols()
    }

    // make separate method to avoid leaking 'this' in constructor
    private fun initTableCols() {
        // This package only uses ColumnImpl objects so we're good
        for (c in columns) {
            when (c) {
                is ColumnImpl<M, *> -> c.table = this
                else -> check(false) { "Columns must be of ColumnImpl type" }
            }
        }
    }

    override fun construct(data: RowData<M>, source: ObjectSource): M {
        return factory.construct(data, source)
    }
}
