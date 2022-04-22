package com.machfour.macros.util

// The Java-to-Kotlin conversion tool replaces Java's String.trim() with this code.
// We'll put it here for safekeeping.
fun String.javaTrim() : String = trim { it <= ' ' }


// copied from deprecated kotlin code
fun String.capitalize(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}