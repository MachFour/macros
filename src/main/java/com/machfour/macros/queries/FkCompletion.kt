package com.machfour.macros.queries

import com.machfour.macros.sql.Column
import com.machfour.macros.sql.ColumnData
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.persistence.MacrosDatabase
import java.sql.SQLException

object FkCompletion {
    // fkColumns by definition contains only the foreign key columns
    private fun <M : MacrosEntity<M>> fkIdsPresent(obj: M): Boolean {
        // if the FK refers to an ID column and it's not nullable, make sure there's a value
        return obj.table.fkColumns.none {
            (it.parentColumn == it.parentTable.idColumn && !it.isNullable) &&
                    (!obj.hasData(it) || obj.getData(it) == MacrosEntity.NO_ID)
        }
    }

    // wildcard capture helper for natural key column type
    @Throws(SQLException::class)
    private fun <M : MacrosEntity<M>, J, N, I> completeFkIdColHelper(
        ds: MacrosDatabase,
        fkColumn: Column.Fk<M, J, N>,
        parentNaturalKeyCol: Column<N, I>,
        data: List<ColumnData<N>>
    ): Map<I, J> {
        assert(parentNaturalKeyCol.isUnique)
        val uniqueColumnValues : Set<I> = data.map {
            it[parentNaturalKeyCol] ?: error("parent natural key column had null data")
        }.toSet()
        val completedParentKeys = CoreQueries.selectColumnMap(
            ds,
            fkColumn.parentTable,
            parentNaturalKeyCol,
            fkColumn.parentColumn,
            uniqueColumnValues
        )
        return completedParentKeys.mapValues {
            val parentKey = it.value ?: error("Value was null for key column ${it.key}")
            parentKey
        }
    }

    private val allowedObjectSources = setOf(ObjectSource.IMPORT, ObjectSource.USER_NEW, ObjectSource.COMPUTED)

    // wildcard capture helper for parent unique column type
    @Throws(SQLException::class)
    private fun <M : MacrosEntity<M>, J, N> completeFkCol(ds: MacrosDatabase, objects: List<M>, fkCol: Column.Fk<M, J, N>): List<M> {
        val completedObjects = ArrayList<M>(objects.size)
        val naturalKeyData = ArrayList<ColumnData<N>>(objects.size)
        for (obj in objects) {
            // needs to be either imported data, new (from builder), or computed, for Recipe nutrition data
            assert(obj.objectSource in allowedObjectSources) { "Object is not from import, new or computed" }
            assert(obj.fkNaturalKeyMap.isNotEmpty()) { "Object has no FK data maps" }
            val objectNkData = obj.getFkParentNaturalKey(fkCol)
            naturalKeyData.add(objectNkData)
        }
        val parentNaturalKeyCol : Column<N, *> = fkCol.parentTable.naturalKeyColumn
                ?: error("Table " + fkCol.parentTable.name + " has no natural key defined")
        val foreignKeyToIdMapping: Map<*, J> = completeFkIdColHelper(ds, fkCol, parentNaturalKeyCol, naturalKeyData)
        for (obj in objects) {
            val newData = obj.dataFullCopy()
            // TODO might be able to remove one level of indirection here because the ColumnData object
            // only contains data for the parentNaturalKeyCol
            val fkParentNaturalKey: ColumnData<N> = obj.getFkParentNaturalKey(fkCol)
            val fkParentNaturalKeyData: Any = fkParentNaturalKey[parentNaturalKeyCol]
                ?: error("Column data contained no data for natural key column")
            val fkParentId: J = foreignKeyToIdMapping[fkParentNaturalKeyData]
                ?: error("Could not find ID for parent object (natural key: $parentNaturalKeyCol = $fkParentNaturalKeyData)")
            newData.put(fkCol, fkParentId)
            val newObject = obj.table.construct(newData, obj.objectSource)
            // copy over old FK data to new object
            newObject.copyFkNaturalKeyMap(obj)
            completedObjects.add(newObject)
        }
        return completedObjects
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> completeForeignKeys(ds: MacrosDatabase, objects: Collection<M>, fk: Column.Fk<M, *, *>): List<M> {
        return completeForeignKeys(ds, objects, listOf(fk))
    }

    // Methods used when saving multiple new objects to the database at once which must be cross-referenced, and
    // IDs are not known at the time of saving.
    // These methods replace a manual database retrieval of objects whose ID is needed. However, there still
    // needs to be a well-ordering of dependencies between the fields of each type of object, so that the first type
    // is inserted without depending on unknown fields/IDs of other types, the second depends only on the first, and so on
    @Throws(SQLException::class)
    // private for now, can make it public if we ever need multiple column completion
    private fun <M : MacrosEntity<M>> completeForeignKeys(ds: MacrosDatabase, objects: Collection<M>, which: List<Column.Fk<M, *, *>>): List<M> {
        if (objects.isEmpty()) {
            return emptyList()
        }
        val factory = objects.first().factory

        // mutable copy of first argument
        var partiallyCompletedObjects: List<M> = ArrayList(objects)
        // cycle through the FK columns.
        for (fkCol in which) {
            partiallyCompletedObjects = completeFkCol(ds, partiallyCompletedObjects, fkCol)
        }
        
        return partiallyCompletedObjects.map {
            // Check everything's fine and (not yet implemented) change source to ObjectSource.IMPORT_FK_PRESENT
            assert(fkIdsPresent(it))
            factory.construct(it.data, it.objectSource)
        }
    }
}