package com.machfour.macros.core

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table
import com.machfour.macros.validation.SchemaViolation
import com.machfour.macros.validation.tableValidator

// ensures that the presence of the ID is consistent with the semantics of the objectSource
// see ObjectSource class for more documentation
private fun checkObjectSource(source: ObjectSource, hasId: Boolean) {
    when (source) {
        ObjectSource.IMPORT,
        ObjectSource.USER_NEW,
        ObjectSource.COMPUTED -> {
            check(!hasId) { "Object should not have an ID" }
        }
        ObjectSource.DB_EDIT,
        ObjectSource.RESTORE,
        ObjectSource.DATABASE,
        ObjectSource.INBUILT -> {
            check(hasId) { "Object should have an ID" }
        }
        ObjectSource.TEST -> {}
    }
}


/**
 * parent class for all Macros persistable objects
 */
abstract class MacrosEntityImpl<M : MacrosEntity<M>> protected constructor(
    final override val data: RowData<M>,
    final override val source: ObjectSource
) : MacrosEntity<M>, FkEntity<M> {

    abstract override val table: Table<M>
    abstract override val factory: Factory<M>

    init {
        require(data.isImmutable) { "MacrosEntity must be constructed with immutable RowData" }

        tableValidator(data.table)
            .validateData(data)
            .let { if (it.isNotEmpty()) { throw SchemaViolation(it) } }

        checkObjectSource(source, hasId)
    }
    override fun toString(): String {
        return "${table.name} id=${id}, objSrc=${source}, data=${data}"
    }

    final override val id: EntityId
        get() = data[table.idColumn]!!

    final override val createTime: Instant
        get() = data[table.createTimeColumn]!!

    final override val modifyTime: Instant
        get() = data[table.modifyTimeColumn]!!

    final override val hasId: Boolean
        get() = super<FkEntity>.hasId

    override fun hashCode() = data.hashCode()

    override fun equals(other: Any?): Boolean {
        return (other is MacrosEntityImpl<*> && this.data == other.data) //&& isFromDb == ((MacrosEntity) o).isFromDb
    }

    fun equalsWithoutMetadata(o: MacrosEntity<M>): Boolean {
        val columnsToCheck: MutableList<Column<M, *>> = ArrayList(table.columns)
        columnsToCheck.remove(table.idColumn)
        columnsToCheck.remove(table.createTimeColumn)
        columnsToCheck.remove(table.modifyTimeColumn)
        return RowData.columnsAreEqual(this.data, o.data, columnsToCheck)
    }

    final override fun <J: Any> getData(col: Column<M, J>): J? {
        return data[col]
    }

    override fun hasData(col: Column<M, *>): Boolean {
        return data.hasValue(col)
    }

    override fun dataCopy(withMetadata: Boolean): RowData<M> {
        val copy = data.copy()
        return when (withMetadata) {
            true -> copy
            false -> copy.apply {
                //copy.setDefaultData(listOf(table.idColumn, table.createTimeColumn, table.modifyTimeColumn))
                put(table.idColumn, MacrosEntity.NO_ID)
                put(table.createTimeColumn, 0L)
                put(table.modifyTimeColumn, 0L)
            }
        }
    }

    // FkEntity methods


    // TODO only really need to map to the Natural key value,
    // but there's no convenient way of enforcing the type relationship except by wrapping it in a RowData
    private val fkParentKeyDataM = HashMap<Column.Fk<M, *, *>, RowData<*>>()

    override val fkParentKeyData: Map<Column.Fk<M, *, *>, RowData<*>>
        get() = fkParentKeyDataM

    // NOTE FOR FUTURE REFERENCE: wildcard capture helpers only work if NO OTHER ARGUMENT
    // shares the same parameter as the wildcard being captured.
    override fun <N : MacrosEntity<N>, J: Any> setFkParentKey(fkCol: Column.Fk<M, *, N>, parentKeyCol: Column<N, J>, parent: N) {
        val parentNaturalKeyData : J = parent.getData(parentKeyCol)
                ?: throw RuntimeException("Parent natural key data was null")
        setFkParentKey(fkCol, parentKeyCol, parentNaturalKeyData)
    }

    // ... or when only the relevant column data is available, but then it's only limited to single-column secondary keys
    override fun <N, J: Any> setFkParentKey(fkCol: Column.Fk<M, *, N>, parentKeyCol: Column<N, J>, data: J) {
        require(parentKeyCol.isUnique)
        val parentSecondaryKey = RowData(fkCol.parentTable, listOf(parentKeyCol))
            .apply { put(parentKeyCol, data) }
        fkParentKeyDataM[fkCol] = parentSecondaryKey
    }

    override fun <N> getFkParentKey(fkCol: Column.Fk<M, *, N>): RowData<N> {
        // Generics ensure that the table types match
        @Suppress("UNCHECKED_CAST")
        return requireNotNull(fkParentKeyData[fkCol] as RowData<N>) {
            "FK parent key missing for $fkCol"
        }
    }
    
    // this also works for import (without IDs) because both columns are NO_ID
    protected fun <M : MacrosEntity<M>, J: Any, N : MacrosEntity<N>> foreignKeyMatches(
        childObj: MacrosEntity<M>,
        childCol: Column.Fk<M, J, N>,
        parentObj: MacrosEntity<N>
    ): Boolean {
        val parentCol = childCol.parentColumn
        val parentIdCol = parentObj.table.idColumn
        return childObj.getData(childCol) == parentObj.getData(parentCol) ||
                // disable ID check for TEST objects
                (parentCol == parentIdCol && parentObj.source == ObjectSource.TEST)
    }

}