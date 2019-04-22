package com.machfour.macros.cli;


import com.machfour.macros.core.Column;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.util.DateStamp;
import com.machfour.macros.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import static com.machfour.macros.cli.CliMain.OUT;
import static com.machfour.macros.core.Schema.NutritionDataTable.*;

public class CliUtils {
    static final List<Column<NutritionData, Double>> allNutrientsToPrint;
    // shortest name for each nutrient printed in default mode
    static final Map<Column<NutritionData, Double>, String> briefNames;
    // longer name for nutrients printed in verbose mode
    static final Map<Column<NutritionData, Double>, String> longerNames;
    // full name for each nutrient
    static final Map<Column<NutritionData, Double>, String> prettyNames;


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
        longerNames.put(CALCIUM, "Calcm");
        longerNames.put(QUANTITY, "Qty");

        prettyNames = new HashMap<>();
        prettyNames.put(KILOJOULES, "Kilojoules");
        prettyNames.put(CALORIES, "Calories");
        prettyNames.put(PROTEIN, "Protein");
        prettyNames.put(FAT, "Fat");
        prettyNames.put(SATURATED_FAT, "Saturated");
        prettyNames.put(CARBOHYDRATE, "Carbohydrate");
        prettyNames.put(SUGAR, "Sugar");
        prettyNames.put(FIBRE, "Fibre");
        prettyNames.put(SODIUM, "Sodium");
        prettyNames.put(CALCIUM, "Calcium");
        prettyNames.put(QUANTITY, "Quantity");
        allNutrientsToPrint = Arrays.asList(
                KILOJOULES
                , CALORIES
                , PROTEIN
                , FAT
                , SATURATED_FAT
                , CARBOHYDRATE
                , SUGAR
                , FIBRE
                , SODIUM
                , CALCIUM
        );
    }

    private CliUtils () {}


    static String prettyDay(@NotNull DateStamp day) {
        StringBuilder prettyStr = new StringBuilder(day.toString());
        DateStamp today = DateStamp.forCurrentDate();
        if (day.equals(today)) {
            prettyStr.append(" (today)");
        } else if (day.equals(today.step(-1))) {
            prettyStr.append(" (yesterday)");
        }
        return prettyStr.toString();
    }

    static void printPer100g(NutritionData nd, boolean verbose, PrintStream out) {
        printNutritionData(nd.rescale(100), verbose, out);
    }

    static void printNutritionData(NutritionData nd, boolean verbose, PrintStream out) {
        String lineFormat = "%15s: %4.0f %s";
        for (Column<NutritionData, Double> col: allNutrientsToPrint) {
            Double value = nd.amountOf(col, 0.0);
            String unit = NutritionData.getUnitForNutrient(col);
            if (!nd.hasCompleteData(col)) {
                // mark incomplete
                unit += " **";
            }
            out.println(String.format(lineFormat, prettyNames.get(col), value, unit));
        }
    }

    static void printEnergyProportions(NutritionData nd, boolean verbose, PrintStream out) {
        out.println("Energy proportions (approx.)");
        Map<Column<NutritionData, Double>, Double> proportionMap = nd.makeEnergyProportionsMap();
        for (Column<NutritionData, Double> col: proportionMap.keySet()) {
            out.printf("%15s: %5.1f%%\n", prettyNames.get(col), proportionMap.get(col));
        }
    }

    // command line inputs
    // returns null if there was an error or input was invalid
    static @Nullable Integer getIntegerInput(BufferedReader in, PrintStream out, int min, int max) {
        String input = getStringInput(in, out);
        if (input == null) {
            return null;
        }
        try {
            int n = Integer.parseInt(input);
            if (n >= min && n <= max) {
                return n;
            }
        } catch (NumberFormatException ignore) {
            OUT.printf("Bad number format: '%s'\n", input);
        }
        return null;
    }

    // command line inputs
    // returns null if there was an error or input was invalid
    static @Nullable Double getDoubleInput(BufferedReader in, PrintStream out) {
        String input = getStringInput(in, out);
        if (input == null) {
            return null;
        }
        try {
            double d = Double.parseDouble(input);
            //if (Double.isFinite(d)) {
            // API 24 required on Android
            if (Math.abs(d) <= Double.MAX_VALUE) {
                return d;
            }
        } catch (NumberFormatException ignore) {
            out.printf("Bad number format: '%s'\n", input);
        }
        return null;
    }

    static @Nullable String getStringInput(BufferedReader in, PrintStream out) {
        try {
            String input = in.readLine();
            if (input != null) {
                return input.trim();
            }
        } catch (IOException e) {
            out.println("Error reading input: " + e.getMessage());
        }
        return null;
    }

    static void clearTerminal(PrintStream out) {
        // this is what /usr/bin/clear outputs on my terminal
        //OUT.println("\u001b\u005b\u0048\u001b\u005b\u0032\u004a");
        // equivalent in octal
        out.println("\033\133\110\033\133\062\112");
    }
}
