package com.machfour.macros.util;

import java.io.File;

public class FileUtils {
    private FileUtils() {
    }

    public static String joinPath(String basePath, String suffix) {
        return basePath + File.separator + suffix;
    }

}

