package com.machfour.macros.cli.utils

import com.machfour.macros.util.javaTrim
import java.io.IOException

private fun badNumberFormat(inputString: String?) {
    if (inputString != null) {
        printlnErr("Bad number format: '$inputString'")
    }
}


// command line inputs
// returns null if there was an error or input was invalid
fun cliGetInteger(min: Int, max: Int): Int? {
    val inputString = cliGetStringInput()
    return inputString?.toIntOrNull()?.takeIf { it in min..max }.also {
        if (it == null) {
            badNumberFormat(inputString)
        }
    }
}

// command line inputs
// returns null if there was an error or input was invalid
fun cliGetDouble(): Double? {
    val inputString = cliGetStringInput()
    return inputString?.toDoubleOrNull()?.takeIf { it.isFinite() }.also {
        if (it == null) {
            badNumberFormat(inputString)
        }
    }
}

fun cliGetStringInput(): String? {
    return try {
        readlnOrNull()?.javaTrim()
    } catch (e: IOException) {
        printlnErr("Error reading input: " + e.message)
        null
    }
}

fun clearTerminal() {
    // this is what /usr/bin/clear outputs on my terminal, equivalent in octal
    println("\u001b\u005b\u0048\u001b\u005b\u0032\u004a")
}

fun cliGetChar(): Char {
    val inputString = cliGetStringInput()
    return if (inputString.isNullOrEmpty()) '\u0000' else inputString[0]
}

inline fun <reified T: Any?> printErr(s: T) {
    System.err.print(s)
}

inline fun <reified T: Any?> printlnErr(s: T) {
    System.err.println(s)
}