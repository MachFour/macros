package com.machfour.macros.cli;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Command {

    // sets up file paths, etc
    //void setConfig(@NotNull MacrosConfig config);

    // name, by which the mode can be specified on the command line
    @NotNull
    String name();
    // does the action for the mode. Arguments are given in the array passed.
    // args[0] is kept the same as that in the original program args,
    // so mode-specific args start at args[1] (if present)
    void doActionNoExitCode(List<String> args);

    // same as above but returns an exit code
    default int doAction(List<String> args) {
        doActionNoExitCode(args);
        return 0;
    }


    // concise usage string representing the program's command line options
    @NotNull
    String usage();

    // help message, which defaults to printing the usage string, if not null
    void printHelp();

    // whether or not the mode should be shown to users / called from the command line
    boolean isUserCommand();
}
