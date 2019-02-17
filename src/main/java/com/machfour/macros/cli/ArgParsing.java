package com.machfour.macros.cli;

import com.machfour.macros.util.DateStamp;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Utility class for argument parsing
public class ArgParsing {
    private ArgParsing() {}

    enum Status {
        PARSE_OK, NO_FLAG, NO_ARG
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
    static Result parseArgument(List<String> args, String flag) {
        int detectedIndex = args.indexOf(flag) + 1;
        String input;
        int index;
        Status status;

        if (detectedIndex == 0) {
            // indexOf returned -1
            input = null;
            index = -1;
            status = Status.NO_FLAG;
        } else if (detectedIndex >= args.size()) {
            input = null;
            index = -1;
            status = Status.NO_ARG;
        } else {
            input = args.get(detectedIndex);
            index = detectedIndex;
            status = Status.PARSE_OK;
        }
        return new Result(index, input, status);
    }
    // just naively assumes that the given argument index is the correct one
    static Result parseArgument(List<String> args, int argIdx) {
        assert argIdx >= 0 && argIdx < args.size();
        String input = args.get(argIdx);
        return new Result(argIdx, input, Status.PARSE_OK);
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
