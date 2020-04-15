package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.cli.utils.CliUtils;
import com.machfour.macros.ingredients.IngredientsParser;
import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.CompositeFood;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.storage.MacrosDatabase;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.machfour.macros.linux.Config.PROGNAME;

public class Recipe extends CommandImpl {
    private static final String NAME = "recipe";
    private static final String USAGE = String.format("Usage: %s %s <recipes.json>", PROGNAME, NAME);

    public Recipe() {
        super(NAME, USAGE);
    }

    @Override
    public void doActionNoExitCode(List<String> args) {
        if (args.contains("--help")) {
            printHelp();
            return;
        } else if (args.size() < 2) {
            out.println(usage());
            return;
        }

        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
        List<CompositeFood> recipes = new ArrayList<>();

        try (Reader jsonReader = new FileReader(args.get(1))) {
            out.println("Importing recipes...");
            recipes.addAll(IngredientsParser.readRecipes(jsonReader, db));
        } catch (IOException e1) {
            out.println("IO exception occurred while reading recipes file: " + e1.getLocalizedMessage());
        } catch (SQLException e2) {
            out.println("SQL exception occurred while creating recipe objects: " + e2.getMessage());
        }

        if (recipes.isEmpty()) {
            out.println("No recipes read! Check the recipes file");
            return;
        }

        out.println("The following recipes were found:");

        for (CompositeFood cf : recipes) {
            out.println();
            out.println("Name: " + cf.getMediumName());
            out.println();
            out.println("Ingredients:");
            out.println();
            CliUtils.printIngredients(cf.getIngredients(), out);
            out.println();
            out.println("Nutrition Information:");
            out.println();
            NutritionData nd = cf.getNutritionData();
            String unit = nd.qtyUnitAbbr();
            // if entered not per 100g, print both original amount and per 100 g
            if (nd.getQuantity() != 100) {
                out.printf("Per %.0f%s:\n", nd.getQuantity(), unit);
                CliUtils.printNutritionData(nd, false, out);
                out.println();
                nd = nd.rescale(100);
            }
            out.printf("Per %.0f%s:\n", nd.getQuantity(), unit); // should now be 100
            CliUtils.printNutritionData(nd, false, out);
            out.println();
            out.println("================================================");
            out.println();
        }

        String article = recipes.size() == 1 ? "this" : "these";
        String plural = recipes.size() == 1 ? "" : "s";
        out.print("Would you like to save " + article + " food" + plural + "? [y/N] ");
        char response = CliUtils.getChar(in, out);
        out.println();
        if (response == 'y' || response == 'Y') {
            try {
                IngredientsParser.saveRecipes(recipes, db);
                out.println("Recipes saved!");
            } catch (SQLException e) {
                out.println("SQL exception occurred while saving recipe objects: " + e.getMessage());
                out.println("Recipes not saved");
            }
        } else {
            out.println("Okay.");
        }
    }

}
