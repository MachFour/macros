package com.machfour.macros.queries

import com.machfour.macros.sql.Column
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.sql.Table
import com.machfour.macros.persistence.MacrosDatabase
import com.machfour.macros.sql.generator.AllColumnSelect
import com.machfour.macros.sql.generator.MultiColumnSelect
import java.sql.SQLException

internal object RawEntityQueries {

    // Constructs a map of key column value to raw object data (i.e. no object references initialised
    // Keys that do not exist in the database will not be contained in the output map
    // The returned map is never null and is unordered
    @Throws(SQLException::class)
    internal fun <M, J> getRawObjectsByKeys(
        ds: MacrosDatabase,
        t: Table<M>,
        keyCol: Column<M, J>,
        keys: Collection<J>,
        // if the number of keys exceeds this number, the query will be iterated
        iterateThreshold: Int = CoreQueries.ITERATE_THRESHOLD,
    ): Map<J, M> {
        require(keyCol.isUnique) { "Key column must be unique" }
        // Without this check, if the list of keys is empty, every row will be returned
        if (keys.isEmpty()) {
            return emptyMap()
        }
        val query = MultiColumnSelect.build(t, t.columns) {
            where(keyCol, keys, iterate = keys.size > iterateThreshold)
        }

        return HashMap<J, M>(keys.size, 1.0f).apply {
            ds.selectMultipleColumns(t, query).forEach { data ->
                val key = data[keyCol]!!
                assert(!this.containsKey(key)) { "Key $key already in returned objects map!" }
                val newObject = t.factory.construct(data, ObjectSource.DATABASE)
                this[key] = newObject
            }
        }
    }

    @Throws(SQLException::class)
    private fun <M> getRawObjectsById(ds: MacrosDatabase, t: Table<M>, query: AllColumnSelect<M>): Map<Long, M> {
        val objects = if (query.isOrdered) LinkedHashMap<Long, M>() else HashMap<Long, M>()
        val resultData = ds.selectAllColumns(t, query)
        for (objectData in resultData) {
            val id = objectData[t.idColumn]
            check(id != null) { "found null ID in $t" }
            assert(!objects.containsKey(id)) { "ID $id already in returned objects map!" }
            val newObject = t.factory.construct(objectData, ObjectSource.DATABASE)
            objects[id] = newObject
        }
        return objects
    }

    @Throws(SQLException::class)
    internal fun <M> getAllRawObjects(ds: MacrosDatabase, t: Table<M>, orderBy: Column<M, *>? = t.idColumn): Map<Long, M> {
        val query = AllColumnSelect.build(t) {
            if (orderBy != null) {
                orderBy(orderBy)
            }
        }
        return getRawObjectsById(ds, t, query)
    }

    @Throws(SQLException::class)
    internal fun <M> getRawObjectsByIds(ds: MacrosDatabase, t: Table<M>, ids: Collection<Long>): Map<Long, M> {
        return getRawObjectsByKeys(ds, t, t.idColumn, ids)
    }

    @Throws(SQLException::class)
    internal fun <M, N> getRawObjectsForParentFk(
        ds: MacrosDatabase,
        parentObjectMap: Map<Long, N>,
        childTable: Table<M>,
        fkCol: Column.Fk<M, Long, N>
    ): Map<Long, M> {
        if (parentObjectMap.isNotEmpty()) {
            val childIdCol = childTable.idColumn
            val ids = CoreQueries.selectNonNullColumn(ds, childTable, childIdCol) {
                where(fkCol, parentObjectMap.keys)
            }
            if (ids.isNotEmpty()) {
                return getRawObjectsByKeys(ds, childTable, childIdCol, ids)
            }
            // else no objects in the child table refer to any of the parent objects/rows
        }
        return emptyMap()
    }

}