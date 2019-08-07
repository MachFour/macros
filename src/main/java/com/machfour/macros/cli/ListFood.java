package com.machfour.macros.cli;

import com.machfour.macros.core.Schema;
import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.objects.Serving;
import com.machfour.macros.storage.MacrosDatabase;

import java.io.PrintStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.machfour.macros.cli.CliMain.PROGNAME;
import static com.machfour.macros.cli.CliMain.OUT;
import static com.machfour.macros.cli.CliUtils.deNull;

class ListFood extends CommandImpl {
    private static final String NAME = "list";
    @Override
    public String name() {
        return NAME;
    }
    @Override
    public void printHelp(PrintStream out) {
        OUT.printf("Usage: %s %s <index_name>\n", PROGNAME, NAME);
    }
    @Override
    public void doAction(List<String> args) {
        if (args.size() == 1 || args.contains("--help")) {
            printHelp(OUT);
            if (args.size() == 1) {
                OUT.println("Please enter the index name of the food to list");
            }
            return;
        }
        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
        String indexName = args.get(1);
        Food foodToList = null;
        try {
            foodToList = db.getFoodByIndexName(indexName);
        } catch (SQLException e) {
            OUT.print("SQL exception occurred: ");
            OUT.println(e.getErrorCode());
        }
        if (foodToList == null) {
            OUT.printf("No food found with index name %s\n", indexName);
            return;
        }

        printFood(foodToList, OUT);
    }

    public static void printFood(Food f, PrintStream out) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());

        out.println("============");
        out.println(" Food Data  ");
        out.println("============");
        out.println();
        out.printf("Name:          %s\n", f.getMediumName());
        out.printf("Notes:         %s\n", deNull(f.getNotes()));
        out.printf("Category:      %s\n", f.getFoodCategory());

        out.println();
        out.printf("Type:          %s\n", f.getFoodType().getName());
        out.printf("Created on:    %s\n", dateFormat.format(f.createDate()));
        out.printf("Last modified: %s\n", dateFormat.format(f.modifyDate()));
        out.println();

        out.println("================================");
        out.println();
        /*
         * Nutrition data
         */
        NutritionData nd = f.getNutritionData();
        String unit = nd.qtyUnit().abbr();
        out.printf("Nutrition data (source: %s)\n", nd.getData(Schema.NutritionDataTable.DATA_SOURCE));
        out.println();

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

        out.println("================================");
        out.println();

        /*
         * Servings
         */
        List<Serving> servings = f.getServings();
        out.println("Servings:");
        out.println();
        if (!servings.isEmpty()) {
            for (Serving s: servings) {
                out.printf(" - %s: %.1f%s\n", s.name(), s.quantity(), s.qtyUnit().abbr());
            }
        } else {
            out.println("No servings recorded for this food");
        }
    }
}
