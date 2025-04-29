package com.machfour.macros.parsing

import com.machfour.datestamp.DateStamp
import com.machfour.macros.core.EntityId
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.Meal
import com.machfour.macros.queries.getMealForDayWithName
import com.machfour.macros.queries.getMealsForDay
import com.machfour.macros.queries.saveObject
import com.machfour.macros.schema.MealTable
import com.machfour.macros.sql.SqlDatabase
import com.machfour.macros.sql.SqlException
import com.machfour.macros.sql.rowdata.RowData

/*
 * Stores information for a meal being input (specified) via text fields,
 * and implements default behaviour for retrieving the meal when not
 * all fields are fully specified
 */
class MealSpec(val name: String?, val day: DateStamp?, error: String? = null) {

    // whether parameters were actually given (true) or the default value used (false)
    val isMealSpecified: Boolean = name != null

    var processedObject: Meal? = null
        private set

    // whether the processedObject was newly created or already existed in the DB
    var isCreated = false
        private set

    var error: String? = error
        private set

    // has process() been called?
    private var processed = false

    @Throws(SqlException::class)
    private fun getOrCreateMeal(ds: SqlDatabase, day: DateStamp, name: String): Meal {
        // if it already exists return it
        val matchingMeal = getMealForDayWithName(ds, day, name)
        if (matchingMeal != null) {
            return matchingMeal
        }

        // else create a new meal, save and return it
        val newMeal = RowData(MealTable).run {
            put(MealTable.DAY, day)
            put(MealTable.NAME, name)
            Meal.factory.construct(this, ObjectSource.USER_NEW)
        }

        saveObject(ds, Meal.factory,newMeal)
        // get it back again, so that it has an ID and stuff
        val newlySavedMeal = getMealForDayWithName(ds, day, name)
        check(newlySavedMeal != null) { "Couldn't find newly saved meal in day $day" }
        return newlySavedMeal
    }

    fun process(ds: SqlDatabase, create: Boolean) {
        if (processed || error != null) {
            // skip processing if there are already errors
            return
        }
        processed = true // only let process() be called once

        if (day == null) {
            throw IllegalStateException("Day cannot be null once process() is called")
        }

        // cases:
        // no meal specified -> use current meal (exists)
        // no meal specified -> no meal exists
        // meal specified that exists -> use it
        // meal specified that does not exist -> create it
        val mealsForDay: Map<EntityId, Meal>
        try {
            mealsForDay = getMealsForDay(ds, day)
        } catch (e: SqlException) {
            error = "Error retrieving meals for day ${day}: $e"
            return
        }
        if (!isMealSpecified) {
            if (mealsForDay.isNotEmpty()) {
                // use most recently modified meal today
                processedObject = mealsForDay.values.maxByOrNull { it.modifyTime }!!
            } else {
                error = "No meals recorded on " + day.prettyPrint()
            }
        } else {
            // if a name was given, try to find a matching meal with that name
            // name cannot be null since it is implied by isMealSpecified
            val name = name!!
            val nameMatch = mealsForDay.values.firstOrNull { it.name == name }
            when {
                nameMatch != null -> {
                    processedObject = nameMatch
                    isCreated = false
                }
                create -> {
                    try {
                        processedObject = getOrCreateMeal(ds, day, name)
                        isCreated = true
                    } catch (e: SqlException) {
                        error = "Error retrieving meal: " + e.message
                        return
                    }
                }
                else -> {
                    // meal doesn't exist and not allowed to create new meal
                    error = "No meal with name '$name' found on ${day.prettyPrint()}"
                }
            }
        }
        check(error != null || processedObject != null) { "No error message but no created object" }
        if (error != null) {
            return
        }
    }


}