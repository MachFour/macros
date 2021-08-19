package com.machfour.macros.sql

import com.machfour.macros.sql.datatype.MacrosType
import com.machfour.macros.sql.generator.ColumnExpr

/**
 * Created by max on 4/11/17.
 */
interface Column<M, J> : ColumnExpr<M, J> {
    /*
     * Describes one column referencing another. N is the parent table type
     */
    interface Fk<M, J, N> : Column<M, J> {
        val parentColumn: Column<N, J>
        val parentTable: Table<N>
    }

    val sqlName: String
    val defaultData: J?

    // overriden ColumnExpr fields
    override val type: MacrosType<J>
    override val table: Table<M>
    override val sql: String
        get() = sqlName

    // unique index of column, giving its order.
    val index: Int

    // whether the column should be shown to and editable by users
    val isUserEditable: Boolean

    // whether the column is allowed to be saved into the DB as null
    val isNullable: Boolean

    // whether the column can be used as part of an alternative key to identify a row
    // NOTE there can also be other columns in the table needed to form the full secondary key.
    // Also, not all tables may have a secondary key.
    val isInSecondaryKey: Boolean

    // is SQL UNIQUE column
    val isUnique: Boolean
}