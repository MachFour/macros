package com.machfour.macros.queries

import com.machfour.macros.core.Column
import com.machfour.macros.core.ColumnData
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.objects.Serving
import com.machfour.macros.storage.MacrosDataSource
import java.sql.SQLException

object FkCompletion {
    // fkColumns by definition contains only the foreign key columns
    private fun <M : MacrosEntity<M>> fkIdsPresent(obj: M): Boolean {
        var idsPresent = true
        for (fkCol in obj.table.fkColumns) {
            // if the FK refers to an ID column and it's not nullable, make sure there's a value
            if (fkCol.parentColumn == fkCol.parentTable.idColumn && !fkCol.isNullable) {
                idsPresent = idsPresent and (obj.hasData(fkCol) && obj.getData(fkCol) != MacrosEntity.NO_ID)
            }
        }
        return idsPresent
    }

    // wildcard capture helper for natural key column type
    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>, J, N, I> completeFkIdColHelper(ds: MacrosDataSource,
            fkColumn: Column.Fk<M, J, N>, parentNaturalKeyCol: Column<N, I>, data: List<ColumnData<N>>): Map<I, J> {
        assert(parentNaturalKeyCol.isUnique)
        val uniqueColumnValues : Set<I> = data.map {
            it[parentNaturalKeyCol] ?: error("parent natural key column had null data")
        }.toSet()
        val completedParentKeys = ds.selectColumnMap(fkColumn.parentTable, parentNaturalKeyCol, fkColumn.parentColumn, uniqueColumnValues)
        return completedParentKeys.mapValues {
            val parentKey = it.value ?: error("Value was null for key column ${it.key}")
            parentKey
        }
    }

    // wildcard capture helper for parent unique column type
    @Throws(SQLException::class)
    private fun <M : MacrosEntity<M>, J, N> completeFkCol(ds: MacrosDataSource, objects: List<M>, fkCol: Column.Fk<M, J, N>): List<M> {
        val completedObjects: MutableList<M> = ArrayList(objects.size)
        val naturalKeyData: MutableList<ColumnData<N>> = ArrayList(objects.size)
        for (obj in objects) {
            // needs to be either imported data, new (from builder), or computed, for Recipe nutrition data
            assert(listOf(ObjectSource.IMPORT, ObjectSource.USER_NEW, ObjectSource.COMPUTED)
                    .contains(obj.objectSource)) { "Object is not from import, new or computed" }
            assert(obj.fkNaturalKeyMap.isNotEmpty()) { "Object has no FK data maps" }
            val objectNkData = obj.getFkParentNaturalKey(fkCol)
            naturalKeyData.add(objectNkData)
        }
        val parentNaturalKeyCol : Column<N, *> = fkCol.parentTable.naturalKeyColumn
                ?: error("Table " + fkCol.parentTable.name + " has no natural key defined")
        val foreignKeyToIdMapping: Map<*, J> = completeFkIdColHelper(ds, fkCol, parentNaturalKeyCol, naturalKeyData)
        for (obj in objects) {
            val newData = obj.allData.copy()
            // TODO might be able to remove one level of indirection here because the ColumnData object
            // only contains data for the parentNaturalKeyCol
            val fkParentNaturalKey : ColumnData<N> = obj.getFkParentNaturalKey(fkCol)
            val fkParentNaturalKeyData: Any = fkParentNaturalKey[parentNaturalKeyCol]
                    ?: throw RuntimeException("Column data contained no data for natural key column")
            val fkParentId : J = foreignKeyToIdMapping[fkParentNaturalKeyData]
                    ?: throw RuntimeException("Could not find ID for parent object (natural key: $parentNaturalKeyCol = $fkParentNaturalKeyData)")
            newData.put(fkCol, fkParentId)
            val newObject = obj.table.construct(newData, obj.objectSource)
            // copy over old FK data to new object
            newObject.copyFkNaturalKeyMap(obj)
            completedObjects.add(newObject)
        }
        return completedObjects
    }

    @JvmStatic
    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> completeForeignKeys(ds: MacrosDataSource, objects: Collection<M>, fk: Column.Fk<M, *, *>): List<M> {
        return completeForeignKeys(ds, objects, listOf(fk))
    }

    // Methods used when saving multiple new objects to the database at once which must be cross-referenced, and
    // IDs are not known at the time of saving.
    // These methods replace a manual database retrieval of objects whose ID is needed. However, there still
    // needs to be a well-ordering of dependencies between the fields of each type of object, so that the first type
    // is inserted without depending on unknown fields/IDs of other types, the second depends only on the first, and so on
    @JvmStatic
    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> completeForeignKeys(ds: MacrosDataSource, objects: Collection<M>, which: List<Column.Fk<M, *, *>>): List<M> {
        val completedObjects: MutableList<M> = ArrayList(objects.size)
        if (!objects.isEmpty()) {
            // objects without foreign key data yet (mutable copy of first argument)
            var partiallyCompletedObjects: List<M> = ArrayList(objects)

            // hack to get correct factory type without passing it explicitly as argument
            val factory = partiallyCompletedObjects[0].factory

            // cycle through the FK columns.
            for (fkCol in which) {
                partiallyCompletedObjects = completeFkCol(ds, partiallyCompletedObjects, fkCol)
            }
            // Check everything's fine and (not yet implemented) change source to ObjectSource.IMPORT_FK_PRESENT
            for (obj in partiallyCompletedObjects) {
                assert(fkIdsPresent(obj))
                completedObjects.add(factory.construct(obj.allData, obj.objectSource))
            }
        }
        return completedObjects
    }
}