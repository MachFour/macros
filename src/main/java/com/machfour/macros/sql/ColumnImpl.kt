package com.machfour.macros.sql

import com.machfour.macros.sql.datatype.MacrosType

import kotlin.properties.Delegates

internal open class ColumnImpl<M, J> private constructor(
    override val sqlName: String,
    override val type: MacrosType<J>,
    private val defaultValue: () -> J?,
    override val isUserEditable: Boolean,
    override val isNullable: Boolean,
    override val isInSecondaryKey: Boolean,
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

    internal class Fk<M, J, N> internal constructor(
        name: String, type: MacrosType<J>,
        defaultValue: () -> J?,
        editable: Boolean,
        nullable: Boolean,
        inSecondaryKey: Boolean,
        unique: Boolean,
        override val parentColumn: Column<N, J>,
        override val parentTable: Table<N>
    ): ColumnImpl<M, J>(name, type, defaultValue, editable, nullable, inSecondaryKey, unique), Column.Fk<M, J, N> {

        override fun toString(): String {
            return super.toString() + " (-> " + parentTable.name + "." + parentColumn.sqlName + ")"
        }
    }

    internal class Builder<J>(private val name: String, private val type: MacrosType<J>) {
        private var editable: Boolean = true
        private var nullable: Boolean = true
        private var inSecondaryKey: Boolean = false
        private var unique: Boolean = false
        private var defaultValue: () -> J? = { null }

        fun notEditable() = apply { editable = false }

        fun notNull() = apply { nullable = false }

        fun inSecondaryKey() = apply { inSecondaryKey = true }

        fun unique() = apply { unique = true }

        fun defaultsTo(value: J?) = apply { defaultValue = { value } }

        fun default(getValue: () -> J?) = apply { defaultValue = getValue }

        private fun <M> build(): ColumnImpl<M, J> {
            return ColumnImpl(name, type, defaultValue, editable, nullable, inSecondaryKey, unique)
        }

        private fun <M, N> buildFk(parent: Column<N, J>, parentTable: Table<N>): Fk<M, J, N> {
            // not sure why the constructor call needs type parameters here...
            return Fk(name, type, defaultValue, editable, nullable, inSecondaryKey, unique, parent, parentTable)
        }

        private fun <M> addToListAndSetIndex(newlyCreated: ColumnImpl<M, *>, columns: MutableList<Column<M, *>>) {
            newlyCreated.index = columns.size
            columns.add(newlyCreated)
        }

        // sets index
        fun <M> buildAndAdd(columnList: MutableList<Column<M, *>>): ColumnImpl<M, J> {
            val builtCol: ColumnImpl<M, J> = build()
            addToListAndSetIndex(builtCol, columnList)
            return builtCol
        }

        fun <M, N> buildAndAddFk(parent: Column<N, J>, parentTable: Table<N>, columnList: MutableList<Column<M, *>>): Fk<M, J, N> {
            val builtCol = buildFk<M, N>(parent, parentTable)
            addToListAndSetIndex(builtCol, columnList)
            return builtCol
        }
    }
}
