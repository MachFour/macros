package com.machfour.macros.util;

import com.machfour.macros.core.Column;
import com.machfour.macros.names.ColumnStrings;
import com.machfour.macros.names.DefaultColumnStrings;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.objects.Unit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import static com.machfour.macros.core.Schema.NutritionDataTable.*;

public class PrintFormatting {

    public static final int nameWidth = 45;
    public static final int servingWidth = 6;
    public static final int shortDataWidth = 4;
    public static final int longDataWidth = 6;

    private final ColumnStrings nutritionDataStrings;

    private PrintFormatting(@NotNull ColumnStrings supplier) {
        this.nutritionDataStrings = supplier;
    }

    private static final PrintFormatting DEFAULT_INSTANCE;
    static {
        DEFAULT_INSTANCE = new PrintFormatting(DefaultColumnStrings.getInstance());
    }

    public static String formatQuantity(@Nullable Double qty, boolean verbose) {
        int width = verbose ? longDataWidth : shortDataWidth;
        return formatQuantity(qty, null, width, 1, verbose, false, "");
    }

    public static String formatQuantity(@Nullable Double qty, int width) {
        return formatQuantity(qty, null, width);
    }
    public static String formatQuantity(@Nullable Double qty, @Nullable Unit unit, int width) {
        return formatQuantity(qty, unit, width, 0, false, false, "");
    }
    private static String formatQuantity(@Nullable Double qty, @Nullable Unit unit, int width, int unitWidth,
                                 boolean withDp, boolean alignLeft, @NotNull String forNullQty) {
        if (qty == null) {
            return forNullQty;
        }

        if (unit != null && unitWidth <= 0) {
            unitWidth = unit.getAbbr().length();
        }
        if (width > 0 && width - unitWidth <= 0) {
            throw new IllegalArgumentException("If width != 0, must have width > unitWidth > 0");
        }
        Formatter f = new Formatter(Locale.getDefault());
        if (alignLeft) {
            f.format("%" + (withDp ? ".1f" : ".0f") + "%s", qty);
            if (unit != null) {
                f.format("%" + unitWidth + "s", qty, unit.getAbbr());
            }
            return width > 0 ? String.format("%-" + width + "s", f.toString()) : f.toString();
        } else {
            f.format("%" + ((width > 0) ? (width-unitWidth) : "") + (withDp ? ".1f" : ".0f"), qty);
            if (unit != null) {
                f.format("%-" + unitWidth + "s", unit.getAbbr());
            }
            return f.toString();
        }
    }

    // Converts the given data field into a string. Adds an asterisk if the data is missing.
    // Returns null if the input nutrition data is null
    public static String formatNutrnData(@Nullable NutritionData nd, Column<NutritionData, Double> field) {
        return formatNutrnData(nd, field, false);
    }

    public static String formatNutrnData(@Nullable NutritionData nd, Column<NutritionData, Double> field, boolean withUnit) {
        if (nd == null) {
            return null;
        }
        boolean missing = !nd.hasCompleteData(field);
        if (!withUnit) {
            return formatQuantity(nd.amountOf(field), false) + (missing ? "*" : "");
        } else {
            //QtyUnit unit = QtyUnit.fromAbbreviation(NutritionData.getUnitStringForNutrient(field));
            // TODO
            Unit unit = DEFAULT_INSTANCE.nutritionDataStrings.getUnit(field);
            return formatQuantity(nd.amountOf(field), unit, 0);

        }
    }

    // list of field that should be formatted without a decimal place (because the values are
    // typically large (in the default/metric unit)
    // TODO use unit instead of checking the exact column
    private static final Set<Column<NutritionData, Double>> fieldsWithoutDp = new HashSet<>(Arrays.asList(
            CALORIES, KILOJOULES, OMEGA_3_FAT, OMEGA_6_FAT, IRON, POTASSIUM, SODIUM, CALCIUM
    ));

    // for formatting nutrition data in food details
    public static String foodDetailsFormat(@Nullable NutritionData nd,
                                           @NotNull Column<NutritionData, Double> field,
                                           @NotNull ColumnStrings ndStrings) {
        if (nd == null) {
            return null;
        }
        Unit unit = ndStrings.getUnit(field);
        int width;
        boolean needsDpFlag;
        if (!fieldsWithoutDp.contains(field)) {
            width = 12;
            needsDpFlag = true;
        } else {
            width = 10;
            needsDpFlag = false;
        }
        return formatQuantity(nd.amountOf(field), unit, width, 0, needsDpFlag, false, "");
    }

    // for formatting nutrition data in meal summaries (no decimal places)
    public static String mealSummaryFormat(@Nullable NutritionData nd,
                                           @NotNull Column<NutritionData, Double> field,
                                           @NotNull ColumnStrings ndStrings) {
        if (nd == null) {
            return null;
        }
        Unit unit = ndStrings.getUnit(field);
        return formatQuantity(nd.amountOf(field), 0) + " " + unit.getAbbr();
    }
}
