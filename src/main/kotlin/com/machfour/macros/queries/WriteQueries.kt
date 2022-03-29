package com.machfour.macros.queries

import com.machfour.macros.core.FoodType
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.Food
import com.machfour.macros.schema.FoodPortionTable
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.IngredientTable
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.generator.SimpleDelete

// These queries modify stuff so to preserve the cache layer, their use is restricted to in-module

/* These functions save the objects given to them into the database, via INSERT or UPDATE.
 * The caller should ensure that objects with an id of null correspond to new entries
 * (INSERTs) into the database, while those with a non-null id correspond to UPDATES of existing
 * rows in the table.
 *
 * Any data that originated from the user should already have been validated
 */
// Do we really need the list methods? The user will probably only edit one object at a time
// except for deleting a bunch of foodPortions from one meal, or servings from a food
@Throws(SqlException::class)
internal fun <M : MacrosEntity<M>> insertObjects(db: SqlDatabase, objects: Collection<M>, withId: Boolean): Int {
    return db.insertRows(objects.map { it.data }, withId)
}

@Throws(SqlException::class)
internal fun <M : MacrosEntity<M>> updateObjects(db: SqlDatabase, objects: Collection<M>): Int {
    return db.updateRows(objects.map { it.data })
}

@Throws(SqlException::class)
internal fun <M : MacrosEntity<M>> deleteObject(db: SqlDatabase, o: M): Int {
    return deleteById(db, o.table, o.id)
}

@Throws(SqlException::class)
internal fun <M : MacrosEntity<M>> deleteObjects(db: SqlDatabase, objects: Collection<M>): Int {
    val table = objects.firstOrNull()?.table ?: return 0
    return deleteObjectsById(db, table, objects.map { it.id })
}

// deletes objects with the given ID from
@Throws(SqlException::class)
private fun <M : MacrosEntity<M>> deleteObjectsById(
    db: SqlDatabase,
    table: Table<M>,
    ids: Collection<Long>
): Int {
    return deleteWhere(db, table, table.idColumn, ids)
}

// returns number of objects saved correctly (i.e. 0 or 1)
// NB: not (yet) possible to return the ID of the saved object with SQLite JDBC
@Throws(SqlException::class)
internal fun <M : MacrosEntity<M>> saveObject(db: SqlDatabase, o: M): Int {
    return saveObjects(db, listOf(o), o.source)
}

// TODO pull these functions up to DataSource level
@Throws(SqlException::class)
internal fun <M : MacrosEntity<M>> saveObjects(
    db: SqlDatabase,
    objects: Collection<M>,
    objectSource: ObjectSource
): Int {
    return when (objectSource) {
        ObjectSource.IMPORT, ObjectSource.USER_NEW -> insertObjects(db, objects, false)
        ObjectSource.DB_EDIT -> updateObjects(db, objects)
        ObjectSource.DATABASE -> {
            assert(false) { "Saving unmodified database object" }
            0
        }
        ObjectSource.RESTORE -> {
            // will have ID. Assume database has been cleared?
            insertObjects(db, objects, true)
        }
        ObjectSource.COMPUTED -> {
            assert(false) { "Saving a computed object" }
            0
        }
        else -> {
            assert(false) { "Unrecognised object source: $objectSource" }
            0
        }
    }
}

/*
 * Entity-specific
 */
@Throws(SqlException::class)
internal fun forgetFood(db: SqlDatabase, f: Food) {
    require(f.source === ObjectSource.DATABASE) { "Food ${f.indexName} is not in DB" }
    // delete nutrition data, foodQuantities, servings, then food

    // servings and nutrient values are deleted on cascade, so we only have to worry about foodquantities
    deleteWhere(db, FoodPortionTable, FoodPortionTable.FOOD_ID, listOf(f.id))
    deleteWhere(db, IngredientTable, IngredientTable.FOOD_ID, listOf(f.id))
    deleteObject(db, f)
}

// TODO replace with update template
@Throws(SqlException::class)
internal fun setSearchRelevanceForFoodType(db: SqlDatabase, foodType: FoodType, value: Int) {
    db.executeRawStatement(
        "UPDATE ${FoodTable.name} SET ${FoodTable.SEARCH_RELEVANCE} = $value WHERE " +
                "${FoodTable.FOOD_TYPE} = '${foodType.niceName}'"
    )
}

@Throws(SqlException::class)
private fun <M> deleteById(db: SqlDatabase, t: Table<M>, id: Long): Int {
    return db.deleteFromTable(SimpleDelete.build(t) { where(t.idColumn, id) })
}


// does DELETE FROM (t) WHERE (whereColumn) = (whereValue)
// or DELETE FROM (t) WHERE (whereColumn) IN (whereValue1, whereValue2, ...)
@Throws(SqlException::class)
fun <M, J> deleteWhere(
    db: SqlDatabase,
    t: Table<M>,
    whereColumn: Column<M, J>,
    whereValues: Collection<J>
): Int {
    return db.deleteFromTable(SimpleDelete.build(t) {
        where(whereColumn, whereValues)
    })
}

// does DELETE FROM (t) WHERE (whereColumn) IS (NOT) NULL
@Throws(SqlException::class)
internal fun <M, J> deleteByNullStatus(
    db: SqlDatabase,
    t: Table<M>,
    whereColumn: Column<M, J>,
    trueForNotNulls: Boolean
): Int {
    return db.deleteFromTable(SimpleDelete.build(t) {
        where(
            whereColumn,
            isNotNull = trueForNotNulls
        )
    })
}

@Throws(SqlException::class)
internal fun deleteAllCompositeFoods(db: SqlDatabase): Int {
    return deleteWhere(db, FoodTable, FoodTable.FOOD_TYPE, listOf(FoodType.COMPOSITE.niceName))
}

@Throws(SqlException::class)
internal fun deleteAllIngredients(db: SqlDatabase) {
    clearTable(db, IngredientTable)
}

@Throws(SqlException::class)
internal fun deleteAllFoodPortions(db: SqlDatabase) {
    clearTable(db, FoodPortionTable)
}


@Throws(SqlException::class)
internal fun <M> clearTable(db: SqlDatabase, t: Table<M>): Int {
    return db.deleteFromTable(SimpleDelete.build(t) {})
}