package com.machfour.macros.cli;

import com.machfour.macros.core.Column;
import com.machfour.macros.objects.FoodPortion;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.objects.QtyUnit;
import com.machfour.macros.util.PrintFormatting;
import com.machfour.macros.util.StringJoiner;
import com.machfour.macros.util.UnicodeUtils;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.*;

import static com.machfour.macros.core.Schema.NutritionDataTable.*;

class MealPrinter {
    private static final String columnSep = "   ";
    private static final List<Column<NutritionData, Double>> conciseTableCols;
    private static final List<Column<NutritionData, Double>> verboseTableCols;

    static {
        conciseTableCols = Arrays.asList(
                CALORIES
                , PROTEIN
                , FAT
                , CARBOHYDRATE
        );
        verboseTableCols = Arrays.asList(
                CALORIES
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

    private static int sum(Collection<Integer> sums) {
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
            int width = widths.get(i);
            String text = row.get(i);
            // fullwidth characters are printed as two characters in a terminal, so we should reduce width by the
            // number of fullwidth characters in the string
            int numDoubleWidthChars = UnicodeUtils.countDoubleWidthChars(text);

            // displayed length of text appears to be length() + numDoubleWidthChars characters long.
            // Equivalently, we can reduce width by this amount, to get the printing right
            width = Math.max(width - numDoubleWidthChars, 0);


            // prevent long strings from overrunning the width:
            // replace "This is a really long string"
            // with    "This is a really lo.."
            if (text.length() > width) {
                int newWidth = Math.max(width - 2, 0);
                // TODO with double width chars, this may reduce by too much
                text = text.substring(0, newWidth - 2) + "..";
            }
            //String widthStr = String.valueOf(width);
            out.printf("%" + align + width + "s%s", text, sep);
        }
        out.println();
    }

    private static List<String> nutritionDataToRow(String name, NutritionData nd, double qty, QtyUnit unit, boolean verbose)  {
        List<Column<NutritionData, Double>> nutrientColumns = verbose ? verboseTableCols : conciseTableCols;
        List<String> row = new ArrayList<>(nutrientColumns.size() + 2);
        // add food name
        row.add(name);
        // add nutrients, formatting to be the appropriate width
        for (Column<NutritionData, Double> nutrient : nutrientColumns) {
            Double value = nd.amountOf(nutrient);
            row.add(PrintFormatting.formatQuantity(value, verbose));
        }
        // add quantity and unit
        row.add(PrintFormatting.formatQuantity(qty, unit, PrintFormatting.servingWidth));
        return row;
    }

    static void printMeal(@NotNull Meal meal, boolean verbose, @NotNull PrintStream out) {
        List<Column<NutritionData, Double>> nutrientCols = verbose ? verboseTableCols : conciseTableCols;
        //Columns: first the food name, then one for each nutrient, then quantity/serving
        int numCols = 1 + nutrientCols.size() + 1;
        // holds the meal name and labels for each nutrient column
        List<String> headingRow = new ArrayList<>(numCols);
        List<Integer> rowWidths = new ArrayList<>(numCols);
        List<Boolean> rightAlign = new ArrayList<>(numCols);
        // first column has meal name (heading) and food names for other rows
        headingRow.add(meal.getName());
        rowWidths.add(PrintFormatting.nameWidth);
        rightAlign.add(false);
        // next columns have names for each nutrient (heading) then corresponding data
        for (Column<NutritionData, Double> col : nutrientCols) {
            if (verbose) {
                headingRow.add(PrintFormatting.longerNames.get(col));
                rowWidths.add(PrintFormatting.longDataWidth);
            } else {
                headingRow.add(PrintFormatting.briefNames.get(col));
                rowWidths.add(PrintFormatting.shortDataWidth);
            }
            rightAlign.add(true);
        }
        // last column is quantity, so is a bit longer
        headingRow.add(PrintFormatting.briefNames.get(QUANTITY));
        rowWidths.add(PrintFormatting.servingWidth);
        rightAlign.add(true);

        // row separator spans all columns plus each separator, but we discount the space
        // after the last separator
        int rowSepLength = sum(rowWidths) + rowWidths.size()*columnSep.length() - 1;
        String rowSeparator = StringJoiner.of(Collections.nCopies(rowSepLength, "=")).join();

        printRow(headingRow, rowWidths, rightAlign, columnSep, out);
        out.println(rowSeparator);
        // now we get to the actual data
        List<List<String>> dataRows = new ArrayList<>();
        for (FoodPortion fp : meal.getFoodPortions()) {
            String name = fp.getFood().getMediumName();
            NutritionData nd = fp.getNutritionData();
            dataRows.add(nutritionDataToRow(name, nd, fp.getQuantity(), fp.getQtyUnit(), verbose));
        }
        for (List<String> row : dataRows) {
            printRow(row, rowWidths, rightAlign, columnSep, out);
        }
        // now print total
        out.println(rowSeparator);
        String totalName = String.format("Total for %s", meal.getName());
        NutritionData totalNd = meal.getNutritionTotal();
        // for total data, just use the quantity and unit from the sum
        List<String> totalRow = nutritionDataToRow(totalName, totalNd, totalNd.getQuantity(), totalNd.qtyUnit(), verbose);
        printRow(totalRow, rowWidths, rightAlign, columnSep, out);
    }

    static void printMeals(Collection<Meal> meals, PrintStream out, boolean verbose, boolean per100, boolean grandTotal) {
        out.println("============");
        out.println("Meal totals:");
        out.println("============");
        out.println();
        for (Meal m : meals) {
            if (m.getFoodPortions().isEmpty()) {
                out.println("Meal " + m.getName() + " has no recorded data");
            } else {
                printMeal(m, verbose, out);
                out.println();
                if (per100) {
                    out.println("== Nutrient total per 100g ==");
                    CliUtils.printPer100g(m.getNutritionTotal(), verbose, out);
                    out.println("=============================");
                    out.println();
                }
            }
        }
        if (grandTotal) {
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

    static void printMeals(Collection<Meal> meals, PrintStream out) {
        printMeals(meals, out, false, false, true);
    }
}
