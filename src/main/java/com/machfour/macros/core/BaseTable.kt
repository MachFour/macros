package com.machfour.macros.core

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

internal abstract class BaseTable<M>(
    final override val name: String,
    final override val factory: Factory<M>,
    cols: List<Column<M, *>>
) : Table<M> {
    final override val columns: List<Column<M, *>> = Collections.unmodifiableList(cols)

    // TODO make these better
    final override val idColumn: Column<M, Long> = cols[0] as Column<M, Long>
    final override val createTimeColumn: Column<M, Long> = cols[1] as Column<M, Long>
    final override val modifyTimeColumn: Column<M, Long> = cols[2] as Column<M, Long>

    final override val fkColumns: List<Column.Fk<M, *, *>>
    final override val naturalKeyColumn: Column<M, *>?
    final override val columnsByName: Map<String, Column<M, *>>
    final override val secondaryKeyCols: List<Column<M, *>>


    override fun getColumnForName(name: String): Column<M, *>? = columnsByName.getOrDefault(name, null)


    // first three columns must be ID, create time, modify time
    init {
        require(Schema.ID_COLUMN_NAME == idColumn.sqlName)
        require(Schema.CREATE_TIME_COLUMN_NAME == createTimeColumn.sqlName)
        require(Schema.MODIFY_TIME_COLUMN_NAME == modifyTimeColumn.sqlName)

        // make name map and secondary key cols list. Linked hash map keeps insertion order
        columnsByName = LinkedHashMap(cols.size, 1.0f)
        secondaryKeyCols = ArrayList(2)
        fkColumns = ArrayList(2)

        var naturalKeyColumn: Column<M, *>? = null

        for (c in cols) {
            // This package only uses ColumnImpl objects so we're good
            require(c is ColumnImpl<*, *>)
            (c as ColumnImpl<M, *>).setTable(this)

            columnsByName[c.sqlName] = c
            if (c.isInSecondaryKey) {
                secondaryKeyCols.add(c)
            }
            // record secondary key
            if (c.isUnique && c != idColumn) {
                require(naturalKeyColumn == null) { "two natural keys defined" }
                naturalKeyColumn = c
            }
            if (c is Column.Fk<*, *, *>) {
                fkColumns.add(c as Column.Fk<M, *, *>)
            }
        }
        this.naturalKeyColumn = naturalKeyColumn
    }


}