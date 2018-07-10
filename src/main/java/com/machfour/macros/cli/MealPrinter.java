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
    private static final String columnSep = " | ";
    private static final int nameWidth = 45;
    private static final int servingWidth = 6;
    private static final int dataWidth = 4;
    private static final List<Column<NutritionData, Double>> conciseTableCols;

    static {
        conciseTableCols = Arrays.asList(
                CALORIES
                , PROTEIN
                , FAT
                , CARBOHYDRATE
        );
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
    private static void printRow(List<String> row, List<Integer> widths, List<Boolean> rightAlign, String sep, PrintStream out) {
        assert (row.size() == widths.size() && row.size() == rightAlign.size());
        for (int i = 0; i < row.size(); ++i) {
            String align = rightAlign.get(i) ? "" : "-";
            String width = String.valueOf(widths.get(i));
            out.printf("%" + align + width + "s%s", row.get(i), sep);
        }
        out.println();
    }

    private static String formatDouble(Double d, int width) {
        return d == null ? "" : String.format("%" + width + ".0f", d);
    }

    private static List<String> nutritionDataToRow(String name, NutritionData nd)  {
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

    static void printMeal(@NotNull Meal meal, @NotNull PrintStream out) {
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
            headingRow.add(CliUtils.briefNames.get(col));
            rowWidths.add(dataWidth);
            rightAlign.add(true);
        }
        // last column is quantity, so is a bit longer
        headingRow.add(CliUtils.briefNames.get(QUANTITY));
        rowWidths.add(servingWidth);
        rightAlign.add(true);

        // row separator spans all columns plus each separator, but we discount the space
        // after the last separator
        int rowSepLength = sum(rowWidths) + rowWidths.size()*columnSep.length() - 1;
        String rowSeparator = new StringJoiner<>(Collections.nCopies(rowSepLength, "=")).join();

        printRow(headingRow, rowWidths, rightAlign, columnSep, out);
        out.println(rowSeparator);
        // now we get to the actual data
        List<List<String>> dataRows = new ArrayList<>();
        for (FoodPortion fp : meal.getFoodPortions()) {
            String name = fp.getFood().getMediumName();
            NutritionData nd = fp.getNutritionData();
            dataRows.add(nutritionDataToRow(name, nd));
        }
        for (List<String> row : dataRows) {
            printRow(row, rowWidths, rightAlign, columnSep, out);
        }
        // now print total
        out.println(rowSeparator);
        String totalName = String.format("Total for %s", meal.getName());
        NutritionData totalNd = meal.getNutritionTotal();
        List<String> totalRow = nutritionDataToRow(totalName, totalNd);
        printRow(totalRow, rowWidths, rightAlign, columnSep, out);
    }

    static void printMeals(List<Meal> meals, PrintStream out) {
        boolean print100 = false;
        boolean verbose = false;
        boolean printGrandTotal = true;
        out.println("============");
        out.println("Meal totals:");
        out.println("============");
        out.println();
        for (Meal m : meals) {
            printMeal(m, out);
            out.println();
            if (print100) {
                CliUtils.printPer100g(m.getNutritionTotal(), verbose, out);
                out.println("============================");
                out.println();
            }
        }
        if (printGrandTotal) {
            List<NutritionData> allNutData = new ArrayList<>();
            for (Meal m : meals) {
                allNutData.add(m.getNutritionTotal());
            }
            NutritionData totalNutData = NutritionData.sum(allNutData);
            out.println("====================");
            out.println("Total for all meals:");
            out.println("====================");
            CliUtils.printNutritionData(totalNutData, verbose, out);
            out.println();
            CliUtils.printEnergyProportions(totalNutData, verbose, out);
        }
    }
}
