package com.machfour.macros.cli;

import com.machfour.macros.core.Column;
import com.machfour.macros.objects.FoodPortion;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.util.StringJoiner;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.*;

import static com.machfour.macros.core.Schema.NutritionDataTable.*;

class MealPrinter {
    private final PrintStream out;
    private static final String columnSep = " | ";
    private static final int nameWidth = 45;
    private static final int servingWidth = 6;
    private static final int dataWidth = 4;
    private static final List<Column<NutritionData, Double>> conciseTableCols;
    private static final List<Column<NutritionData, Double>> allNutrientsToPrint;
    private static final Map<Column<NutritionData, Double>, String> briefNames;
    private static final Map<Column<NutritionData, Double>, String> prettyNames;
    static {
        briefNames = new HashMap<>();
        briefNames.put(CALORIES, "Cals");
        briefNames.put(PROTEIN, "Prot");
        briefNames.put(FAT, "Fat");
        briefNames.put(CARBOHYDRATE, "Carb");
        briefNames.put(QUANTITY, "Qty");
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
        prettyNames.put(QUANTITY, "Quantity");
        conciseTableCols = Arrays.asList(
                  CALORIES
                , PROTEIN
                , FAT
                , CARBOHYDRATE
        );
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
        );

    }


    MealPrinter(PrintStream out) {
        this.out = out;
    }

    private void println(String s) {
        out.println(s);
    }
    private void printf(String s, Object ... args) {
        out.printf(s, args);
    }
    private void println() {
        out.println();
    }

    private void printPer100g(NutritionData nd, boolean verbose) {
        printNutritionData(nd.rescale(100), verbose);
    }

    private void printNutritionData(NutritionData nd, boolean verbose) {
        String lineFormat = "%15s: %4.0f %s";
        for (Column<NutritionData, Double> col: allNutrientsToPrint) {
            Double value = nd.amountOf(col, 0.0);
            String unit = NutritionData.getUnitForNutrient(col);
            if (!nd.hasCompleteData(col)) {
                // mark incomplete
                unit += " **";
            }
            println(String.format(lineFormat, prettyNames.get(col), value, unit));
        }
    }

    private void printEnergyProportions(NutritionData nd, boolean verbose) {
        println("Energy proportions (approx.)");
        Map<Column<NutritionData, Double>, Double> proportionMap = nd.makeEnergyProportionsMap();
        for (Column<NutritionData, Double> col: proportionMap.keySet()) {
            printf("%15s: %5.1f%%\n", prettyNames.get(col), proportionMap.get(col));
        }
    }

    private static int sum(List<Integer> sums) {
        int sum = 0;
        for (Integer i : sums) {
            sum += i;
        }
        return sum;
    }

    /*
     * Prints a row of a table (helper method for printMeal())
     */
    private void printRow(List<String> row, List<Integer> widths, List<Boolean> rightAlign, String sep) {
        assert (row.size() == widths.size() && row.size() == rightAlign.size());
        for (int i = 0; i < row.size(); ++i) {
            String align = rightAlign.get(i) ? "" : "-";
            String width = String.valueOf(widths.get(i));
            printf("%" + align + width + "s%s", row.get(i), sep);
        }
        println();
    }

    private static String formatDouble(Double d, int width) {
        return d == null ? "" : String.format("%" + width + ".0f", d);
    }

    private List<String> nutritionDataToRow(String name, NutritionData nd)  {
        List<String> row = new ArrayList<>(conciseTableCols.size() + 2);
        // add food name
        row.add(name);
        // add nutrients, formatting to be the appropriate width
        // (no decimal point)
        for (Column<NutritionData, Double> nutrient : conciseTableCols) {
            Double value = nd.amountOf(nutrient);
            row.add(value == null ? "" : formatDouble(value, dataWidth));
        }
        // add quantity and unit
        String qty = formatDouble(nd.getQuantity(), servingWidth - 2);
        String qtyUnit = String.format("%-2s", nd.getQuantityUnitAbbr());
        row.add(qty + qtyUnit);
        return row;
    }

    private void printMeal(@NotNull Meal meal) {
        /*
         * Columns: first the food name, then one for each nutrient, then quantity/serving
         */
        int numCols = 1 + conciseTableCols.size() + 1;
        // holds the meal name and labels for each nutrient column
        List<String> headingRow = new ArrayList<>(numCols);
        List<Integer> rowWidths = new ArrayList<>(numCols);
        List<Boolean> rightAlign = new ArrayList<>(numCols);
        // first column has meal name (heading) and food names for other rows
        headingRow.add(meal.getName());
        rowWidths.add(nameWidth);
        rightAlign.add(false);
        // next columns have names for each nutrient (heading) then corresponding data
        for (Column<NutritionData, Double> col : conciseTableCols) {
            headingRow.add(briefNames.get(col));
            rowWidths.add(dataWidth);
            rightAlign.add(true);
        }
        // last column is quantity, so is a bit longer
        headingRow.add(briefNames.get(QUANTITY));
        rowWidths.add(servingWidth);
        rightAlign.add(true);

        // row separator spans all columns plus each separator, but we discount the space
        // after the last separator
        int rowSepLength = sum(rowWidths) + rowWidths.size()*columnSep.length() - 1;
        String rowSeparator = new StringJoiner<>(Collections.nCopies(rowSepLength, "=")).join();

        printRow(headingRow, rowWidths, rightAlign, columnSep);
        println(rowSeparator);
        // now we get to the actual data
        List<List<String>> dataRows = new ArrayList<>();
        for (FoodPortion fp : meal.getFoodPortions()) {
            String name = fp.getFood().getMediumName();
            NutritionData nd = fp.getNutritionData();
            dataRows.add(nutritionDataToRow(name, nd));
        }
        for (List<String> row : dataRows) {
            printRow(row, rowWidths, rightAlign, columnSep);
        }
        // now print total
        println(rowSeparator);
        String totalName = String.format("Total for %s", meal.getName());
        NutritionData totalNd = meal.getNutritionTotal();
        List<String> totalRow = nutritionDataToRow(totalName, totalNd);
        printRow(totalRow, rowWidths, rightAlign, columnSep);
    }

    public void printMeals(List<Meal> meals) {
        boolean print100 = false;
        boolean verbose = false;
        boolean printGrandTotal = true;
        println("============");
        println("Meal totals:");
        println("============");
        println();
        for (Meal m : meals) {
            printMeal(m);
            println();
            if (print100) {
                printPer100g(m.getNutritionTotal(), verbose);
                println("============================");
                println();
            }
        }
        if (printGrandTotal) {
            List<NutritionData> allNutData = new ArrayList<>();
            for (Meal m : meals) {
                allNutData.add(m.getNutritionTotal());
            }
            NutritionData totalNutData = NutritionData.sum(allNutData);
            println("====================");
            println("Total for all meals:");
            println("====================");
            printNutritionData(totalNutData, verbose);
            println();
            printEnergyProportions(totalNutData, verbose);
        }
    }
}
