package com.machfour.macros.core

import com.machfour.macros.validation.SchemaViolation
import com.machfour.macros.validation.ValidationError
import java.time.Instant
import java.util.Collections;

/**
 * parent class for all Macros persistable objects
 */
abstract class MacrosEntityImpl<M : MacrosEntity<M>> protected constructor(
        final override val data: ColumnData<M>,
        final override val objectSource: ObjectSource
) : MacrosEntity<M> {


    abstract override val table: Table<M>
    abstract override val factory: Factory<M>

    // whether this object was created from a database instance or whether it was created by the
    // application (e.g. by a 'new object' action initiated by the user)
    val createInstant: Instant
    val modifyInstant: Instant

    // TODO only really need to map to the Natural key value,
    // but there's no convenient way of enforcing the type relationship except by wrapping it in a ColumnData
    private val mutableFkForeignKeyMap: MutableMap<Column.Fk<M, *, *>, ColumnData<*>> = HashMap()

    override val fkNaturalKeyMap: Map<Column.Fk<M, *, *>, ColumnData<*>>
        get() = Collections.unmodifiableMap(mutableFkForeignKeyMap)


    // NOTE data passed in is made Immutable as a side effect
    init {
        val errors: Map<Column<M, *>, List<ValidationError>> = MacrosBuilder.validate(data)
        if (errors.isNotEmpty()) {
            throw SchemaViolation(errors)
        }
        //this.dataMap = new ColumnData<>(data);
        this.data.setImmutable()
        createInstant = Instant.ofEpochSecond(data[data.table.createTimeColumn]!!)
        modifyInstant = Instant.ofEpochSecond(data[data.table.modifyTimeColumn]!!)
        checkObjectSource()
    }

    override fun copyFkNaturalKeyMap(from: MacrosEntity<M>) {
        for (fkCol in from.fkNaturalKeyMap.keys) {
            mutableFkForeignKeyMap[fkCol] = from.getFkParentNaturalKey(fkCol)
        }
    }

    // ensures that the presence of the ID is consistent with the semantics of the objectSource
    // see ObjectSource class for more documentation
    private fun checkObjectSource() {
        when (objectSource) {
            ObjectSource.IMPORT, ObjectSource.USER_NEW, ObjectSource.COMPUTED -> {
                assert(!hasId) { "Object should not have an ID" }
            }
            ObjectSource.DB_EDIT, ObjectSource.RESTORE, ObjectSource.DATABASE, ObjectSource.INBUILT -> {
                assert(hasId) { "Object should have an ID" }
            }
        }
    }

    final override val id: Long
        get() = getData(table.idColumn)!!

    final override val createTime: Long
        get() = getData(table.createTimeColumn)!!

    final override val modifyTime: Long
        get() = getData(table.modifyTimeColumn)!!

    override fun hashCode(): Int {
        return data.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return (other is MacrosEntityImpl<*> && this.data == other.data) //&& isFromDb == ((MacrosEntity) o).isFromDb
    }

    fun equalsWithoutMetadata(o: MacrosEntity<M>): Boolean {
        val columnsToCheck: MutableList<Column<M, *>> = ArrayList(table.columns)
        columnsToCheck.remove(table.idColumn)
        columnsToCheck.remove(table.createTimeColumn)
        columnsToCheck.remove(table.modifyTimeColumn)
        return ColumnData.columnsAreEqual(this.data, o.data, columnsToCheck)
    }

    override fun <J> getData(col: Column<M, J>): J? {
        val value = data[col]
        assert(col.isNullable || value != null) { "null data retrieved from not-nullable column" }
        return value
    }

    override fun hasData(col: Column<M, *>): Boolean {
        return data.hasData(col)
    }

    override val dataFullCopy: ColumnData<M>
        get() = data.copy()

    // returns immutable copy of data map
    override val dataCopy: ColumnData<M>
        get() = dataFullCopy.apply {
                // have to remove ID since it's now a computed value
                //copy.setDefaultData(listOf(table.idColumn, table.createTimeColumn, table.modifyTimeColumn))
                put(table.idColumn, MacrosEntity.NO_ID)
                put(table.createTimeColumn, 0L)
                put(table.modifyTimeColumn, 0L)
            }



    // NOTE FOR FUTURE REFERENCE: wildcard capture helpers only work if NO OTHER ARGUMENT
    // shares the same parameter as the wildcard being captured.
    override fun <N : MacrosEntity<N>, J> setFkParentNaturalKey(fkCol: Column.Fk<M, *, N>, parentNaturalKey: Column<N, J>, parent: N) {
        val parentNaturalKeyData : J = parent.getData(parentNaturalKey)
                ?: throw RuntimeException("Parent natural key data was null")
        setFkParentNaturalKey(fkCol, parentNaturalKey, parentNaturalKeyData)
    }

    // ... or when only the relevant column data is available, but then it's only limited to single-column secondary keys
    override fun <N, J> setFkParentNaturalKey(fkCol: Column.Fk<M, *, N>, parentNaturalKey: Column<N, J>, data: J) {
        assert(parentNaturalKey.isUnique)
        val parentKeyColAsList: List<Column<N, *>> = listOf(parentNaturalKey)
        val parentSecondaryKey = ColumnData(fkCol.parentTable, parentKeyColAsList)
        parentSecondaryKey.put(parentNaturalKey, data)
        mutableFkForeignKeyMap[fkCol] = parentSecondaryKey
    }

    @Suppress("UNCHECKED_CAST")
    override fun <N> getFkParentNaturalKey(fkCol: Column.Fk<M, *, N>): ColumnData<N> {
        // Generics ensure that the table types match
        assert(fkNaturalKeyMap.containsKey(fkCol)) { "No FK parent data for column: $fkCol" }
        return fkNaturalKeyMap[fkCol] as ColumnData<N>
    }

    override fun toString(): String {
        return "${table.name} id=${id}, objSrc=${objectSource}, data=${data}"
    }

    // this also works for import (without IDs) because both columns are NO_ID
    protected fun <M : MacrosEntity<M>, J, N : MacrosEntity<N>> foreignKeyMatches(
            childObj: MacrosEntityImpl<M>, childCol: Column.Fk<M, J, N>, parentObj: MacrosEntityImpl<N>): Boolean {
        val childData = childObj.getData(childCol)
        val parentData = parentObj.getData(childCol.parentColumn)
        return childData == parentData
    }
}