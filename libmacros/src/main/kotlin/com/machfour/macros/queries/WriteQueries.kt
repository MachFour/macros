package com.machfour.macros.queries

import com.machfour.macros.core.*
import com.machfour.macros.entities.Food
import com.machfour.macros.schema.FoodPortionTable
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.IngredientTable
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.entities.MacrosSqlEntity
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
fun <M : MacrosEntity> insertObjects(db: SqlDatabase, table: Table<M>, objects: Collection<M>, useDataIds: Boolean): Int {
    return db.insertRows(objects.map { table.deconstruct(it) }, useDataIds)
}

@Throws(SqlException::class)
fun <M : MacrosEntity> insertObjectsReturningIds(db: SqlDatabase, table: Table<M>, objects: Collection<M>, useDataIds: Boolean): List<EntityId> {
    return db.insertRowsReturningIds(objects.map { table.deconstruct(it) }, useDataIds)
}

@Throws(SqlException::class)
fun <M : MacrosEntity> updateObjects(db: SqlDatabase, table: Table<M>, objects: Collection<M>): Int {
    return db.updateRows(objects.map { table.deconstruct(it) })
}

@Throws(SqlException::class)
fun <M : MacrosEntity> deleteObject(db: SqlDatabase, table: Table<M>, o: M): Int {
    return deleteById(db, table, o.id)
}

@Throws(SqlException::class)
fun <M : MacrosEntity> deleteObjects(db: SqlDatabase, table: Table<M>, objects: Collection<M>): Int {
    return deleteObjectsById(db, table, objects.map { it.id })
}

// deletes objects with the given ID from
@Throws(SqlException::class)
private fun <M : MacrosEntity> deleteObjectsById(
    db: SqlDatabase,
    table: Table<M>,
    ids: Collection<Long>
): Int {
    return deleteWhere(db, table, table.idColumn, ids)
}

// returns number of objects saved correctly (i.e. 0 or 1)
@Throws(SqlException::class)
fun <M : MacrosSqlEntity<M>> saveObject(db: SqlDatabase, o: M): EntityId {
    return saveObjectsReturningIds(db, o.table, listOf(o), o.source).first()
}

@Throws(SqlException::class)
fun <M : MacrosEntity> saveObjectsReturningIds(
    db: SqlDatabase,
    table: Table<M>,
    objects: Collection<M>,
    objectSource: ObjectSource
    ): List<EntityId> {
    return when (objectSource) {
        ObjectSource.DATABASE -> error { "Saving unmodified database object" }
        ObjectSource.DB_EDIT -> {
            updateObjects(db, table, objects)
            objects.map { it.id }
        }
        ObjectSource.IMPORT, ObjectSource.USER_NEW -> insertObjectsReturningIds(db, table, objects, false)
        // TODO switch on whether they have IDs
        ObjectSource.JSON -> insertObjectsReturningIds(db, table, objects, false)
        // will have ID. Assume database has been cleared?
        ObjectSource.RESTORE -> insertObjectsReturningIds(db, table, objects, true)
        ObjectSource.COMPUTED -> error { "Saving a computed object" }
        ObjectSource.TEST -> error { "Saving a test object" }
        ObjectSource.INBUILT -> error { "Saving an inbuilt object" }
    }
}

// TODO pull these functions up to DataSource level
@Throws(SqlException::class)
fun <M : MacrosEntity> saveObjects(
    db: SqlDatabase,
    table: Table<M>,
    objects: Collection<M>,
    objectSource: ObjectSource
): Int {
    return when (objectSource) {
        ObjectSource.DATABASE -> error { "Saving unmodified database object" }
        ObjectSource.DB_EDIT -> updateObjects(db, table, objects)
        ObjectSource.IMPORT, ObjectSource.USER_NEW -> insertObjects(db, table, objects, false)
        // TODO switch on whether they have IDs
        ObjectSource.JSON -> insertObjects(db, table, objects, false)
        // will have ID. Assume database has been cleared?
        ObjectSource.RESTORE -> insertObjects(db, table, objects, true)
        ObjectSource.COMPUTED -> error { "Saving a computed object" }
        ObjectSource.TEST -> error { "Saving a test object" }
        ObjectSource.INBUILT -> error { "Saving an inbuilt object" }
    }
}

/*
 * Entity-specific
 */
@Throws(SqlException::class)
fun forgetFood(db: SqlDatabase, f: Food) {
    require(f.source === ObjectSource.DATABASE) { "Food ${f.indexName} is not in DB" }
    // delete nutrition data, foodQuantities, servings, then food

    // servings and nutrient values are deleted on cascade, so we only have to worry about food quantities
    deleteWhere(db, FoodPortionTable, FoodPortionTable.FOOD_ID, listOf(f.id))
    deleteWhere(db, IngredientTable, IngredientTable.FOOD_ID, listOf(f.id))
    deleteObject(db, FoodTable, f)
}

// TODO replace with update template
@Throws(SqlException::class)
fun setSearchRelevanceForFoodType(db: SqlDatabase, foodType: FoodType, value: Int) {
    db.executeRawStatement(
        "UPDATE ${FoodTable.sqlName} SET ${FoodTable.SEARCH_RELEVANCE} = $value WHERE " +
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
fun <M, J: Any> deleteWhere(
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
fun <M, J: Any> deleteByNullStatus(
    db: SqlDatabase,
    t: Table<M>,
    whereColumn: Column<M, J>,
    negate: Boolean
): Int {
    return db.deleteFromTable(SimpleDelete.build(t) {
        whereNull(whereColumn, negate)
    })
}

@Throws(SqlException::class)
fun deleteAllCompositeFoods(db: SqlDatabase): Int {
    return deleteWhere(db, FoodTable, FoodTable.FOOD_TYPE, listOf(FoodType.COMPOSITE.niceName))
}

@Throws(SqlException::class)
fun deleteAllIngredients(db: SqlDatabase) {
    clearTable(db, IngredientTable)
}

@Throws(SqlException::class)
fun deleteAllFoodPortions(db: SqlDatabase) {
    clearTable(db, FoodPortionTable)
}


@Throws(SqlException::class)
fun <M> clearTable(db: SqlDatabase, t: Table<M>): Int {
    return db.deleteFromTable(SimpleDelete.build(t) {})
}