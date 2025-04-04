package com.machfour.macros.cli

import com.machfour.macros.cli.modes.*

private val commandConstructors = listOf(
    ::Help,
    ::Edit,
    ::Meals,
    ::ShowFood,
    ::AddFood,
    ::DeleteFood,
    ::Portion,
    ::NewMeal,
    ::Read,
    ::SearchFood,
    ::Total,
    ::Recipe,
    ::AllFoods,
    ::Import,
    ::Export,
    ::Backup,
    ::Restore,
    ::Json,
    ::Init,
    ::Version,
    ::InvalidCommand,
    ::NoArgs,
)

private fun String.clean(): String {
    val trimmed = trim()
    return when (trimmed.startsWith("--")) {
        true -> trimmed.substring(2)
        else -> trimmed
    }
}

private val commandsByNameInternal: MutableMap<String, Command> = HashMap()
val commandsByName: Map<String, Command>
    get() = commandsByNameInternal

val commands: Collection<Command>
    get() = commandsByName.values

// and then init commands
fun initCommands(config: CliConfig) {
    commandsByNameInternal.putAll(
        commandConstructors.map { it(config) }.associateBy { it.name }
    )
}

// Parses the name of a command from the first element of args
fun parseCommand(cmdArg: String?): Command {
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
