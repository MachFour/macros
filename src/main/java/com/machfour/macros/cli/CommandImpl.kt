package com.machfour.macros.cli

import com.machfour.macros.core.DummyConfig
import com.machfour.macros.core.MacrosConfig
import java.io.BufferedReader
import java.io.PrintStream


abstract class CommandImpl @JvmOverloads protected constructor(final override val name: String, usage: String? = null) : Command {

    final override val usage = usage ?: "No help available for mode '${name}'"

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
        @JvmStatic
        fun overwriteConfig(newConfig: MacrosConfig) {
            newConfig.let {
                config = it
                out = it.outStream
                err = it.errStream
                `in` = it.inputReader
            }
        }

        @JvmField
        // have to initialise this first with overwriteConfig
        protected var config: MacrosConfig = DummyConfig()

        @JvmField
        protected var out: PrintStream = config.outStream
        @JvmField
        protected var err: PrintStream = config.errStream
        @JvmField
        protected var `in`: BufferedReader = config.inputReader

        // logic for deciding whether a command is user-facing
        @JvmStatic
        fun isUserCommand(name: String): Boolean {
            return !name.startsWith("_")
        }
    }

}