package com.machfour.macros.cli;


import com.machfour.macros.core.Column;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.Ingredient;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.util.PrintFormatting;
import com.machfour.macros.util.StringJoiner;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import static com.machfour.macros.core.Schema.NutritionDataTable.*;

public class CliUtils {
    static final List<Column<NutritionData, Double>> allNutrientsToPrint = Arrays.asList(
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

    private CliUtils () {}


    static void printPer100g(NutritionData nd, boolean verbose, PrintStream out) {
        printNutritionData(nd.rescale(100), verbose, out);
    }

    // TODO use methods from PrintFormatting here?
    static void printNutritionData(NutritionData nd, boolean verbose, PrintStream out) {
        String lineFormat = "%15s: %4.0f %s";
        for (Column<NutritionData, Double> col: allNutrientsToPrint) {
            Double value = nd.amountOf(col, 0.0);
            String unit = NutritionData.getUnitForNutrient(col);
            if (!nd.hasCompleteData(col)) {
                // mark incomplete
                unit += " **";
            }
            out.println(String.format(lineFormat, PrintFormatting.prettyNames.get(col), value, unit));
        }
    }

    static void printEnergyProportions(NutritionData nd, boolean verbose, PrintStream out) {
        out.println("Energy proportions (approx.)");
        Map<Column<NutritionData, Double>, Double> proportionMap = nd.makeEnergyProportionsMap();
        for (Column<NutritionData, Double> col: proportionMap.keySet()) {
            out.printf("%15s: %5.1f%%\n", PrintFormatting.prettyNames.get(col), proportionMap.get(col));
        }
    }

    static void printIngredients(List<Ingredient> ingredients, PrintStream out) {
        int quantityWidth = 10;
        String lineFormat = " | %-" + PrintFormatting.nameWidth + "s %-25s %" + quantityWidth + "s |\n";
        // 2 + 2 + 20 + 2 = 31
        String hLine = " " + StringJoiner.of("-").copies(PrintFormatting.nameWidth + 31 + quantityWidth).join();
        // XXX use printLine(text, widths), etc function
        out.printf(lineFormat, "Name", "Notes", "Quantity");
        out.println(hLine);
        for (Ingredient i: ingredients) {
            // format:  <name>          (<notes>)     <quantity/serving>
            Food iFood = i.getIngredientFood();
            String notes = i.getNotes();
            String name = iFood.getMediumName();
            String noteString = (notes != null ? "(" + notes + ")" : "");
            String quantityString = PrintFormatting.formatQuantity(i.quantity(), i.qtyUnit(), quantityWidth);
            out.printf(lineFormat, name, noteString, quantityString);
            // TODO replace quantity with serving if specified
            //Serving iServing = i.getServing();
            //out.printf(" %-8s", iServing != null ? "(" + i.servingCountString() + " " +  iServing.name() + ")" : "");
        }
        out.println(hLine);
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
            out.printf("Bad number format: '%s'\n", input);
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
        //out.println("\u001b\u005b\u0048\u001b\u005b\u0032\u004a");
        // equivalent in octal
        out.println("\033\133\110\033\133\062\112");
    }

    static char getChar(BufferedReader in, PrintStream out) {
        String input = getStringInput(in, out);
        if (input == null || input.isEmpty()) {
            return '\0';
        } else {
            return input.charAt(0);
        }
    }
}
