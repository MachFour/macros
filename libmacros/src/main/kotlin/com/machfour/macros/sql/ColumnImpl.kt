package com.machfour.macros.sql

import com.machfour.macros.sql.datatype.SqlType

import kotlin.properties.Delegates

internal open class ColumnImpl<M, J: Any> private constructor(
    override val sqlName: String,
    override val type: SqlType<J>,
    private val defaultValue: () -> J?,
    override val isUserEditable: Boolean,
    override val isNullable: Boolean,
    override val isUnique: Boolean
) : Column<M, J> {

    // These are set later, when added to Table
    override lateinit var table: Table<M>
    // can't use lateinit on primitive types
    override var index by Delegates.notNull<Int>()

    override val defaultData: J?
        get() = defaultValue()

    override fun toString(): String {
        return sqlName
    }

    internal class Fk<M, J: Any, N> internal constructor(
        name: String, type: SqlType<J>,
        defaultValue: () -> J?,
        editable: Boolean,
        nullable: Boolean,
        unique: Boolean,
        override val parentColumn: Column<N, J>,
    ): ColumnImpl<M, J>(name, type, defaultValue, editable, nullable, unique), Column.Fk<M, J, N> {

        override fun toString(): String {
            return super.toString() + " (-> " + parentColumn.table.sqlName + "." + parentColumn.sqlName + ")"
        }
    }

    internal class Builder<J: Any>(private val name: String, private val type: SqlType<J>): Column.Builder<J> {
        private var editable: Boolean = true
        private var nullable: Boolean = true
        private var unique: Boolean = false
        private var defaultValue: () -> J? = { null }

        override fun notEditable() = apply { editable = false }

        override fun notNull() = apply { nullable = false }

        override fun unique() = apply { unique = true }

        override fun default(getValue: () -> J?) = apply { defaultValue = getValue }

        private fun <M> build(): ColumnImpl<M, J> {
            return ColumnImpl(name, type, defaultValue, editable, nullable, unique)
        }

        private fun <M, N> buildFk(parent: Column<N, J>): Fk<M, J, N> {
            return Fk(name, type, defaultValue, editable, nullable, unique, parent)
        }

        private fun <M> addToListAndSetIndex(newlyCreated: ColumnImpl<M, J>, columns: MutableList<Column<M, out Any>>) {
            newlyCreated.index = columns.size
            columns.add(newlyCreated)
        }

        // sets index
        override fun <M> buildFor(tableColumns: MutableList<Column<M, out Any>>): ColumnImpl<M, J> {
            val builtCol: ColumnImpl<M, J> = build()
            return builtCol.also {
                addToListAndSetIndex(it, tableColumns)
            }
        }

        override fun <M, N> buildFkFor(
            parentCol: Column<N, J>,
            tableColumns: MutableList<Column<M, out Any>>
        ): Fk<M, J, N> {
            return buildFk<M, N>(parentCol).also {
                addToListAndSetIndex(it, tableColumns)
            }
        }
    }
}
