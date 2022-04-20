package com.machfour.macros.cli

interface Command {
    // name by which the mode can be specified on the command line
    val name: String
    // Concise usage string representing the program's command line options
    val usage: String

    // Whether the mode should be shown to users / called from the command line
    val isUserCommand: Boolean

    // does the action for the mode. Arguments are given in the array passed.
    // args[0] is kept the same as that in the original program args,
    // so mode-specific args start at args[1] (if present)
    fun doAction(args: List<String>): Int

    // help message, which defaults to printing the usage string
    fun printHelp()
}