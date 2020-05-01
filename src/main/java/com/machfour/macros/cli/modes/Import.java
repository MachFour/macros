package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.core.Schema;
import com.machfour.macros.core.datatype.TypeCastException;
import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.*;
import com.machfour.macros.storage.CsvImport;
import com.machfour.macros.storage.MacrosDatabase;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.List;

import static com.machfour.macros.linux.Config.PROGNAME;
import static com.machfour.macros.util.MiscUtils.toList;

public class Import extends CommandImpl {
    private static final String NAME = "import";
    private static final String USAGE = String.format("%s %s [--clear] [--norecipes] [--nofoods]", PROGNAME, NAME);

    public Import() {
        super(NAME, USAGE);
    }

    @Override
    public void printHelp() {
        out.println("Imports CSV data for foods, servings, recipes and ingredients into the database.");
        out.println("Only foods with index names not already in the database will be imported.");
        out.println("However, it will try to import all servings, and so will fail if duplicate servings exist.");
        out.println("Options:");
        out.println("  --clear       removes existing data before import");
        out.println("  --nofoods     prevents import of food and serving data (and clearing if --clear is used)");
        out.println("  --norecipes   prevents import of recipe data (and clearing if --clear is used)");

    }

    @Override
    public int doAction(List<String> args) {
        if (args.contains("--help")) {
            printHelp();
            return 0;
        }
        boolean doClear = args.contains("--clear");
        boolean noRecipes = args.contains("--norecipes");
        boolean noFoodsServings = args.contains("--nofoods");

        String foodCsvFile = Config.FOOD_CSV_PATH;
        String servingCsvFile = Config.SERVING_CSV_PATH;
        String recipeCsvFile = Config.RECIPE_CSV_PATH;
        String ingredientsCsvFile = Config.INGREDIENTS_CSV_PATH;
        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);

        try {
            if (doClear) {
                if (!noFoodsServings) {
                    out.println("Clearing existing foods, servings, nutrition data and ingredients...");
                    // have to clear in reverse order
                    db.clearTable(Ingredient.table());
                    db.clearTable(Serving.table());
                    db.clearTable(NutritionData.table());
                    db.clearTable(Food.table());
                } else if (!noRecipes) {
                    out.println("Clearing existing recipes and ingredients...");
                    // have to clear nutrition data first
                    db.deleteByColumn(Food.table(), Schema.FoodTable.FOOD_TYPE, toList(FoodType.COMPOSITE.getName()));
                    db.clearTable(Ingredient.table());
                } else {
                    out.println("Warning: nothing was cleared because both --nofoods and --norecipes were used");
                }
            }
            if (!noFoodsServings) {
                try (Reader foodCsv = new FileReader(foodCsvFile);
                     Reader servingCsv = new FileReader(servingCsvFile)) {
                    out.println("Importing foods and nutrition data into database...");
                    CsvImport.importFoodData(foodCsv, db, false);
                    out.println("Saved foods and nutrition data");
                    CsvImport.importServings(servingCsv, db, false);
                    out.println("Saved servings");
                    out.println();
                }
            }

            if (!noRecipes) {
                try (Reader recipeCsv = new FileReader(recipeCsvFile);
                     Reader ingredientsCsv = new FileReader(ingredientsCsvFile)) {
                    out.println("Importing recipes and ingredients into database...");
                    CsvImport.importRecipes(recipeCsv, ingredientsCsv, db);
                    out.println("Saved recipes and ingredients");
                    out.println();
                }
            }
        } catch (SQLException e1) {
            out.println();
            err.println("SQL Exception occurred: " + e1.getMessage());
            return 1;
        } catch (IOException e2) {
            out.println();
            err.println("IO exception occurred: " + e2.getMessage());
            return 1;
        } catch (TypeCastException e3) {
            out.println();
            err.println("Type cast exception occurred: " + e3.getMessage());
            err.println("Please check the format of the CSV files");
            return 1;
        }

        out.println();
        out.println("Import completed successfully");
        return 0;
    }
}
