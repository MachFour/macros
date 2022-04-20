package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.commands
import com.machfour.macros.cli.commandsByName
import com.machfour.macros.core.CliConfig

class Help(config: CliConfig) : CommandImpl(config) {
    override val name = "help"
    override val usage = "Usage: ${config.programName} $name"

    override fun printHelp() {
        println("################################")
        println("## Max's nutrition calculator ##")
        println("################################")
        println()
        println("Available commands:")
        commands.forEach {
            if (it.isUserCommand) {
                println(it.name)
            }
        }
        println()
        println("For help using a particular command, run ${config.programName} $name <command>" +
                " or ${config.programName} <command> --$name")
    }

    override fun doAction(args: List<String>): Int {
        if (args.size >= 2) {
            // help on a particular action, if that action exists. Otherwise default help is printed
            commandsByName[args[1]]?.printHelp() ?: printHelp()
        } else {
            // no command specified
            printHelp()
        }
        return 0
    }

}