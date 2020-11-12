package com.machfour.macros.queries

import com.machfour.macros.core.*
import com.machfour.macros.storage.MacrosDataSource
import java.sql.SQLException

object Queries {
    @Throws(SQLException::class)
    fun <M> prefixSearch(ds: MacrosDataSource, t: Table<M>, cols: List<Column<M, String>>, keyword: String): List<Long> {
        return stringSearch(ds, t, cols, keyword, false, true)
    }

    @Throws(SQLException::class)
    fun <M> substringSearch(ds: MacrosDataSource, t: Table<M>, cols: List<Column<M, String>>, keyword: String): List<Long> {
        return stringSearch(ds, t, cols, keyword, true, true)
    }

    @Throws(SQLException::class)
    fun <M> exactStringSearch(ds: MacrosDataSource, t: Table<M>, cols: List<Column<M, String>>, keyword: String): List<Long> {
        return stringSearch(ds, t, cols, keyword, false, false)
    }

    @Throws(SQLException::class)
    fun <M> stringSearch(ds: MacrosDataSource, t: Table<M>, cols: List<Column<M, String>>, keyword: String, globBefore: Boolean, globAfter: Boolean): List<Long> {
        return ds.stringSearch(t, cols, keyword, globBefore, globAfter)
    }

    // Convenience method (default arguments)
    @Throws(SQLException::class)
    fun <M, I, J> selectColumn(ds: MacrosDataSource, t: Table<M>, selectColumn: Column<M, I>, whereColumn: Column<M, J>, whereValue: J): List<I?> {
        return selectColumn(ds, t, selectColumn, whereColumn, listOf(whereValue), false)
    }

    // does SELECT (selectColumn) FROM (t) WHERE (whereColumn) = (whereValue)
    // or SELECT (selectColumn) FROM (t) WHERE (whereColumn) IN (whereValue1, whereValue2, ...)
    @Throws(SQLException::class)
    fun <M, I, J> selectColumn(ds: MacrosDataSource, t: Table<M>, selected: Column<M, I>, where: Column<M, J>,
                               whereValues: Collection<J>, distinct: Boolean = false): List<I?> {
        return ds.selectColumn(t, selected, where, whereValues, distinct)
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
        val objectData : List<ColumnData<M>> = objects.map { it.data }
        return ds.insertObjectData(objectData, withId)
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> updateObjects(ds: MacrosDataSource, objects: Collection<M>): Int {
        return ds.updateObjects(objects)
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> deleteObject(ds: MacrosDataSource, o: M): Int {
        return ds.deleteById(o.id, o.table)
    }

    // TODO make this the general one
    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> deleteObjects(ds: MacrosDataSource, objects: List<M>): Int {
        var deleted = 0
        if (objects.isNotEmpty()) {
            val table = objects[0].table
            objects.forEach {
                deleted += ds.deleteById(it.id, table)
            }
        }
        return deleted
    }

    // deletes objects with the given ID from
    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> deleteObjectsById(ds: MacrosDataSource, table: Table<M>, ids: List<Long>): Int {
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
            ds.idExistsInTable(o.table, o.id)
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
}