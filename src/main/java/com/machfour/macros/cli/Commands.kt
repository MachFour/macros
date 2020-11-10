package com.machfour.macros.cli

import com.machfour.macros.cli.modes.*
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.util.MiscUtils.javaTrim
import java.util.Collections;

/*
 * Helper class that holds static instances of all the other commands
 */
object Commands {
    // TODO the configs don't do anything yet - need to add into constructor
    private val COMMAND_CONSTRUCTORS = listOf<(MacrosConfig) -> Command>(
        { _ -> Help() },
        { _ -> Edit() },
        { _ -> Meals() },
        { _ -> ShowFood() },
        { _ -> AddFood() },
        { _ -> DeleteFood() },
        { _ -> Portion() },
        { _ -> NewMeal() },
        { _ -> Read() },
        { _ -> SearchFood() },
        { _ -> Total() },
        { _ -> Recipe() },
        { _ -> AllFoods() },
        { _ -> Import() },
        { _ -> Export() },
        { _ -> Restore() },
        { _ -> Init() },
        { _ -> InvalidCommand() },
        { _ -> NoArgs() }
    )
    private var initialised = false

    lateinit var commandsByName: Map<String, Command>
        private set

    // and then init commands
    fun initCommands(config: MacrosConfig) {
        // TODO put config into constructor
        CommandImpl.defaultConfig = { config }
        val tmpCommandsByName = LinkedHashMap<String, Command>()
        COMMAND_CONSTRUCTORS.forEach { it(config).let { cmd ->
            assert(!tmpCommandsByName.containsKey(cmd.name)) { "Two commands have the same name" }
            tmpCommandsByName[cmd.name] = cmd
        } }
        commandsByName = Collections.unmodifiableMap(tmpCommandsByName)
        initialised = true
    }

    private fun checkInitialised() {
        assert(initialised) { "Commands not initialised" }
    }

    private fun cleanInput(s: String): String {
        return when (s.javaTrim().startsWith("--")) {
            true -> s.substring(2)
            else -> s
        }
    }

    val commands: Collection<Command>
        get() {
            checkInitialised()
            return commandsByName.values
        }

    /*
     * Parses the name of a command from the first element of args
     */
    fun parseCommand(cmdArg: String): Command {
        checkInitialised()
        return commandsByName.getOrDefault(cleanInput(cmdArg), invalidCommand())
    }

    fun noArgsCommand(): Command {
        checkInitialised()
        return commandsByName.getValue(NoArgs.NAME)
    }

    private fun invalidCommand(): Command {
        checkInitialised()
        return commandsByName.getValue(InvalidCommand.NAME)
    }

    /* Some more miscellaneous commands that don't (yet?) warrant their own class */
    private class InvalidCommand internal constructor() : CommandImpl(NAME) {
        companion object {
            const val NAME = "_invalidCommand"
        }

        override fun doAction(args: List<String>): Int {
            out.println("Command not recognised: '${args[0]}%s'\n")
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