package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.Commands
import com.machfour.macros.core.MacrosConfig

class Help(config: MacrosConfig) : CommandImpl(NAME, USAGE, config) {
    companion object {
        private const val NAME = "help"
        private val USAGE = "Usage: $programName $NAME"
    }

    override fun printHelp() {
        out.println("################################")
        out.println("## Max's nutrition calculator ##")
        out.println("################################")
        out.println()
        out.println("Available commands:")
        Commands.commands.forEach {
            if (it.isUserCommand) {
                out.println(it.name)
            }
        }
        out.println()
        out.println("For help using a particular command, run $programName $NAME <command>" +
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