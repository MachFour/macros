package com.machfour.macros.queries

import com.machfour.macros.core.FkEntity
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.SqlDatabase
import java.sql.SQLException

private val allowedObjectSources = setOf(ObjectSource.IMPORT, ObjectSource.USER_NEW, ObjectSource.COMPUTED)

// fkColumns by definition contains only the foreign key columns
private fun <M : MacrosEntity<M>> fkIdsPresent(obj: M): Boolean {
    // if the FK refers to an ID column and it's not nullable, make sure there's a value
    return obj.table.fkColumns.none {
        (it.parentColumn == it.parentTable.idColumn && !it.isNullable) &&
                (!obj.hasData(it) || obj.getData(it) == MacrosEntity.NO_ID)
    }
}

private fun <M: MacrosEntity<M>, J, N> FkEntity<M>.copyFkNaturalKey(from: FkEntity<M>, fkCol: Column.Fk<M, J, N>) {
    val parentKey = from.getFkParentKey(fkCol)
    
    // Generics ensure that the table types match
    @Suppress("UNCHECKED_CAST")
    val parentKeyCol = requireNotNull(parentKey.columns.singleOrNull() as Column<N, J>?) {
        "FkEntity $this requires exactly one FK parent column (got ${parentKey.columns})"
    }
    
    val keyData = requireNotNull(parentKey[parentKeyCol]) {
        "Null parent key data for entity $this, FK $fkCol, parent key col $parentKeyCol"
    }
    setFkParentKey(fkCol, parentKeyCol, keyData)
}

private fun <M: MacrosEntity<M>> FkEntity<M>.copyFkNaturalKeyMap(from: FkEntity<M>) {
    for (fkCol in from.fkParentKeyData.keys) {
        copyFkNaturalKey(from, fkCol)
    }
}


// wildcard capture helper for natural key column type
@Throws(SQLException::class)
private fun <M : MacrosEntity<M>, J, N, I> completeFkIdColHelper(
    db: SqlDatabase,
    fkColumn: Column.Fk<M, J, N>,
    parentKeyCol: Column<N, I>,
    data: List<RowData<N>>
): Map<I, J> {
    require(parentKeyCol.isUnique)
    val uniqueColumnValues = data.map {
        requireNotNull(it[parentKeyCol]) { "parent natural key column had null data" }
    }.toSet()

    val completedParentKeys = selectColumnMap(
        db,
        fkColumn.parentTable,
        parentKeyCol,
        fkColumn.parentColumn,
        uniqueColumnValues,
        enforceNotNull = false
    )
    return completedParentKeys.mapValues {
        requireNotNull(it.value) { "Value was null for key column ${it.key}" }
    }
}


// wildcard capture helper for parent unique column type
@Throws(SQLException::class)
private fun <M : FkEntity<M>, J, N> completeFkCol(
    ds: SqlDatabase,
    objects: List<M>,
    fkCol: Column.Fk<M, J, N>,
): List<M> {
    val parentKeyData = objects.map {
        // needs to be either imported data, new (from builder), or computed, for Recipe nutrition data
        assert(it.source in allowedObjectSources) { "Object is not from import, new or computed" }
        assert(it.fkParentKeyData.isNotEmpty()) { "Object has no FK data maps" }
        it.getFkParentKey(fkCol)
    }


    //val parentKeyCol: Column<N, *> = requireNotNull(fkCol.parentTable.naturalKeyColumn) {
    //    "Table ${fkCol.parentTable.name} has no natural key defined"
    //}

    // collect parent key columns of all objects, ensure they are the same
    val parentKeyCols: Set<Column<N, *>> = objects.flatMap { it.getFkParentKey(fkCol).columns }.toSet()

    val parentKeyCol = parentKeyCols.singleOrNull() ?: error {
        "All objects must specify the same, single parent key column (got $parentKeyCols)"
    }


    val foreignKeyToIdMapping: Map<*, J> = completeFkIdColHelper(ds, fkCol, parentKeyCol, parentKeyData)

    val completedObjects = objects.map {
        // TODO might be able to remove one level of indirection here because the RowData object
        // only contains data for the parentNaturalKeyCol
        val fkParentKey: RowData<N> = it.getFkParentKey(fkCol)
        val fkParentKeyData: Any = requireNotNull(fkParentKey[parentKeyCol]) {
            "Fk data for $it (col = $fkCol) contained no data for parent key $parentKeyCol"
        }
        val fkParentId: J = requireNotNull(foreignKeyToIdMapping[fkParentKeyData]) {
            "Could not find ID for parent object (key: $parentKeyCol = $fkParentKeyData)"
        }

        val newData = it.dataFullCopy().apply { put(fkCol, fkParentId) }

        // create new object nd copy over old FK data to new object
        it.table.construct(newData, it.source).also { newObject ->
            newObject.copyFkNaturalKeyMap(it)
        }
    }
    return completedObjects
}

// Methods used when saving multiple new objects to the database at once which must be cross-referenced, and
// IDs are not known at the time of saving.
// These methods replace a manual database retrieval of objects whose ID is needed. However, there still
// needs to be a well-ordering of dependencies between the fields of each type of object, so that the first type
// is inserted without depending on unknown fields/IDs of other types, the second depends only on the first, and so on
@Throws(SQLException::class)
// private for now, can make it public if we ever need multiple column completion
private fun <M : FkEntity<M>> completeForeignKeys(ds: SqlDatabase, objects: Collection<M>, which: List<Column.Fk<M, *, *>>): List<M> {
    val factory = objects.firstOrNull()?.factory ?: return emptyList()

    // mutable copy of first argument
    var partiallyCompletedObjects: List<M> = ArrayList(objects)
    // cycle through the FK columns.
    for (fkCol in which) {
        partiallyCompletedObjects = completeFkCol(ds, partiallyCompletedObjects, fkCol)
    }

    return partiallyCompletedObjects.map {
        // Check everything's fine and (not yet implemented) change source to ObjectSource.IMPORT_FK_PRESENT
        assert(fkIdsPresent(it))
        factory.construct(it.data, it.source)
    }
}


@Throws(SQLException::class)
internal fun <M : FkEntity<M>> completeForeignKeys(ds: SqlDatabase, objects: Collection<M>, fk: Column.Fk<M, *, *>): List<M> {
    return completeForeignKeys(ds, objects, listOf(fk))
}