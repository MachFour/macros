package com.machfour.macros.util

import java.io.File

object FileUtils {
    fun joinPath(basePath: String, suffix: String): String {
        return basePath + File.separator + suffix
    }
}