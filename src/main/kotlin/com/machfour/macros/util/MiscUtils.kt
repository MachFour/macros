package com.machfour.macros.util

object MiscUtils {
    /*
     * This is the version of trim that the Java-to-Kotlin conversion tool
     * replaces Java's String.trim() with. We'll put it here for safe keeping
     */
    fun String.javaTrim() : String = trim { it <= ' ' }
}