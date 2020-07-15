package com.machfour.macros.cli

interface Command {
    // sets up file paths, etc
    //void setConfig(@NotNull MacrosConfig config);
    // name, by which the mode can be specified on the command line
    val name: String
    // concise usage string representing the program's command line options
    val usage: String
    // whether or not the mode should be shown to users / called from the command line
    val isUserCommand: Boolean

    // does the action for the mode. Arguments are given in the array passed.
    // args[0] is kept the same as that in the original program args,
    // so mode-specific args start at args[1] (if present)
    fun doActionNoExitCode(args: List<String>)

    // same as above but returns an exit code
    fun doAction(args: List<String>): Int {
        doActionNoExitCode(args)
        return 0
    }

    // help message, which defaults to printing the usage string, if not null
    fun printHelp()
}