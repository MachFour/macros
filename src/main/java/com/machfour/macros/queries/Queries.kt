package com.machfour.macros.queries

import com.machfour.macros.core.*
import com.machfour.macros.sql.*
import com.machfour.macros.persistence.MacrosDataSource
import java.sql.SQLException

object Queries {
    @Throws(SQLException::class)
    fun <M> prefixSearch(ds: MacrosDataSource, t: Table<M>, cols: List<Column<M, String>>, keyword: String): List<Long> {
        return stringSearch(ds, t, cols, keyword, globBefore = false, globAfter = true)
    }

    @Throws(SQLException::class)
    fun <M> substringSearch(ds: MacrosDataSource, t: Table<M>, cols: List<Column<M, String>>, keyword: String): List<Long> {
        return stringSearch(ds, t, cols, keyword, globBefore = true, globAfter = true)
    }

    @Throws(SQLException::class)
    fun <M> exactStringSearch(ds: MacrosDataSource, t: Table<M>, cols: List<Column<M, String>>, keyword: String): List<Long> {
        return stringSearch(ds, t, cols, keyword, globBefore = false, globAfter = false)
    }

    /*
     * Returns empty list for either blank keyword or column list
     */
    @Throws(SQLException::class)
    fun <M> stringSearch(
        ds: MacrosDataSource,
        t: Table<M>,
        cols: List<Column<M, String>>,
        keyword: String,
        globBefore: Boolean,
        globAfter: Boolean
    ): List<Long> {
        if (keyword.isEmpty() || cols.isEmpty()) {
            return emptyList()
        }

        val keywordGlob = (if (globBefore) "%" else "") + keyword + if (globAfter) "%" else ""
        val keywordCopies = List(cols.size) { keywordGlob }
        val query = SingleColumnSelect.build(t, t.idColumn) {
            whereLike(cols, keywordCopies)
        }
        return selectNonNullColumn(ds, query)
    }


    // for convenience
    @Throws(SQLException::class)
    fun <M, I> selectSingleColumn(
        ds: MacrosDataSource,
        table: Table<M>,
        selectColumn: Column<M, I>,
        queryOptions: SingleColumnSelect<M, I>.() -> Unit
    ): List<I?> {
        return ds.selectColumn(SingleColumnSelect.build(table, selectColumn, queryOptions))
    }
    @Throws(SQLException::class)
    fun <M, I> selectNonNullColumn(
        ds: MacrosDataSource,
        table: Table<M>,
        selectColumn: Column<M, I>,
        queryOptions: SingleColumnSelect<M, I>.() -> Unit
    ): List<I> {
        return selectNonNullColumn(ds, SingleColumnSelect.build(table, selectColumn, queryOptions))
    }

    @Throws(SQLException::class)
    private fun <M, I> selectNonNullColumn(ds: MacrosDataSource, query: SingleColumnSelect<M, I>): List<I> {
        require(!query.selectColumn.isNullable) { "column is nullable" }
        val values = ds.selectColumn(query)
        val nonNullValues = ArrayList<I>(values.size)
        for (value in values) {
            require(value != null) { "Found null value for column ${query.selectColumn} in table ${query.table}"}
            nonNullValues.add(value)
        }
        return nonNullValues
    }

    /* These functions save the objects given to them into the database, via INSERT or UPDATE.
     * The caller should ensure that objects with an id of null correspond to new entries
     * (INSERTs) into the database, while those with a non-null id correspond to UPDATES of existing
     * rows in the table.
     *
     * Any data that originated from the user should already have been validated
     */
    // Do we really need the list methods? The user will probably only edit one object at a time
    // except for deleting a bunch of foodPortions from one meal, or servings from a food
    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> insertObjects(ds: MacrosDataSource, objects: Collection<M>, withId: Boolean): Int {
        val objectData = objects.map { it.data }
        return ds.insertObjectData(objectData, withId)
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> updateObjects(ds: MacrosDataSource, objects: Collection<M>): Int {
        return ds.updateObjects(objects)
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> deleteObject(ds: MacrosDataSource, o: M): Int {
        return ds.deleteById(o.table, o.id)
    }

    // TODO make this the general one
    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> deleteObjects(ds: MacrosDataSource, objects: Collection<M>): Int {
        var deleted = 0
        if (objects.isNotEmpty()) {
            val table = objects.first().table
            objects.forEach {
                deleted += ds.deleteById(table, it.id)
            }
        }
        return deleted
    }

    // deletes objects with the given ID from
    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> deleteObjectsById(ds: MacrosDataSource, table: Table<M>, ids: Collection<Long>): Int {
        return ds.deleteByColumn(table, table.idColumn, ids)
    }

    // returns number of objects saved correctly (i.e. 0 or 1)
    // NB: not (yet) possible to return the ID of the saved object with SQLite JDBC
    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> saveObject(ds: MacrosDataSource, o: M): Int {
        return saveObjects(ds, listOf(o), o.objectSource)
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> saveObjects(ds: MacrosDataSource, objects: Collection<M>, objectSource: ObjectSource): Int {
        return when (objectSource) {
            ObjectSource.IMPORT, ObjectSource.USER_NEW -> insertObjects(ds, objects, false)
            ObjectSource.DB_EDIT -> updateObjects(ds, objects)
            ObjectSource.DATABASE -> {
                assert(false) { "Saving unmodified database object" }
                0
            }
            ObjectSource.RESTORE -> {
                // will have ID. Assume database has been cleared?
                insertObjects(ds, objects, true)
            }
            ObjectSource.COMPUTED -> {
                assert(false) { "Why save a computed object?" }
                0
            }
            else -> {
                assert(false) { "Unrecognised object source: $objectSource" }
                0
            }
        }
    }


    @Throws(SQLException::class)
    private fun <M : MacrosEntity<M>> isInDatabase(ds: MacrosDataSource, o: M): Boolean {
        return if (o.id != MacrosEntity.NO_ID) {
            idExistsInTable(ds, o.table, o.id)
        } else {
            val secondaryKey = o.table.secondaryKeyCols
            if (secondaryKey.isEmpty()) {
                // no way to know except by ID...
            }
            TODO()
            @Suppress("UNREACHABLE_CODE")
            false
        }
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> idExistsInTable(ds: MacrosDataSource, table: Table<M>, id: Long): Boolean {
        val idCol = table.idColumn
        val idMatch = selectSingleColumn(ds, table, idCol) {
            where(idCol, id)
        }
        return idMatch.size == 1
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> idsExistInTable(ds: MacrosDataSource, table: Table<M>, queryIds: Collection<Long>): Map<Long, Boolean> {
        val idCol: Column<M, Long> = table.idColumn
        val existingIds = selectNonNullColumn(ds, table, idCol) {
            where(idCol, queryIds)
        }.toSet()

        return LinkedHashMap<Long, Boolean>(queryIds.size, 1f).apply {
            for (id in queryIds) {
                this[id] = existingIds.contains(id)
            }

        }
    }

    // Constructs a map of key column value to raw object data (i.e. no object references initialised
    // Keys that do not exist in the database will not be contained in the output map
    // The returned map is never null and is unordered
    @Throws(SQLException::class)
    fun <M, J> getRawObjectsByKeys(ds: MacrosDataSource, t: Table<M>, keyCol: Column<M, J>, keys: Collection<J>): Map<J, M> {
        require(keyCol.isUnique) { "Key column must be unique" }
        // if the list of keys is empty, every row will be returned
        if (keys.isEmpty()) {
            return emptyMap()
        }
        val query = MultiColumnSelect.build(t, t.columns) {
            where(keyCol, keys)
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
    fun <M> getRawObjectsById(ds: MacrosDataSource, t: Table<M>, query: AllColumnSelect<M>): Map<Long, M> {
        val objects = if (query.isOrdered) LinkedHashMap<Long, M>() else HashMap<Long, M>()
        val resultData = ds.selectAllColumns(t, query)
        for (objectData in resultData) {
            val id = objectData[t.idColumn]
            assert(id != null) { "found null ID in $t" }
            assert(!objects.containsKey(id)) { "ID $id already in returned objects map!" }
            val newObject = t.factory.construct(objectData, ObjectSource.DATABASE)
            objects[id!!] = newObject
        }
        return objects
    }

    @Throws(SQLException::class)
    fun <M> getAllRawObjects(ds: MacrosDataSource, t: Table<M>, orderBy: Column<M, *>? = t.idColumn): Map<Long, M> {
        val query = AllColumnSelect.build(t) {
            if (orderBy != null) {
                orderBy(orderBy)
            }
        }
        return getRawObjectsById(ds, t, query)
    }

    // The resulting map is unordered
    @Throws(SQLException::class)
    fun <M, I, J> selectColumnMap(
        ds: MacrosDataSource,
        t: Table<M>,
        keyColumn: Column<M, I>,
        valueColumn: Column<M, J>,
        keys: Collection<I>,
        // when number of keys gets too large, split up the query
        iterateThreshold: Int = 100
    ): Map<I, J?> {
        require(!keyColumn.isNullable && keyColumn.isUnique) { "Key column $keyColumn (table $t) must be unique and not nullable" }
        val unorderedResults = HashMap<I, J?>(keys.size, 1.0f)
        val query = TwoColumnSelect.build(t, keyColumn, valueColumn) {
            where(keyColumn, keys, iterate = keys.size >= iterateThreshold)
        }
        val data = ds.selectTwoColumns(query)
        for (pair in data) {
            val (key, value) = pair
            assert(key != null) { "Found null key in ${t}.$keyColumn!" }
            assert(!unorderedResults.containsKey(key)) { "Two rows in the DB contained the same data in the key column!" }
            unorderedResults[key!!] = value

        }
        return unorderedResults
    }

    // The resulting map is unordered
    @Throws(SQLException::class)
    fun <M, I> getIdsByKeys(
        ds: MacrosDataSource,
        t: Table<M>,
        keyColumn: Column<M, I>,
        keys: Collection<I>
    ): Map<I, Long> {
        val keyMap = selectColumnMap(ds, t, keyColumn, t.idColumn, keys)
        return keyMap.mapValues { it.value!! }
    }
}