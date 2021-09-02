package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.Commands
import com.machfour.macros.core.MacrosConfig

class Help(config: MacrosConfig) : CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "help"
        private const val USAGE = "Usage: $programName $NAME"
    }

    override fun printHelp() {
        println("################################")
        println("## Max's nutrition calculator ##")
        println("################################")
        println()
        println("Available commands:")
        Commands.commands.forEach {
            if (it.isUserCommand) {
                println(it.name)
            }
        }
        println()
        println("For help using a particular command, run $programName $NAME <command>" +
                " or $programName <command> --$NAME")
    }

    override fun doAction(args: List<String>): Int {
        if (args.size >= 2) {
            // help on a particular action, if that action exists. Otherwise default help is printed
            Commands.commandsByName[args[1]]?.printHelp() ?: printHelp()
        } else {
            // no command specified
            printHelp()
        }
        return 0
    }

}