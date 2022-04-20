package com.machfour.macros.cli

import com.machfour.macros.core.CliConfig


abstract class CommandImpl protected constructor(val config: CliConfig): Command {
    abstract override val name: String

    override val usage: String
        get() = "No help available for mode '${name}'"

    // convenience
    protected val noArgsUsage
        get() = "Usage: ${config.programName} $name"

    // Subclasses should override this to provide more detailed help than just the usage string
    override fun printHelp() {
        println(usage)
    }

    override fun toString() = name

    // logic for deciding whether a command is user-facing
    override val isUserCommand: Boolean
        get() = !name.startsWith("_")
}