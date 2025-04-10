package com.machfour.macros.sql

import com.machfour.macros.sql.datatype.SqlType
import com.machfour.macros.sql.generator.ColumnExpr

/**
 * Created by max on 4/11/17.
 */
interface Column<M, J: Any> : ColumnExpr<M, J> {
    // Describes one column referencing another. N is the parent table type
    interface Fk<M, J: Any, N> : Column<M, J> {
        val parentColumn: Column<N, J>
    }

    val sqlName: String
    val defaultData: J?

    // overridden ColumnExpr fields
    override val type: SqlType<J>
    override val table: Table<M>
    override val sql: String
        get() = sqlName

    // unique index of column, giving its order.
    val index: Int

    // whether the column should be shown to and editable by users
    val isUserEditable: Boolean

    // whether the column is allowed to be saved into the DB as null
    val isNullable: Boolean

    // is SQL UNIQUE column
    val isUnique: Boolean

    interface Builder<J: Any> {
        fun notEditable(): Builder<J>

        fun notNull(): Builder<J>

        fun unique(): Builder<J>

        fun default(getValue: () -> J?): Builder<J>

        fun <M> buildFor(tableColumns: MutableList<Column<M, out Any>>): Column<M, J>

        fun <M, N> buildFkFor(
            parentCol: Column<N, J>,
            tableColumns: MutableList<Column<M, out Any>>
        ): Fk<M, J, N>
    }
}