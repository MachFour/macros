package com.machfour.macros.queries

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.entities.Food
import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.entities.FoodType
import com.machfour.macros.entities.Ingredient
import com.machfour.macros.orm.ObjectSource
import com.machfour.macros.orm.schema.FoodPortionTable
import com.machfour.macros.orm.schema.FoodTable
import com.machfour.macros.orm.schema.IngredientTable
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.Table
import java.sql.SQLException

/*
 * These queries modify stuff so to preserve the cache layer, their use is restricted to in-module
 */
// TODO make internal
object WriteQueries {

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
    fun <M : MacrosEntity<M>> insertObjects(ds: SqlDatabase, objects: Collection<M>, withId: Boolean): Int {
        return ds.insertRows(objects.map { it.data }, withId)
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> updateObjects(ds: SqlDatabase, objects: Collection<M>): Int {
        return ds.updateRows(objects.map { it.data })
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> deleteObject(ds: SqlDatabase, o: M): Int {
        return ds.deleteById(o.table, o.id)
    }

    // TODO make this the general one
    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> deleteObjects(ds: SqlDatabase, objects: Collection<M>): Int {
        val table = objects.firstOrNull()?.table ?: return 0
        return objects.fold(0) { numDeleted, m -> numDeleted + ds.deleteById(table, m.id) }
    }

    // deletes objects with the given ID from
    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> deleteObjectsById(ds: SqlDatabase, table: Table<M>, ids: Collection<Long>): Int {
        return ds.deleteByColumn(table, table.idColumn, ids)
    }

    // returns number of objects saved correctly (i.e. 0 or 1)
    // NB: not (yet) possible to return the ID of the saved object with SQLite JDBC
    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> saveObject(ds: SqlDatabase, o: M): Int {
        return saveObjects(ds, listOf(o), o.objectSource)
    }

    @Throws(SQLException::class)
    fun <M : MacrosEntity<M>> saveObjects(ds: SqlDatabase, objects: Collection<M>, objectSource: ObjectSource): Int {
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
    @Throws(SQLException::class)
    fun forgetFood(db: SqlDatabase, f: Food) {
        require(f.objectSource === ObjectSource.DATABASE) { "Food ${f.indexName} is not in DB" }
        // delete nutrition data, foodQuantities, servings, then food

        // servings and nutrient values are deleted on cascade, so we only have to worry about foodquantities
        db.deleteByColumn(FoodPortion.table, FoodPortionTable.FOOD_ID, listOf(f.id))
        db.deleteByColumn(Ingredient.table, IngredientTable.FOOD_ID, listOf(f.id))
        deleteObject(db, f)
    }

    @Throws(SQLException::class)
    fun setSearchRelevanceForFoodType(db: SqlDatabase, foodType: FoodType, value: Int) {
        db.executeRawStatement(
            "UPDATE ${Food.table.name} SET ${FoodTable.SEARCH_RELEVANCE} = $value WHERE " +
                "${FoodTable.FOOD_TYPE} = '${foodType.niceName}'"
        )
    }


    @Throws(SQLException::class)
    fun deleteAllCompositeFoods(db: SqlDatabase) : Int {
        return db.deleteByColumn(Food.table, FoodTable.FOOD_TYPE, listOf(FoodType.COMPOSITE.niceName))
    }

    @Throws(SQLException::class)
    fun deleteAllIngredients(db: SqlDatabase) {
        db.clearTable(Ingredient.table)
    }

    @Throws(SQLException::class)
    fun deleteAllFoodPortions(db: SqlDatabase) {
        db.clearTable(FoodPortion.table)
    }


}