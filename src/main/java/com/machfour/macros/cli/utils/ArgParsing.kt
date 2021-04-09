package com.machfour.macros.cli.utils

import com.machfour.macros.util.DateStamp
import com.machfour.macros.util.DateStamp.Companion.currentDate

// Utility class for argument parsing
object ArgParsing {
    /*
     * Generic function to parse an argument from a commandline
     * parser must satisfy the following properties:
     *     returns null if argument has bad format
     *     returns non-null for otherwise valid arguments
     *     when called with null, returns a default value
     */
    private fun <T> parseArgument(args: List<String>, flag: String, parser: (String?) -> T?): T? {
        return when (val r = findArgumentFromFlag(args, flag)) {
            is Result.KeyValFound -> parser(r.argument)
            is Result.ArgFound -> parser(r.flag)
            else -> parser(null)
        }
    }

    fun parseDate(args: List<String>, flag: String): DateStamp? {
        return parseArgument(args, flag) { dayStringParse(it) }
    }

    fun findArgumentFromFlag(args: List<String>, flag: String): Result {
        val flagIndex = args.indexOf(flag)
        val detectedIndex = flagIndex + 1
        return when {
            // indexOf returned -1
            detectedIndex == 0 -> Result.ArgNotFound
            detectedIndex >= args.size -> Result.ValNotFound(flagIndex, flag)
            else -> Result.KeyValFound(detectedIndex, flag, args[detectedIndex])
        }
    }

    // just attempts to use the given argument index
    fun findArgument(args: List<String>, argIdx: Int): Result {
        assert(argIdx >= 0)
        return if (argIdx < args.size) {
            Result.ArgFound(argIdx, args[argIdx])
        } else {
            // info not provided, so return NOT_FOUND
            Result.ArgNotFound
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
    sealed class Result {
        class ArgFound(val index: Int, val flag: String): Result()
        object ArgNotFound : Result()
        class KeyValFound(val argIndex: Int, val flag: String, val argument: String): Result()
        // for flags with extra arguments; when the flag is found but there is no extra arg
        class ValNotFound(val flagIndex: Int, val flag: String) : Result()
    }

}