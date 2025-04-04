package com.machfour.macros.queries

import com.machfour.macros.core.FkEntity
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException

private val allowedObjectSources = setOf(ObjectSource.IMPORT, ObjectSource.USER_NEW, ObjectSource.COMPUTED)

// fkColumns by definition contains only the foreign key columns
private fun <M : MacrosEntity<M>> fkIdsPresent(obj: M): Boolean {
    // if the FK refers to an ID column, and it's not nullable, make sure there's a value
    return obj.table.fkColumns.none {
        (it.parentColumn == it.parentTable.idColumn && !it.isNullable) &&
                (!obj.data.hasValue(it) || obj.getData(it) == MacrosEntity.NO_ID)
    }
}

private fun <M: MacrosEntity<M>, J: Any, N> FkEntity<M>.copyFkNaturalKey(from: FkEntity<M>, fkCol: Column.Fk<M, J, N>) {
    val parentKey = from.getFkParentKey(fkCol)
    
    // Generics ensure that the table types match
    @Suppress("UNCHECKED_CAST")
    val parentKeyCol = checkNotNull(parentKey.columns.singleOrNull() as Column<N, J>?) {
        "FkEntity $this requires exactly one FK parent column (got ${parentKey.columns})"
    }
    
    val keyData = checkNotNull(parentKey[parentKeyCol]) {
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
@Throws(SqlException::class)
private fun <M : MacrosEntity<M>, J: Any, N, I: Any> completeFkIdColHelper(
    db: SqlDatabase,
    fkColumn: Column.Fk<M, J, N>,
    parentKeyCol: Column<N, I>,
    data: List<RowData<N>>
): Map<I, J> {
    require(parentKeyCol.isUnique)
    val uniqueColumnValues = buildSet {
        data.mapTo(this) {
            requireNotNull(it[parentKeyCol]) { "parent natural key column had null data" }
        }
    }

    val completedParentKeys = selectColumnMap(
        db,
        fkColumn.parentTable,
        parentKeyCol,
        fkColumn.parentColumn,
        uniqueColumnValues,
        enforceNotNull = false
    )
    return completedParentKeys.mapValues {
        checkNotNull(it.value) { "Value was null for key column ${it.key}" }
    }
}


// wildcard capture helper for parent unique column type
@Throws(SqlException::class)
private fun <M : FkEntity<M>, J: Any, N> completeFkCol(
    ds: SqlDatabase,
    objects: List<M>,
    fkCol: Column.Fk<M, J, N>,
): List<M> {
    val parentKeyData = objects.map {
        // needs to be either imported data, new (from builder), or computed, for Recipe nutrition data
        check(it.source in allowedObjectSources) { "Object is not from import, new or computed" }
        check(it.fkParentKeyData.isNotEmpty()) { "Object has no FK data maps" }
        it.getFkParentKey(fkCol)
    }

    //val parentKeyCol: Column<N, *> = requireNotNull(fkCol.parentTable.naturalKeyColumn) {
    //    "Table ${fkCol.parentTable.name} has no natural key defined"
    //}

    // collect parent key columns of all objects, ensure they are the same
    val parentKeyCols: Set<Column<N, *>> = objects.flatMap { it.getFkParentKey(fkCol).columns }.toSet()

    val parentKeyCol = checkNotNull(parentKeyCols.singleOrNull()) {
        "All objects must specify the same, single parent key column (got $parentKeyCols)"
    }

    val foreignKeyToIdMapping: Map<*, J> = completeFkIdColHelper(ds, fkCol, parentKeyCol, parentKeyData)
    return objects.map { obj ->
        // TODO might be able to remove one level of indirection here because the RowData object
        // only contains data for the parentNaturalKeyCol
        val fkParentKey: RowData<N> = obj.getFkParentKey(fkCol)
        val fkParentKeyData: Any = requireNotNull(fkParentKey[parentKeyCol]) {
            "Fk data for $obj (col = $fkCol) contained no data for parent key $parentKeyCol"
        }
        val fkParentId: J = checkNotNull(foreignKeyToIdMapping[fkParentKeyData]) {
            "Could not find ID for parent object (key: $parentKeyCol = $fkParentKeyData)"
        }

        val newData = obj.dataFullCopy().also { it.put(fkCol, fkParentId) }

        // create new object nd copy over old FK data to new object
        obj.table.construct(newData, obj.source).also { it.copyFkNaturalKeyMap(obj) }
    }
}

// Methods used when saving multiple new objects to the database at once which must be cross-referenced, and
// IDs are not known at the time of saving.
// These methods replace a manual database retrieval of objects whose ID is needed. However, there still
// needs to be a well-ordering of dependencies between the fields of each type of object, so that the first type
// is inserted without depending on unknown fields/IDs of other types, the second depends only on the first, and so on
@Throws(SqlException::class)
// private for now, can make it public if we ever need multiple column completion
private fun <M : FkEntity<M>> completeForeignKeys(db: SqlDatabase, objects: Collection<M>, which: List<Column.Fk<M, *, *>>): List<M> {
    val factory = objects.firstOrNull()?.factory ?: return emptyList()

    // mutable copy of first argument
    var partiallyCompletedObjects: List<M> = ArrayList(objects)
    // cycle through the FK columns.
    for (fkCol in which) {
        partiallyCompletedObjects = completeFkCol(db, partiallyCompletedObjects, fkCol)
    }

    return partiallyCompletedObjects.map {
        // Check everything's fine and (not yet implemented) change source to ObjectSource.IMPORT_FK_PRESENT
        check(fkIdsPresent(it))
        factory.construct(it.data, it.source)
    }
}


@Throws(SqlException::class)
fun <M : FkEntity<M>> completeForeignKeys(ds: SqlDatabase, objects: Collection<M>, fk: Column.Fk<M, *, *>): List<M> {
    return completeForeignKeys(ds, objects, listOf(fk))
}