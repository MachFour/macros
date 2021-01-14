package com.machfour.macros.cli

import com.machfour.macros.core.MacrosConfig
import java.io.BufferedReader
import java.io.PrintStream


abstract class CommandImpl protected constructor(
    final override val name: String,
    final override val usage: String = "No help available for mode '${name}'",
    val config: MacrosConfig,
) : Command {

    // have to initialise this first with overwriteConfig

    protected val out: PrintStream
        get() = config.outStream
    protected val err: PrintStream
        get() = config.errStream
    protected val input: BufferedReader
        get() = config.inputReader

    /*
     * Subclasses should override this to provide more detailed help than just the usage string
     */
    override fun printHelp() {
        out.println(usage)
    }

    override fun toString(): String {
        return name
    }

    override val isUserCommand = isUserCommand(name)


    companion object {
        // logic for deciding whether a command is user-facing
        fun isUserCommand(name: String): Boolean {
            return !name.startsWith("_")
        }

        // TODO this is a hack for now
        const val programName: String = "macros"

    }

}