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
        val r = findArgument(args, flag)
        return parser(r.argument)
    }

    fun parseDate(args: List<String>, flag: String): DateStamp? {
        return parseArgument(args, flag) { dayStringParse(it) }
    }

    fun findArgument(args: List<String>, flag: String?): Result {
        val detectedIndex = args.indexOf(flag) + 1
        val argument: String?
        val index: Int
        val status: Status
        when {
            detectedIndex == 0 -> {
                // indexOf returned -1
                argument = null
                index = -1
                status = Status.NOT_FOUND
            }
            detectedIndex >= args.size -> {
                argument = null
                index = -1
                status = Status.OPT_ARG_MISSING
            }
            else -> {
                argument = args[detectedIndex]
                index = detectedIndex
                status = Status.ARG_FOUND
            }
        }
        return Result(index, argument, status)
    }

    // just attempts to use the given argument index
    fun findArgument(args: List<String>, argIdx: Int): Result {
        assert(argIdx >= 0)
        val argument: String?
        val status: Status
        if (argIdx < args.size) {
            argument = args[argIdx]
            status = Status.ARG_FOUND
        } else {
            // info not provided, so return NOT_FOUND
            argument = null
            status = Status.NOT_FOUND
        }
        return Result(argIdx, argument, status)
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

    enum class Status {
        ARG_FOUND,  // arg was found either by index or after flag
        NOT_FOUND,  // arg not found by index (out of range) or flag not present
        OPT_ARG_MISSING // flag found but no corresponding argument (out of range)
    }

    // represents result of parsed argument from command line
    data class Result constructor(val index: Int, val argument: String?, val status: Status)
}