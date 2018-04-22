package com.machfour.macros.util;

import com.machfour.macros.core.NutritionData;
import com.machfour.macros.data.Column;
import com.machfour.macros.data.Columns;
import com.machfour.macros.data.Types;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by max on 25/10/17.
 */

public final class DataUtils {
    public static final Map<Column<NutritionData, Types.Real, Double>, String> unitString;

    static {
        unitString = new HashMap<>(NutritionData.NUTRIENT_COLUMNS.size());
        for (Column<NutritionData, Types.Real, Double> nutrient : NutritionData.NUTRIENT_COLUMNS) {
            if (nutrient.equals(Columns.NutritionDataCol.SODIUM) ||
                nutrient.equals(Columns.NutritionDataCol.CALCIUM) ||
                nutrient.equals(Columns.NutritionDataCol.OMEGA_3_FAT) ||
                nutrient.equals(Columns.NutritionDataCol.OMEGA_6_FAT)) {
                unitString.put(nutrient, "mg");
            } else if (nutrient.equals(Columns.NutritionDataCol.CALORIES)) {
                unitString.put(nutrient, "cal");
            } else if (nutrient.equals(Columns.NutritionDataCol.KILOJOULES)) {
                unitString.put(nutrient, "kj");
            } else {
                unitString.put(nutrient, "g");
            }
        }
    }

    private DataUtils() {
    }

    // Formats the given data field (assumed to be a double) to a string.
    // Automatically adds an asterisk if the data is missing.
    public static String formatNutrnData(NutritionData nd, Column<NutritionData, Types.Real, Double> field, int decimalPoints) {
        return formatDouble(nd.getNutrientData(field), decimalPoints, !nd.hasNutrient(field));
    }

    public static String formatDouble(double in, int decimalPoints, boolean missing) {
        String formatString = "%." + decimalPoints + "f" + (missing ? "*" : "");
        return String.format(Locale.getDefault(), formatString, in);
    }

    public static String formatDouble(double in, int width, int decimalPoints, boolean missing) {
        String formatString = "%" + width + "." + decimalPoints + "f" + (missing ? "*" : "");
        return String.format(Locale.getDefault(), formatString, in);
    }

}
