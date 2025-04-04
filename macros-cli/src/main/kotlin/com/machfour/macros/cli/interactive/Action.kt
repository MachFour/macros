package com.machfour.macros.cli.interactive

internal enum class Action(val niceName: String) {
    MORE_FIELDS("Extra fields"), //TODO
    SAVE("Save"),
    RESET("Reset"),
    EXIT("Exit"),
    HELP("Help");

    override fun toString(): String {
        return niceName
    }
}

