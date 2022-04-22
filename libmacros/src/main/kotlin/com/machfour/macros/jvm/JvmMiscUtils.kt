package com.machfour.macros.jvm

fun pauseUntilEnter() {
    try {
        System.`in`.read()
    } catch (e: Exception) {
        // do nothing
    }
}
