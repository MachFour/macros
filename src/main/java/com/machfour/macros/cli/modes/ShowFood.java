package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.cli.utils.CliUtils;
import com.machfour.macros.core.Schema;
import com.machfour.macros.objects.CompositeFood;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.FoodType;
import com.machfour.macros.objects.Ingredient;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.objects.Serving;
import com.machfour.macros.queries.FoodQueries;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.util.DateTimeUtils;

import java.io.PrintStream;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;


public class ShowFood extends CommandImpl {
    private static final String NAME = "show";
    private static final String USAGE = String.format("Usage: %s %s <index_name>", getProgramName(), NAME);

    public ShowFood() {
        super(NAME, USAGE);
    }

    @Override
    public void doActionNoExitCode(List<String> args) {
        if (args.size() == 1 || args.contains("--help")) {
            printHelp();
            if (args.size() == 1) {
                out.println("Please enter the index name of the food to show");
            }
            return;
        }
        boolean verbose = false;
        if (args.contains("-v") || args.contains("--verbose")) {
            verbose = true;
        }

        MacrosDataSource ds =  config.getDataSourceInstance();
        String indexName = args.get(1);
        Food foodToList = null;
        try {
            foodToList = FoodQueries.getFoodByIndexName(ds, indexName);
        } catch (SQLException e) {
            out.print("SQL exception occurred: ");
            out.println(e.getErrorCode());
        }
        if (foodToList == null) {
            out.printf("No food found with index name %s\n", indexName);
            return;
        }

        printFood(foodToList, verbose, out);
    }

    public static void printFoodSummary(Food f, PrintStream out) {
        final DateTimeFormatter dateFormat = DateTimeUtils.LOCALIZED_DATETIME_MEDIUM;
        out.printf("Name:          %s\n", f.getMediumName());
        out.printf("Notes:         %s\n", Objects.toString(f.getNotes(), "(no notes)"));
        out.printf("Category:      %s\n", f.getFoodCategory());
        out.println();
        out.printf("Type:          %s\n", f.getFoodType().getName());
        out.printf("Created on:    %s\n", dateFormat.format(f.getCreateInstant()));
        out.printf("Last modified: %s\n", dateFormat.format(f.getModifyInstant()));
    }

    public static void printFood(Food f, boolean verbose, PrintStream out) {
        out.println("============");
        out.println(" Food Data  ");
        out.println("============");
        out.println();
        out.println();
        printFoodSummary(f, out);

        out.println("================================");
        out.println();
        /*
         * Nutrition data
         */
        NutritionData nd = f.getNutritionData();
        String unit = nd.qtyUnitAbbr();
        out.printf("Nutrition data (source: %s)\n", nd.getData(Schema.NutritionDataTable.DATA_SOURCE));
        out.println();

        if (nd.getDensity() != null) {
            // width copied from printFoodSummary()
            out.printf("Density:       %.2f (g/ml)\n", nd.getDensity());
            out.println();
        }

        // if entered not per 100g, print both original amount and per 100 g
        if (nd.getQuantity() != 100) {
            out.printf("Per %.0f%s:\n", nd.getQuantity(), unit);
            CliUtils.printNutritionData(nd, verbose, out);
            out.println();
            nd = nd.rescale(100);
        }
        out.printf("Per %.0f%s:\n", nd.getQuantity(), unit); // should now be 100
        CliUtils.printNutritionData(nd, verbose, out);
        out.println();

        /*
         * Servings
         */
        out.println("================================");
        out.println();
        out.println("Servings:");
        out.println();

        List<Serving> servings = f.getServings();
        if (!servings.isEmpty()) {
            for (Serving s: servings) {
                out.printf(" - %s: %.1f%s\n", s.getName(), s.getQuantity(), s.getQtyUnit().getAbbr());
            }
        } else {
            out.println("(No servings recorded)");
        }

        out.println();
        /*
         * Ingredients
         */

        if (!f.getFoodType().equals(FoodType.COMPOSITE)) {
            return;
        }
        assert (f instanceof CompositeFood);

        CompositeFood cf = (CompositeFood) f;

        out.println("================================");
        out.println();
        out.println("Ingredients:");
        out.println();
        List<Ingredient> ingredients = cf.getIngredients();
        if (!ingredients.isEmpty()) {
            CliUtils.printIngredients(ingredients, out);
            out.println();
        } else {
            out.println("(No ingredients recorded (but there probably should be!)");
        }
        out.println("================================");
        out.println();
    }
}
