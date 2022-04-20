package com.machfour.macros.cli

import com.machfour.macros.cli.modes.*
import com.machfour.macros.util.javaTrim

// File that holds static instances of all the other commands

private val commandConstructors = listOf<(CliConfig) -> Command>(
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
    { config -> Version(config) },
    { config -> InvalidCommand(config) },
    { config -> NoArgs(config) }
)

private var initialised = false

private fun checkInitialised() {
    check(initialised) { "Commands not initialised" }
}

private fun String.clean(): String {
    val trimmed = javaTrim()
    return when (trimmed.startsWith("--")) {
        true -> trimmed.substring(2)
        else -> trimmed
    }
}

lateinit var commandsByName: Map<String, Command>
    private set

// and then init commands
fun initCommands(config: CliConfig) {
    commandsByName = commandConstructors.associate { constructor ->
        constructor(config).let { it.name to it }
    }
    initialised = true
}

val commands: Collection<Command>
    get() {
        checkInitialised()
        return commandsByName.values
    }

// Parses the name of a command from the first element of args
fun parseCommand(cmdArg: String?): Command {
    checkInitialised()
    val commandName = when (val it = cmdArg?.clean()) {
        null -> noArgsCommandName
        in commandsByName -> it
        else -> invalidCommandName
    }
    return commandsByName.getValue(commandName)
}

private const val invalidCommandName = "_invalidCommand"
private const val noArgsCommandName = "_noArgs"

//
// Some more miscellaneous commands that don't (yet?) warrant their own class
//
private class InvalidCommand(config: CliConfig): CommandImpl(config) {
    override val name: String = invalidCommandName

    override fun doAction(args: List<String>): Int {
        println("Command not recognised: '${args[0]}'\n")
        return commandsByName.getValue(noArgsCommandName).doAction(emptyList())
    }
}

private class NoArgs(config: CliConfig): CommandImpl(config) {
    override val name: String = noArgsCommandName

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
