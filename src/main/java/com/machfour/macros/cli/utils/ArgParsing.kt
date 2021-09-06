package com.machfour.macros.cli.utils

import com.machfour.macros.util.DateStamp
import com.machfour.macros.util.DateStamp.Companion.currentDate

// Utility functions for argument parsing

/*
 * Generic function to parse an argument from a commandline
 * parser must satisfy the following properties:
 *     returns null if argument has bad format
 *     returns non-null for otherwise valid arguments
 *     when called with null, returns a default value
 */
private fun <T> parseArgument(args: List<String>, flag: String, parser: (String?) -> T?): T? {
    return when (val r = findArgumentFromFlag(args, flag)) {
        is ArgParsingResult.KeyValFound -> parser(r.argument)
        is ArgParsingResult.ArgFound -> parser(r.flag)
        else -> parser(null)
    }
}

fun parseDate(args: List<String>, flag: String): DateStamp? {
    return parseArgument(args, flag) { dayStringParse(it) }
}

fun findArgumentFromFlag(args: List<String>, flag: String): ArgParsingResult {
    val flagIndex = args.indexOf(flag)
    val detectedIndex = flagIndex + 1
    return when {
        // indexOf returned -1
        detectedIndex == 0 -> ArgParsingResult.ArgNotFound
        detectedIndex >= args.size -> ArgParsingResult.ValNotFound(flagIndex, flag)
        else -> ArgParsingResult.KeyValFound(detectedIndex, flag, args[detectedIndex])
    }
}

// just attempts to use the given argument index
fun findArgument(args: List<String>, argIdx: Int): ArgParsingResult {
    assert(argIdx >= 0)
    return if (argIdx < args.size) {
        ArgParsingResult.ArgFound(argIdx, args[argIdx])
    } else {
        // info not provided, so return NOT_FOUND
        ArgParsingResult.ArgNotFound
    }
}

// returns null for invalid, today if flag not found, or otherwise decodes day from argument string
fun dayStringParse(dayString: String?): DateStamp? {
    // default values
    if (dayString == null) {
        return currentDate()
    }
    try {
        // enter day as '-1' for yesterday, '0' for today, '1' for tomorrow, etc.
        val daysAgo = dayString.toInt()
        return DateStamp.forDaysAgo(-daysAgo.toLong())
    } catch (ignore: NumberFormatException) {
    }
    try {
        return DateStamp.fromIso8601String(dayString)
    } catch (ignore: IllegalArgumentException) { }
    // invalid
    return null
}

// represents result of parsed argument from command line
sealed class ArgParsingResult {
    class ArgFound(val index: Int, val flag: String): ArgParsingResult()
    object ArgNotFound : ArgParsingResult()
    class KeyValFound(val argIndex: Int, val flag: String, val argument: String): ArgParsingResult()
    // for flags with extra arguments; when the flag is found but there is no extra arg
    class ValNotFound(val flagIndex: Int, val flag: String): ArgParsingResult()
}