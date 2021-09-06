package com.machfour.macros.cli

import com.machfour.macros.cli.modes.*
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.util.javaTrim

// Helper class that holds static instances of all the other commands
object Commands {
    private val COMMAND_CONSTRUCTORS = listOf<(MacrosConfig) -> Command>(
        { config -> Help(config) },
        { config -> Edit(config) },
        { config -> Meals(config) },
        { config -> ShowFood(config) },
        { config -> AddFood(config) },
        { config -> DeleteFood(config) },
        { config -> Portion(config) },
        { config -> NewMeal(config) },
        { config -> Read(config) },
        { config -> SearchFood(config) },
        { config -> Total(config) },
        { config -> Recipe(config) },
        { config -> AllFoods(config) },
        { config -> Import(config) },
        { config -> Export(config) },
        { config -> Restore(config) },
        { config -> Init(config) },
        // these ones don't need configs
        { config -> InvalidCommand(config) },
        { config -> NoArgs(config) }
    )
    private var initialised = false

    lateinit var commandsByName: Map<String, Command>
        private set

    // and then init commands
    fun initCommands(config: MacrosConfig) {
        commandsByName = LinkedHashMap<String, Command>().apply {
            for (cmdConstructor in COMMAND_CONSTRUCTORS) {
                cmdConstructor(config).let {
                    assert(!this.containsKey(it.name)) { "Two commands have the same name" }
                    this[it.name] = it
                }
            }
        }
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
    private class InvalidCommand(config: MacrosConfig): CommandImpl(NAME, config = config) {
        companion object {
            const val NAME = "_invalidCommand"
        }

        override fun doAction(args: List<String>): Int {
            println("Command not recognised: '${args[0]}'\n")
            return noArgsCommand().doAction(emptyList())
        }
    }

    private class NoArgs(config: MacrosConfig): CommandImpl(NAME, config = config) {
        companion object {
            const val NAME = "_noArgs"
        }

        override fun doAction(args: List<String>): Int {
            println("Please specify one of the following commands:")
            for (m in commands) {
                if (m.isUserCommand) {
                    println(m.name)
                }
            }
            return -1
        }
    }
}