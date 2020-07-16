package com.machfour.macros.cli

import com.machfour.macros.cli.modes.*
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.util.MiscUtils.javaTrim

/*
 * Helper class that holds static instances of all the other commands
 */
object Commands {
    private val CMDS_BY_NAME: MutableMap<String, Command> = LinkedHashMap()
    private val COMMAND_CONSTRUCTORS = listOf(
        { Help() },
        { Edit() },
        { Meals() },
        { ShowFood() },
        { AddFood() },
        { DeleteFood() },
        { Portion() },
        { NewMeal() },
        { Read() },
        { SearchFood() },
        { Total() },
        { Recipe() },
        { AllFoods() },
        { Import() },
        { Export() },
        { Restore() },
        { Init() },
        { InvalidCommand() },
        { NoArgs() }
    )
    private var initialised = false

    // and then init commands
    fun initCommands(config: MacrosConfig) {
        // TODO put config into constructor
        CommandImpl.defaultConfig = { config }
        COMMAND_CONSTRUCTORS.forEach {constructor ->
            val command = constructor()
            assert(!CMDS_BY_NAME.containsKey(command.name)) { "Two commands have the same name" }
            CMDS_BY_NAME[command.name] = command
        }
        initialised = true
    }

    private fun checkInitialised() {
        check(initialised) { "Commands not initialised" }
    }

    @JvmStatic
    fun getCommandByName(name: String): Command? {
        checkInitialised()
        return CMDS_BY_NAME.getOrDefault(name, null)
    }

    private fun cleanInput(s: String): String {
        return when (s.javaTrim().startsWith("--")) {
            true -> s.substring(2)
            else -> s
        }
    }

    @JvmStatic
    val commands: List<Command>
        get() {
            checkInitialised()
            return CMDS_BY_NAME.values.toList()
        }

    /*
     * Parses the name of a command from the first element of args
     */
    fun parseCommand(cmdArg: String): Command {
        checkInitialised()
        return CMDS_BY_NAME.getOrDefault(cleanInput(cmdArg), invalidCommand())
    }

    fun noArgsCommand(): Command {
        checkInitialised()
        return getCommandByName(NoArgs.NAME)!!
    }

    private fun invalidCommand(): Command {
        checkInitialised()
        return getCommandByName(InvalidCommand.NAME)!!
    }

    /* Some more miscellaneous commands that don't (yet?) warrant their own class */
    private class InvalidCommand internal constructor() : CommandImpl(NAME) {
        companion object {
            const val NAME = "_invalidCommand"
        }

        override fun doAction(args: List<String>): Int {
            out.printf("Command not recognised: '%s'\n\n", args[0])
            return noArgsCommand().doAction(emptyList())
        }
    }

    private class NoArgs internal constructor() : CommandImpl(NAME) {
        companion object {
            const val NAME = "_noArgs"
        }

        override fun doAction(args: List<String>): Int {
            out.println("Please specify one of the following commands:")
            for (m in commands) {
                if (m.isUserCommand) {
                    out.println(m.name)
                }
            }
            return -1
        }
    }
}