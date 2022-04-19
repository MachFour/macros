package com.machfour.macros.cli.modes

import com.machfour.macros.cli.COMMANDS_BY_NAME
import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.commands
import com.machfour.macros.core.MacrosConfig

class Help(config: MacrosConfig) : com.machfour.macros.cli.CommandImpl(NAME, USAGE, config) {
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
        commands.forEach {
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
            COMMANDS_BY_NAME[args[1]]?.printHelp() ?: printHelp()
        } else {
            // no command specified
            printHelp()
        }
        return 0
    }

}