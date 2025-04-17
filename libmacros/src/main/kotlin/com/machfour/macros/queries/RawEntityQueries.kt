package com.machfour.macros.queries

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.generator.AllColumnSelect
import com.machfour.macros.sql.generator.SelectQuery

// Constructs a map of ID to raw object data (i.e. no object references initialised)
// IDs that do not exist in the database will not be contained in the output map.
@Throws(SqlException::class)
fun <M, J: Any> getRawObjects(db: SqlDatabase, keyColumn: Column<M, J>, queryOptions: SelectQuery.Builder<M>.() -> Unit): Map<J, M> {
    require(keyColumn.isUnique) { "Key column must be unique" }

    val table = keyColumn.table
    val query = AllColumnSelect.build(table, queryOptions)

    val objects = if (query.isOrdered) LinkedHashMap<J, M>() else HashMap<J, M>()

    val resultData = db.selectAllColumns(query)
    for (objectData in resultData) {
        val key = objectData[keyColumn]
        checkNotNull(key) { "found null key in $table" }
        check(!objects.containsKey(key)) { "Key $key already in returned objects map!" }
        objects[key] = table.construct(objectData, ObjectSource.DATABASE)
    }
    return objects
}

@Throws(SqlException::class)
fun <M> getAllRawObjects(db: SqlDatabase, t: Table<M>, orderBy: Column<M, *>? = t.idColumn): Map<Long, M> {
    return getRawObjects(db, t.idColumn) {
        if (orderBy != null) {
            orderBy(orderBy)
        }
    }
}

@Throws(SqlException::class)
fun <M> getRawObjectsWithIds(
    db: SqlDatabase,
    t: Table<M>,
    ids: Collection<Long>,
    preserveIdOrder: Boolean = false,
    // if the number of keys exceeds this number, the query will be iterated
    iterateThreshold: Int? = null,
): Map<Long, M> {
    return getRawObjectsWithKeys(db, t.idColumn, ids, preserveIdOrder, iterateThreshold)
}

@Throws(SqlException::class)
fun <M, J: Any> getRawObjectsWithKeys(
    db: SqlDatabase,
    keyCol: Column<M, J>,
    keys: Collection<J>,
    preserveKeyOrder: Boolean = false,
    // if the number of keys exceeds this number, the query will be iterated
    iterateThreshold: Int? = null,
): Map<J, M> {
    if (keys.isEmpty()) {
        return emptyMap()
    }
    val unorderedObjects = getRawObjects(db, keyCol) {
        if (iterateThreshold != null) {
            where(keyCol, keys, iterateThreshold = iterateThreshold)
        } else {
            where(keyCol, keys)
        }
    }

    return if (preserveKeyOrder) {
        // match order of ids in input
        return buildMap(unorderedObjects.size) {
            keys.associateNotNullWithTo(this) { unorderedObjects[it] }
        }
    } else {
        unorderedObjects
    }
}


@Throws(SqlException::class)
fun <M, N> getRawObjectsForParentFk(
    db: SqlDatabase,
    parentIds: Collection<EntityId>,
    childTable: Table<M>,
    fkCol: Column.Fk<M, EntityId, N>
): Map<Long, M> {
    if (parentIds.isEmpty()) {
        return emptyMap()
    }
    val ids = selectNonNullColumn(db, childTable.idColumn) {
        where(fkCol, parentIds)
    }
    // if empty, no objects in the child table refer to any of the parent objects/rows
    return if (ids.isEmpty()) emptyMap() else getRawObjectsWithIds(db, childTable, ids)
}
