package com.machfour.macros.util

import java.io.File

fun joinFilePath(basePath: String, suffix: String): String {
    return basePath + File.separator + suffix
}