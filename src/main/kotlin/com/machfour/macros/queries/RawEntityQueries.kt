package com.machfour.macros.queries

import com.machfour.macros.core.ObjectSource
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.generator.AllColumnSelect
import com.machfour.macros.sql.generator.SelectQuery
import java.sql.SQLException

// Constructs a map of ID to raw object data (i.e. no object references initialised)
// IDs that do not exist in the database will not be contained in the output map.
@Throws(SQLException::class)
internal fun <M, J> getRawObjects(ds: SqlDatabase, keyColumn: Column<M, J>, queryOptions: SelectQuery.Builder<M>.() -> Unit): Map<J, M> {
    require(keyColumn.isUnique) { "Key column must be unique" }

    val t = keyColumn.table
    val query = AllColumnSelect.build(t, queryOptions)

    val objects = if (query.isOrdered) LinkedHashMap<J, M>() else HashMap<J, M>()

    val resultData = ds.selectAllColumns(query)
    for (objectData in resultData) {
        val key = objectData[keyColumn]
        checkNotNull(key) { "found null key in $t" }
        assert(!objects.containsKey(key)) { "Key $key already in returned objects map!" }
        objects[key] = t.construct(objectData, ObjectSource.DATABASE)
    }
    return objects
}

@Throws(SQLException::class)
internal fun <M> getAllRawObjects(db: SqlDatabase, t: Table<M>, orderBy: Column<M, *>? = t.idColumn): Map<Long, M> {
    return getRawObjects(db, t.idColumn) {
        if (orderBy != null) {
            orderBy(orderBy)
        }
    }
}

@Throws(SQLException::class)
internal fun <M> getRawObjectsWithIds(
    ds: SqlDatabase,
    t: Table<M>,
    ids: Collection<Long>,
    preserveIdOrder: Boolean = false,
    // if the number of keys exceeds this number, the query will be iterated
    iterateThreshold: Int = ITERATE_THRESHOLD,
): Map<Long, M> {
    return getRawObjectsWithKeys(ds, t.idColumn, ids, preserveIdOrder, iterateThreshold)
}

@Throws(SQLException::class)
internal fun <M, J> getRawObjectsWithKeys(
    ds: SqlDatabase,
    keyCol: Column<M, J>,
    keys: Collection<J>,
    preserveKeyOrder: Boolean = false,
    // if the number of keys exceeds this number, the query will be iterated
    iterateThreshold: Int = ITERATE_THRESHOLD,
): Map<J, M> {
    if (keys.isEmpty()) {
        return emptyMap()
    }
    val unorderedObjects = getRawObjects(ds, keyCol) {
        where(keyCol, keys, iterate = keys.size > iterateThreshold)
    }

    return if (preserveKeyOrder) {
        // match order of ids in input
        keys.intersect(unorderedObjects.keys).associateWith { unorderedObjects.getValue(it) }
    } else {
        unorderedObjects
    }
}


@Throws(SQLException::class)
internal fun <M, N> getRawObjectsForParentFk(
    ds: SqlDatabase,
    parentObjectMap: Map<Long, N>,
    childTable: Table<M>,
    fkCol: Column.Fk<M, Long, N>
): Map<Long, M> {
    if (parentObjectMap.isEmpty()) {
        return emptyMap()
    }
    val ids = selectNonNullColumn(ds, childTable.idColumn) {
        where(fkCol, parentObjectMap.keys, iterate = parentObjectMap.size > ITERATE_THRESHOLD)
    }
    // if empty, no objects in the child table refer to any of the parent objects/rows
    return if (ids.isEmpty()) emptyMap() else getRawObjectsWithIds(ds, childTable, ids)
}