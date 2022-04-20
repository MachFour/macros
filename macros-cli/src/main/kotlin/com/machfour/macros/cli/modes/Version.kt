package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.core.CliConfig


class Version(config: CliConfig) : CommandImpl(config) {
    override val name: String = "version"
    override val usage: String = noArgsUsage

    override fun doAction(args: List<String>): Int {
        if (args.contains("--help")) {
            printHelp()
            return 0
        }

        println("Version ${config.programVersion}")
        return 0
    }

}
