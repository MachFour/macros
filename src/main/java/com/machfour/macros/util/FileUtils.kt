package com.machfour.macros.util

import java.io.File

object FileUtils {
    @JvmStatic
    fun joinPath(basePath: String, suffix: String): String {
        return basePath + File.separator + suffix
    }
}