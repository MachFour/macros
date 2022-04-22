package com.machfour.macros.cli.utils

import com.machfour.datestamp.DateStamp
import com.machfour.datestamp.currentDateStamp
import com.machfour.datestamp.iso8601StringDateStamp
import com.machfour.datestamp.pastDateStamp

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
        else -> ArgParsingResult.KeyValFound(flagIndex, flag, args[detectedIndex])
    }
}

// just attempts to use the given argument index
fun findArgument(args: List<String>, argIdx: Int): ArgParsingResult {
    check(argIdx >= 0)
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
        return currentDateStamp()
    }
    try {
        // enter day as '-1' for yesterday, '0' for today, '1' for tomorrow, etc.
        val daysAgo = dayString.toInt()
        return pastDateStamp(-daysAgo.toLong())
    } catch (ignore: NumberFormatException) {
    }
    try {
        return iso8601StringDateStamp(dayString)
    } catch (ignore: IllegalArgumentException) {
    }
    // invalid
    return null
}

// represents result of parsed argument from command line
sealed class ArgParsingResult {
    class ArgFound(val index: Int, val flag: String): ArgParsingResult() {
        override fun toString() = "Arg found: $flag at index $index"
    }
    object ArgNotFound : ArgParsingResult() {
        override fun toString() = "Arg not found"
    }

    class KeyValFound(val flagIndex: Int, val flag: String, val argument: String): ArgParsingResult() {
        override fun toString() = "Flag $flag found at index $flagIndex with argument $argument"
    }
    // for flags with extra arguments; when the flag is found but there is no extra arg
    class ValNotFound(val flagIndex: Int, val flag: String): ArgParsingResult() {
        override fun toString() = "Flag $flag found at index $flagIndex, but no value found"

    }
}