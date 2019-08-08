package com.machfour.macros.util;

import com.machfour.macros.core.Column;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.objects.QtyUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.machfour.macros.core.Schema.NutritionDataTable.*;

public class PrintFormatting {

    public static final int nameWidth = 45;
    public static final int servingWidth = 6;
    public static final int shortDataWidth = 4;
    public static final int longDataWidth = 6;

    // shortest name for each nutrient printed in default mode
    public static final Map<Column<NutritionData, Double>, String> briefNames;
    // longer name for nutrients printed in verbose mode
    public static final Map<Column<NutritionData, Double>, String> longerNames;
    // full name for each nutrient
    public static final Map<Column<NutritionData, Double>, String> prettyNames;

    static {
        briefNames = new HashMap<>();
        briefNames.put(CALORIES, "Cals");
        briefNames.put(PROTEIN, "Prot");
        briefNames.put(FAT, "Fat");
        briefNames.put(CARBOHYDRATE, "Carb");
        briefNames.put(QUANTITY, "Qty");

        longerNames = new HashMap<>();
        longerNames.put(CALORIES, "Cals");
        longerNames.put(PROTEIN, "Protæn");
        longerNames.put(FAT, "Fat");
        longerNames.put(SATURATED_FAT, "SatFat");
        longerNames.put(CARBOHYDRATE, "Carbs");
        longerNames.put(SUGAR, "Sugar");
        longerNames.put(FIBRE, "Fibre");
        longerNames.put(SODIUM, "Sodium");
        longerNames.put(CALCIUM, "Ca");
        longerNames.put(QUANTITY, "Qty");

        prettyNames = new HashMap<>();
        prettyNames.put(KILOJOULES, "Kilojoules");
        prettyNames.put(CALORIES, "Calories");
        prettyNames.put(PROTEIN, "Protein");
        prettyNames.put(FAT, "Fat");
        prettyNames.put(SATURATED_FAT, "Saturated Fat");
        prettyNames.put(CARBOHYDRATE, "Carbohydrate");
        prettyNames.put(SUGAR, "Sugar");
        prettyNames.put(FIBRE, "Fibre");
        prettyNames.put(SODIUM, "Sodium");
        prettyNames.put(CALCIUM, "Calcium");
        prettyNames.put(QUANTITY, "Quantity");
    }

    private PrintFormatting() {}


    public static String prettyDay(@NotNull DateStamp day) {
        StringBuilder prettyStr = new StringBuilder(day.toString());
        DateStamp today = DateStamp.forCurrentDate();
        if (day.equals(today)) {
            prettyStr.append(" (today)");
        } else if (day.equals(today.step(-1))) {
            prettyStr.append(" (yesterday)");
        }
        return prettyStr.toString();
    }

    // converts null to blank strings, with optional default
    public static String deNull(String in, String ifNull) {
        return in != null ? in : ifNull;
    }

    public static String deNull(String in) {
        return deNull(in, "");
    }

    public static String formatQuantity(@Nullable Double qty, boolean verbose) {
        int width = verbose ? longDataWidth : shortDataWidth;
        return formatQuantity(qty, null, width, 1, verbose, false, "");
    }

    public static String formatQuantity(@Nullable Double qty, int width) {
        return formatQuantity(qty, null, width);
    }
    public static String formatQuantity(@Nullable Double qty, @Nullable QtyUnit unit, int width) {
        return formatQuantity(qty, unit, width, 2, false, false, "");
    }
    private static String formatQuantity(@Nullable Double qty, @Nullable QtyUnit unit, int width, int unitWidth,
                                 boolean withDp, boolean alignLeft, @NotNull String forNullQty) {
        if (qty == null) {
            return forNullQty;
        } else if (width <= 0) {
            throw new IllegalArgumentException("Must have width > 0");
        } else if (unit != null && (unitWidth <= 0 || width - unitWidth <= 0)) {
            throw new IllegalArgumentException("Must have width > unitWidth > 0");
        }
        Formatter f = new Formatter(Locale.getDefault());
        if (alignLeft) {
            f.format("%" + (withDp ? ".1f" : ".0f") + "%s", qty);
            if (unit != null) {
                f.format("%" + unitWidth + "s", qty, unit.abbr());
            }
            return String.format("%-" + width + "s", f.toString());
        } else {
            f.format("%" + (width-unitWidth) + (withDp ? ".1f" : ".0f"), qty);
            if (unit != null) {
                f.format("%-" + unitWidth + "s", unit.abbr());
            }
            return f.toString();
        }
    }

    // Converts the given data field into a string. Adds an asterisk if the data is missing.
    public static String formatNutrnData(NutritionData nd, Column<NutritionData, Double> field) {
        boolean missing = !nd.hasCompleteData(field);
        return formatQuantity(nd.amountOf(field), false) + (missing ? "*" : "");
    }
}
