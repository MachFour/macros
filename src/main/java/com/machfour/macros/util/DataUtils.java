package com.machfour.macros.util;

import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.core.Column;
import com.machfour.macros.core.Schema;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by max on 25/10/17.
 */

public final class DataUtils {

    private DataUtils() {
    }

    // Formats the given data field (assumed to be a double) to a string.
    // Automatically adds an asterisk if the data is missing.
    public static String formatNutrnData(NutritionData nd, Column<NutritionData, Double> field, int decimalPoints) {
        return formatDouble(nd.amountOf(field), decimalPoints, !nd.hasCompleteData(field));
    }

    public static String formatDouble(double in, int decimalPoints, boolean missing) {
        String formatString = "%." + decimalPoints + "f" + (missing ? "*" : "");
        return String.format(Locale.getDefault(), formatString, in);
    }

    public static String formatDouble(double in, int width, int decimalPoints, boolean missing) {
        String formatString = "%" + width + "." + decimalPoints + "f" + (missing ? "*" : "");
        return String.format(Locale.getDefault(), formatString, in);
    }

    public static long systemMillis() {
        return System.nanoTime()/1000000;
    }
}
