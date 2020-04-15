package com.machfour.macros.util;

import java.io.File;

public class FileUtils {
    private FileUtils() {
    }

    public static String joinPath(String dir, String filename) {
        return dir + File.separator + filename;
    }

}

