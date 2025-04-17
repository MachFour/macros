package com.machfour.macros.core

import com.machfour.macros.core.MacrosEntity.Companion.NO_ID
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.rowdata.RowData
import com.machfour.macros.validation.SchemaViolation
import com.machfour.macros.validation.validateNonNull

// ensures that the presence of the ID is consistent with the semantics of the objectSource
// see ObjectSource class for more documentation
private fun checkIdPresence(source: ObjectSource, hasId: Boolean): Boolean {
    when (source) {
        ObjectSource.COMPUTED,
        ObjectSource.IMPORT,
        ObjectSource.JSON,
        ObjectSource.USER_NEW, -> {
            return !hasId // object should not have an ID
        }
        ObjectSource.DATABASE,
        ObjectSource.DB_EDIT,
        ObjectSource.INBUILT,
        ObjectSource.RESTORE, -> {
            return hasId // object should have an ID
        }
        ObjectSource.TEST -> {
            return true // never invalid
        }
    }
}


/**
 * parent class for all Macros persistable objects
 */
abstract class MacrosEntityImpl<M : MacrosSqlEntity<M>> protected constructor(
    final override val data: RowData<M>,
    final override val source: ObjectSource
) : MacrosSqlEntity<M> {

    abstract override val table: Table<M>
    abstract val factory: Factory<M>

    init {
        require(data.isImmutable) { "MacrosEntity must be constructed with immutable RowData" }

        validateNonNull(data).takeIf { it.isNotEmpty() }?.let {
            throw SchemaViolation(it)
        }

        require(checkIdPresence(source, hasId)) {
            if (hasId) { "Object should not have ID:\n" }
            else { "Object should have ID:\n" } + data

        }
    }
    override fun toString(): String {
        return "${table.sqlName} id=${id}, objSrc=${source}, data=${data}"
    }

    final override val id: EntityId
        get() = data[table.idColumn]!!

    final override val createTime: Instant
        get() = data[table.createTimeColumn]!!

    final override val modifyTime: Instant
        get() = data[table.modifyTimeColumn]!!

    final override val hasId: Boolean
        get() = (id != NO_ID)

    override fun hashCode() = data.hashCode()

    override fun equals(other: Any?): Boolean {
        return (other is MacrosEntityImpl<*> && this.data == other.data) //&& isFromDb == ((MacrosEntity) o).isFromDb
    }

    fun equalsWithoutMetadata(o: MacrosSqlEntity<M>): Boolean {
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

    override fun toRowData(): RowData<M> {
        return data.copy()
    }

    // this also works for import (without IDs) because both columns are NO_ID
    protected fun <M : MacrosSqlEntity<M>, J: Any, N : MacrosSqlEntity<N>> foreignKeyMatches(
        childObj: MacrosSqlEntity<M>,
        childCol: Column.Fk<M, J, N>,
        parentObj: MacrosSqlEntity<N>
    ): Boolean {
        val parentCol = childCol.parentColumn
        if (parentCol == parentObj.table.idColumn && parentObj.source == ObjectSource.TEST) {
            // disable ID check for TEST objects
            return true
        }
        return childObj.getData(childCol) == parentObj.getData(parentCol)
    }

}