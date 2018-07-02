package com.machfour.macros.cli;

import java.io.PrintStream;
import java.util.List;

interface Mode {
    // name, by which the mode can be specified on the command line
    String name();
    // does the action for the mode. Arguments are given in the array passed.
    // args[0] is kept the same as that in the original program args,
    // so mode-specific args start at args[1] (if present)
    void doAction(List<String> args);

    // help message, which defaults to generic help
    default void printHelp(PrintStream out) {
        out.printf("No help available for mode '%s'\n", name());
    }

    // whether or not the mode should be shown to users / called from the command line
    boolean isUserMode();
}
