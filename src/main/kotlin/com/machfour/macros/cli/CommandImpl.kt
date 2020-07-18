package com.machfour.macros.cli

import com.machfour.macros.core.DummyConfig
import com.machfour.macros.core.MacrosConfig
import java.io.BufferedReader
import java.io.PrintStream


abstract class CommandImpl protected constructor(final override val name: String, usage: String? = null) : Command {

    final override val usage = usage ?: "No help available for mode '${name}'"
    // have to initialise this first with overwriteConfig

    protected var config: MacrosConfig = defaultConfig()
    protected var out: PrintStream = config.outStream
    protected var err: PrintStream = config.errStream
    protected var input: BufferedReader = config.inputReader


    // can be overridden
    override fun doActionNoExitCode(args: List<String>) {
        doAction(args)
    }

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

        private val dummyConfig = DummyConfig()

        var defaultConfig: () -> MacrosConfig = { dummyConfig }

        // TODO this is a hack for now
        val programName: String = "macros"

    }

}