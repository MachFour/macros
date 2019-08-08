package com.machfour.macros.cli;

import com.machfour.macros.util.DateStamp;
import com.machfour.macros.util.Function;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Utility class for argument parsing
class ArgParsing {
    private ArgParsing() {}

    enum Status {
        ARG_FOUND, NOT_FOUND, OPT_ARG_MISSING
    }

    // represents result of parsed argument from command line
    static class Result {
        private final int index;
        private final String argument;
        private final Status status;

        Result(int index, String argument, Status status) {
            this.index = index;
            this.argument = argument;
            this.status = status;
        }
        Status status() {
            return status;
        }
        int index() {
            return index;
        }
        String argument() {
            return argument;
        }

    }

    /*
     * Generic function to parse an argument from a commandline
     * parser must satisfy the following properties:
     *     returns null if argument has bad format
     *     returns non-null for otherwise valid arguments
     *     when called with null, returns a default value
     */
    private static <T> T parseArgument(List<String> args, String flag, Function<String, T> parser) {
        Result r = findArgument(args, flag);
        return parser.apply(r.argument());

    }

    static DateStamp parseDate(List<String> args, String flag) {
        return parseArgument(args, flag, ArgParsing::dayStringParse);
    }

    static Result findArgument(List<String> args, String flag) {
        int detectedIndex = args.indexOf(flag) + 1;
        String argument;
        int index;
        Status status;

        if (detectedIndex == 0) {
            // indexOf returned -1
            argument = null;
            index = -1;
            status = Status.NOT_FOUND;
        } else if (detectedIndex >= args.size()) {
            argument = null;
            index = -1;
            status = Status.OPT_ARG_MISSING;
        } else {
            argument = args.get(detectedIndex);
            index = detectedIndex;
            status = Status.ARG_FOUND;
        }
        return new Result(index, argument, status);
    }
    // just attempts to use the given argument index
    static Result findArgument(List<String> args, int argIdx) {
        assert argIdx >= 0;
        String argument;
        Status status;
        if (argIdx < args.size()) {
            argument = args.get(argIdx);
            status = Status.ARG_FOUND;
        } else {
            // info not provided, so return NOT_FOUND
            argument = null;
            status = Status.NOT_FOUND;
        }
        return new Result(argIdx, argument, status);
    }

    // returns null for invalid, today if flag not found, or otherwise decodes day from argument string
    @Nullable
    static DateStamp dayStringParse(@Nullable String dayString) {
        // default values
        if (dayString == null) {
            return DateStamp.forCurrentDate();
        }
        try {
            // enter day as '-1' for yesterday, '0' for today, '1' for tomorrow, etc.
            int daysAgo = Integer.parseInt(dayString);
            return DateStamp.forDaysAgo(-daysAgo);
        } catch (NumberFormatException ignore) {}
        try {
            return DateStamp.fromIso8601String(dayString);
        } catch (IllegalArgumentException ignore) {}
        // invalid
        return null;
    }

}
