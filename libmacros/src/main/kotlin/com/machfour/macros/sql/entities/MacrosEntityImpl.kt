package com.machfour.macros.sql.entities

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.Instant
import com.machfour.macros.core.MacrosEntity.Companion.NO_ID
import com.machfour.macros.core.ObjectSource
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
abstract class MacrosEntityImpl<M: MacrosSqlEntity<M>> protected constructor(
    final override val data: RowData<M>,
    final override val source: ObjectSource
) : MacrosSqlEntity<M> {

    protected abstract fun getTable(): Table<*, M>

    init {
        require(data.isImmutable) { "MacrosEntity must be constructed with immutable RowData" }

        validateNonNull(data).also {
            if (it.isNotEmpty()) {
                throw SchemaViolation(it)
            }
        }

        require(checkIdPresence(source, hasId)) {
            "RowData should ${(if (hasId) "not " else "")}have ID:\n$data"
        }
    }
    override fun toString(): String {
        return "${javaClass.simpleName}<${getTable().sqlName}> id=${id}, objSrc=${source}, data=${data}"
    }

    final override val id: EntityId
        get() = data[getTable().idColumn]!!

    final override val createTime: Instant
        get() = data[getTable().createTimeColumn]!!

    final override val modifyTime: Instant
        get() = data[getTable().modifyTimeColumn]!!

    final override val hasId: Boolean
        get() = (id != NO_ID)

    override fun hashCode() = data.hashCode()

    override fun equals(other: Any?): Boolean {
        return (other is MacrosEntityImpl<*> && this.data == other.data) //&& isFromDb == ((MacrosEntity) o).isFromDb
    }

    fun equalsWithoutMetadata(o: MacrosSqlEntity<M>): Boolean {
        val columnsToCheck: MutableList<Column<M, *>> = ArrayList(getTable().columns)
        columnsToCheck.remove(getTable().idColumn)
        columnsToCheck.remove(getTable().createTimeColumn)
        columnsToCheck.remove(getTable().modifyTimeColumn)
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
    protected fun <M : MacrosEntityImpl<M>, J: Any, N : MacrosEntityImpl<N>> foreignKeyMatches(
        childObj: MacrosEntityImpl<M>,
        childCol: Column.Fk<M, J, N>,
        parentObj: MacrosEntityImpl<N>
    ): Boolean {
        val parentCol = childCol.parentColumn
        if (parentCol == parentObj.getTable().idColumn && parentObj.source == ObjectSource.TEST) {
            // disable ID check for TEST objects
            return true
        }
        return childObj.getData(childCol) == parentObj.getData(parentCol)
    }

}