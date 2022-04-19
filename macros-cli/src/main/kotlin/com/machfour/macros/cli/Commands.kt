package com.machfour.macros.cli

import com.machfour.macros.cli.modes.*
import com.machfour.macros.core.MacrosConfig
import com.machfour.macros.util.javaTrim

// File that holds static instances of all the other commands

private val COMMAND_CONSTRUCTORS = listOf<(MacrosConfig) -> com.machfour.macros.cli.Command>(
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
    { config -> Backup(config) },
    { config -> Restore(config) },
    { config -> Init(config) },
    // these ones don't need configs
    { config -> InvalidCommand(config) },
    { config -> NoArgs(config) }
)

private var initialised = false

private fun checkInitialised() {
    assert(initialised) { "Commands not initialised" }
}

private fun String.clean(): String {
    return when (javaTrim().startsWith("--")) {
        true -> substring(2)
        else -> this
    }
}

lateinit var COMMANDS_BY_NAME: Map<String, com.machfour.macros.cli.Command>
    private set

// and then init commands
fun initCommands(config: MacrosConfig) {
    COMMANDS_BY_NAME = COMMAND_CONSTRUCTORS.associate { constructor ->
        constructor(config).let { it.name to it }
    }
    initialised = true
}

val commands: Collection<com.machfour.macros.cli.Command>
    get() {
        checkInitialised()
        return COMMANDS_BY_NAME.values
    }


// Parses the name of a command from the first element of args
fun parseCommand(cmdArg: String): com.machfour.macros.cli.Command {
    checkInitialised()
    return COMMANDS_BY_NAME.getOrDefault(cmdArg.clean(), invalidCommand())
}

fun noArgsCommand(): com.machfour.macros.cli.Command {
    checkInitialised()
    return COMMANDS_BY_NAME.getValue(NoArgs.NAME)
}

private fun invalidCommand(): com.machfour.macros.cli.Command {
    checkInitialised()
    return COMMANDS_BY_NAME.getValue(InvalidCommand.NAME)
}

//
// Some more miscellaneous commands that don't (yet?) warrant their own class
//
private class InvalidCommand(config: MacrosConfig): com.machfour.macros.cli.CommandImpl(NAME, config = config) {
    companion object {
        const val NAME = "_invalidCommand"
    }

    override fun doAction(args: List<String>): Int {
        println("Command not recognised: '${args[0]}'\n")
        return noArgsCommand().doAction(emptyList())
    }
}

private class NoArgs(config: MacrosConfig): com.machfour.macros.cli.CommandImpl(NAME, config = config) {
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
