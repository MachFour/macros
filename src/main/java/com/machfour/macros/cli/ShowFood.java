package com.machfour.macros.cli;

import com.machfour.macros.core.Schema;
import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.*;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.util.PrintFormatting;
import com.machfour.macros.util.StringJoiner;

import java.io.PrintStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.machfour.macros.cli.CliMain.PROGNAME;
import static com.machfour.macros.cli.CliMain.OUT;
import static com.machfour.macros.util.PrintFormatting.deNull;

class ShowFood extends CommandImpl {
    private static final String NAME = "show";
    private static final String USAGE = String.format("Usage: %s %s <index_name>", PROGNAME, NAME);

    ShowFood() {
        super(NAME, USAGE);
    }

    @Override
    public void doAction(List<String> args) {
        if (args.size() == 1 || args.contains("--help")) {
            printHelp();
            if (args.size() == 1) {
                out.println("Please enter the index name of the food to show");
            }
            return;
        }
        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
        String indexName = args.get(1);
        Food foodToList = null;
        try {
            foodToList = db.getFoodByIndexName(indexName);
        } catch (SQLException e) {
            out.print("SQL exception occurred: ");
            out.println(e.getErrorCode());
        }
        if (foodToList == null) {
            out.printf("No food found with index name %s\n", indexName);
            return;
        }

        printFood(foodToList, OUT);
    }

    public static void printFoodSummary(Food f, PrintStream out) {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
        out.printf("Name:          %s\n", f.getMediumName());
        out.printf("Notes:         %s\n", deNull(f.getNotes(), "(no notes)"));
        out.printf("Category:      %s\n", f.getFoodCategory());
        out.println();
        out.printf("Type:          %s\n", f.getFoodType().getName());
        out.printf("Created on:    %s\n", dateFormat.format(f.createDate()));
        out.printf("Last modified: %s\n", dateFormat.format(f.modifyDate()));
    }

    public static void printFood(Food f, PrintStream out) {
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
            CliUtils.printNutritionData(nd, true, out);
            out.println();
            nd = nd.rescale(100);
        }
        out.printf("Per %.0f%s:\n", nd.getQuantity(), unit); // should now be 100
        CliUtils.printNutritionData(nd, true, out);
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
                out.printf(" - %s: %.1f%s\n", s.name(), s.quantity(), s.qtyUnit().abbr());
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