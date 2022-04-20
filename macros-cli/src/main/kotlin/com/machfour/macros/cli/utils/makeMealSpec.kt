package com.machfour.macros.cli.utils

import com.machfour.macros.parsing.MealSpec

// extracts a meal specification from the argument list using the following rules:
// -d <day> specifies a day to search for, or on which to create the meal if create = true
// -m <name> specifies a name for the meal, which is created if create = true and it doesn't already exist.
// Both options can be omitted under certain condititions:
// If -d is omitted then the current day is used.
// If there are no meals recorded for the day, then an error is given.

fun makeMealSpec(name: String? = null, dayString: String? = null): MealSpec {
    val day = dayStringParse(dayString)
    return MealSpec(name, day)
}

fun makeMealSpec(nameArg: ArgParsingResult, dayArg: ArgParsingResult): MealSpec {
    return when {
        nameArg !is ArgParsingResult.KeyValFound ->
            MealSpec(null, null, error = "-d option requires an argument: <day>")
        dayArg !is ArgParsingResult.KeyValFound ->
            MealSpec(null, null, error = "-m option requires an argument: <meal>")
        else -> {
            val name = nameArg.argument
            val dayString = dayArg.argument
            val day = dayStringParse(dayString)
            if (day != null) {
                MealSpec(name, day)
            } else {
                MealSpec(name, null, invalidDayMsg(dayString))
            }
        }
    }
}

private fun invalidDayMsg(dayString: String) = "Invalid day format: '$dayString'. " +
        "Must be a number (e.g. 0 for today, -1 for yesterday), or a date: yyyy-mm-dd"
