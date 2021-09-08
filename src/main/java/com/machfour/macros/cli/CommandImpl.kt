package com.machfour.macros.cli

import com.machfour.macros.core.MacrosConfig


abstract class CommandImpl protected constructor(
    final override val name: String,
    final override val usage: String = "No help available for mode '${name}'",
    val config: MacrosConfig,
): Command {
    
    // Subclasses should override this to provide more detailed help than just the usage string
    override fun printHelp() {
        println(usage)
    }

    override fun toString() = name

    override val isUserCommand: Boolean
        get() = isUserCommand(name)

    companion object {
        // TODO this is a hack for now
        const val programName: String = "macros"

        // logic for deciding whether a command is user-facing
        fun isUserCommand(name: String): Boolean {
            return !name.startsWith("_")
        }
    }
}