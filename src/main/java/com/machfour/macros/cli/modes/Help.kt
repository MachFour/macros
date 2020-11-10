package com.machfour.macros.cli.modes

import com.machfour.macros.cli.CommandImpl
import com.machfour.macros.cli.Commands

class Help : CommandImpl(NAME, USAGE) {
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
        Commands.commands.filter { it.isUserCommand }.forEach { out.println(it.name) }
        out.println()
        val (pgmNm, nm) = arrayOf(programName, NAME)
        out.println("For help using a particular command, run ${pgmNm} ${nm} <command> or ${pgmNm} <command> --${nm}")
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