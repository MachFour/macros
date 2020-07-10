package com.machfour.macros.cli.utils;


import com.machfour.macros.core.Column;
import com.machfour.macros.names.ColumnNamer;
import com.machfour.macros.names.ColumnUnits;
import com.machfour.macros.names.DefaultColumnUnits;
import com.machfour.macros.names.EnglishColumnNames;
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


    public static void printPer100g(NutritionData nd, boolean verbose, PrintStream out) {
        printNutritionData(nd.rescale(100), verbose, out);
    }

    // TODO use methods from PrintFormatting here?
    public static void printNutritionData(NutritionData nd, boolean verbose, PrintStream out) {
        // TODO pass in ColumnUnits and ColumnNamer
        ColumnNamer colNamer = EnglishColumnNames.getInstance();
        ColumnUnits colUnits = DefaultColumnUnits.getInstance();
        // TODO get these lengths from ColumnNamer
        String lineFormat = verbose ? "%15s: %6.1f %-2s" : "%15s: %4.0f %-2s";
        for (Column<NutritionData, Double> col: allNutrientsToPrint) {
            Double value = nd.amountOf(col, 0.0);
            String unitStr = colUnits.getUnit(col).getAbbr();
            String colName = colNamer.getNutrientName(col);
            out.print(String.format(lineFormat, colName, value, unitStr));
            if (!nd.hasCompleteData(col)) {
                // mark incomplete
                out.print(" (*)");
            }
            out.println();
        }
    }

    public static void printEnergyProportions(NutritionData nd, boolean verbose, PrintStream out) {
        // TODO pass in ColumnUnits and ColumnNamer
        ColumnNamer colNamer = EnglishColumnNames.getInstance();
        out.println("Energy proportions (approx.)");
        // TODO get these lengths from ColumnNamer / ColumnUnits
        Map<Column<NutritionData, Double>, Double> proportionMap = nd.makeEnergyProportionsMap();
        String fmt = verbose ? "%15s: %5.1f%%\n" : "%15s: %4.0f %%\n";
        for (Column<NutritionData, Double> col: proportionMap.keySet()) {
            out.printf(fmt, colNamer.getNutrientName(col), proportionMap.get(col));
        }
    }

    /*
     * Fixed width string format, left aligned
     */
    static String strFmtL(int n) {
        return "%-" + n + "s";
    }
    /*
     * Fixed width string format
     */
    static String strFmt(int n) {
        return "%" + n + "s";
    }

    /*
     * Fixed width string format
     */
    static String strFmt(int n, boolean leftAlign) {
        return "%" + (leftAlign ? "-" : "") + n + "s";
    }

    /*
     * Ingredients printing parameters
     */
    private static final int quantityWidth = 10;
    private static final int notesWidth = 25;
    private static final int nameWidth = PrintFormatting.nameWidth;
    private static final String start = " | ";
    private static final String sep = "  ";
    private static final String end = " |\n";
    private static final String lineFormat = start + strFmtL(nameWidth) + sep + strFmt(quantityWidth) + sep + strFmtL(notesWidth) + end;
    private static final int lineLength = nameWidth + notesWidth + quantityWidth + 2*sep.length() + start.length() + end.length() - 2;
    private static final String hLine = " " + StringJoiner.of("-").copies(lineLength).join();

    public static void printIngredients(List<Ingredient> ingredients, PrintStream out) {
        // XXX use printLine(text, widths), etc function
        out.printf(lineFormat, "Name", "Quantity", "Notes");
        out.println(hLine);
        for (Ingredient i: ingredients) {
            // format:  <name>          (<notes>)     <quantity/serving>
            Food iFood = i.getIngredientFood();
            String notes = i.getNotes();
            String name = iFood.getMediumName();
            String noteString = (notes != null) ? notes : "";
            String quantityString = PrintFormatting.formatQuantity(i.quantity(), i.qtyUnit(), quantityWidth);
            out.printf(lineFormat, name, quantityString, noteString);
            // TODO replace quantity with serving if specified
            //Serving iServing = i.getServing();
            //out.printf(" %-8s", iServing != null ? "(" + i.servingCountString() + " " +  iServing.name() + ")" : "");
        }
        out.println(hLine);
    }

    // command line inputs
    // returns null if there was an error or input was invalid
    public static @Nullable Integer getIntegerInput(BufferedReader in, PrintStream out, int min, int max) {
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
    public static @Nullable Double getDoubleInput(BufferedReader in, PrintStream out) {
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

    @Nullable
    public static String getStringInput(BufferedReader in, PrintStream out) {
        String input = null;
        try {
            input = in.readLine();
        } catch (IOException e) {
            out.println("Error reading input: " + e.getMessage());
        }
        if (input != null) {
            input = input.trim();
        }
        return input;
    }

    public static void clearTerminal(PrintStream out) {
        // this is what /usr/bin/clear outputs on my terminal
        //out.println("\u001b\u005b\u0048\u001b\u005b\u0032\u004a");
        // equivalent in octal
        out.println("\033\133\110\033\133\062\112");
    }

    public static char getChar(BufferedReader in, PrintStream out) {
        String input = getStringInput(in, out);
        if (input == null || input.isEmpty()) {
            return '\0';
        } else {
            return input.charAt(0);
        }
    }
}
