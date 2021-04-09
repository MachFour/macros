package com.machfour.macros.core

import com.machfour.macros.core.schema.SchemaHelpers

abstract class BaseTable<M>(
    final override val name: String,
    final override val factory: Factory<M>,
    cols: List<Column<M, *>>
) : Table<M> {
    final override val columns: List<Column<M, *>>

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
    final override val naturalKeyColumn: Column<M, *>?


    // first three columns must be ID, create time, modify time
    init {
        require(SchemaHelpers.ID_COLUMN_NAME == idColumn.sqlName)
        require(SchemaHelpers.CREATE_TIME_COLUMN_NAME == createTimeColumn.sqlName)
        require(SchemaHelpers.MODIFY_TIME_COLUMN_NAME == modifyTimeColumn.sqlName)

        // make name map and secondary key cols list. Linked hash map keeps insertion order
        val tmpColumnsByName = LinkedHashMap<String, Column<M, *>>(cols.size, 1.0f)
        var naturalKeyColumn: Column<M, *>? = null

        val tmpSecondaryKeyCols = ArrayList<Column<M, *>>(2)
        val tmpFkColumns = ArrayList<Column.Fk<M, *, *>>(2)

        for (c in cols) {
            tmpColumnsByName[c.sqlName] = c
            if (c.isInSecondaryKey) {
                tmpSecondaryKeyCols.add(c)
            }
            // record secondary key
            if (c.isUnique && c != idColumn) {
                require(naturalKeyColumn == null) { "two natural keys defined" }
                naturalKeyColumn = c
            }
            if (c is Column.Fk<*, *, *>) {
                tmpFkColumns.add(c as Column.Fk<M, *, *>)
            }
        }
        this.columns = cols // unmodifiable
        this.columnsByName = tmpColumnsByName // unmodifiable
        this.secondaryKeyCols = tmpSecondaryKeyCols // unmodifiable
        this.fkColumns = tmpFkColumns // unmodifiable
        this.naturalKeyColumn = naturalKeyColumn

        initTableCols()
    }

    // make separate method to avoid leaking 'this' in constructor
    private fun initTableCols() {
        // This package only uses ColumnImpl objects so we're good
        for (c in columns) {
            when (c) {
                is ColumnImpl<M, *> -> c.table = this
                else -> assert(false) { "Columns must be of ColumnImpl type" }
            }
        }
    }
}
