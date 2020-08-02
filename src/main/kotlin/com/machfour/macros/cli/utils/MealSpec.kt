package com.machfour.macros.cli.utils

import com.machfour.macros.cli.utils.ArgParsing.dayStringParse
import com.machfour.macros.cli.utils.ArgParsing.findArgument
import com.machfour.macros.objects.Meal
import com.machfour.macros.queries.MealQueries
import com.machfour.macros.storage.MacrosDataSource
import com.machfour.macros.util.DateStamp
import com.machfour.macros.util.DateStamp.Companion.prettyPrint
import java.sql.SQLException

/*
 * Stores information for a meal being input (specified) via text fields,
 * and implements default behaviour for retrieving the meal when not
 * all fields are fully specified
 */
class MealSpec {
    val name: String?
    val day: DateStamp?

    // whether parameters were actually given (true) or the default value used (false)
    val isMealSpecified: Boolean
        get() = name != null

    // not just day != null - if no day was given we use current date as default.
    val isDaySpecified: Boolean

    var processedObject: Meal? = null
        private set

    // whether the processedObject was newly created or already existed in the DB
    var isCreated = false
        private set

    var error: String? = null
        private set

    // has process() been called?
    private var processed = false


    private constructor(name: String?, day: DateStamp?) {
        this.name = name
        this.day = day
        this.isDaySpecified = day != null
    }

    private constructor(name: String?, dayString: String?) {
        val day = dayStringParse(dayString)
        if (day == null) {
            error = String.format("Invalid day format: '%s'. ", dayString)
            error += "Must be a number (e.g. 0 for today, -1 for yesterday), or a date: yyyy-mm-dd"
        }
        this.name = name
        this.day = day
        this.isDaySpecified = day != null && dayString != null
    }

    private constructor(mealArg: ArgParsing.Result, dayArg: ArgParsing.Result) : this(mealArg.argument, dayArg.argument) {
        if (dayArg.status === ArgParsing.Status.OPT_ARG_MISSING) {
            error = "-d option requires an argument: <day>"
        } else if (mealArg.status === ArgParsing.Status.OPT_ARG_MISSING) {
            error = "-m option requires an argument: <meal>"
        }
    }

    fun process(ds: MacrosDataSource, create: Boolean) {
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
        val mealsForDay: Map<Long, Meal>
        try {
            mealsForDay = MealQueries.getMealsForDay(ds, day)
        } catch (e: SQLException) {
            error = "Error retrieving meals for day ${day}: $e"
            return
        }
        if (!isMealSpecified) {
            if (mealsForDay.isNotEmpty()) {
                // use most recently modified meal today
                processedObject = mealsForDay.values.maxByOrNull { it.modifyTime } !!
            } else {
                error = "No meals recorded on " + prettyPrint(day)
            }
        } else {
            // if a name was given, try to find a matching meal with that name
            // name cannot be null since it is implied by isMealSpecified
            val nameMatch = MealQueries.findMealWithName(mealsForDay, name!!)
            when {
                nameMatch != null -> {
                    processedObject = nameMatch
                    isCreated = false
                }
                create -> {
                    try {
                        processedObject = MealQueries.getOrCreateMeal(ds, day, name)
                        isCreated = true
                    } catch (e: SQLException) {
                        error = "Error retrieving meal: " + e.message
                        return
                    }
                }
                else -> {
                    // meal doesn't exist and not allowed to create new meal
                    error = "No meal with name '$name' found on ${prettyPrint(day)}"
                }
            }
        }
        assert(error != null || processedObject != null) { "No error message but no created object" }
        if (error != null) {
            return
        }
    }


    companion object {
        // extracts a meal specification from the argument list using the following rules:
        // -d <day> specifies a day to search for, or on which to create the meal if create = true
        // -m <name> specifies a name for the meal, which is created if create = true and it doesn't already exist.
        // Both options can be omitted under certain condititions:
        // If -d is omitted then the current day is used.
        // If there are no meals recorded for the day, then an error is given.
        fun makeMealSpec(args: List<String>): MealSpec {
            val dayArg: ArgParsing.Result = findArgument(args, "-d")
            val mealArg: ArgParsing.Result = findArgument(args, "-m")
            return MealSpec(dayArg, mealArg)
        }

        fun makeMealSpec(name: String? = null, dayString: String? = null): MealSpec {
            val day = dayStringParse(dayString)
            return MealSpec(name, day)
        }

        fun makeMealSpec(nameArg: ArgParsing.Result, dayArg: ArgParsing.Result): MealSpec {
            return MealSpec(nameArg, dayArg)
        }
    }
}