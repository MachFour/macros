package com.machfour.macros.cli;

import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.storage.MacrosDatabase;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;

import static com.machfour.macros.cli.CliMain.OUT;
import static com.machfour.macros.cli.CliMain.PROGNAME;

class Portion extends ModeImpl {
    private static final String NAME = "portion";
    @Override
    public String name() {
        return NAME;
    }
    @Override
    public void printHelp(PrintStream out) {
        out.printf("Usage: %s %s [-m <meal name>] [-d <day>] <portion spec>\n", PROGNAME, NAME);
    }
    @Override
    public void doAction(List<String> args) {
        if (args.size() == 1 || args.contains("--help")) {
            printHelp(OUT);
            return;
        }
        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
        MealSpec mealSpec = CliUtils.makeMealSpec(args);

        String foodPortionArg = args.get(args.size() - 1);
        FoodPortionSpec spec = FileParser.makefoodPortionSpecFromLine(foodPortionArg);

        process(mealSpec, spec, db);

    }

    private static void process(MealSpec mealSpec, FoodPortionSpec spec, MacrosDatabase db) {
        if (mealSpec.error != null) {
            OUT.println(mealSpec.error);
            return;
        }
        if (!mealSpec.mealSpecified) {
            OUT.println("No meal specified, assuming " + mealSpec.createdObject.getName() + " on " + CliUtils.prettyDay(mealSpec.day));
        }
        try {
            CliUtils.processMealSpec(mealSpec, db, true);
        } catch (SQLException e) {
            OUT.println("Error retrieving meal: " + e.getMessage());
            return;
        }
        if (mealSpec.error != null) {
            OUT.println(mealSpec.error);
            return;
        }

        assert (mealSpec.createdObject != null) : "No error message but no created object";
        Meal toAddTo = mealSpec.createdObject;

        // now read the food portion
        // assume it's last arg
        Food f;
        try {
            f = db.getFoodByIndexName(spec.foodIndexName);
        } catch (SQLException e) {
            OUT.println("Could not retrieve food. Reason: " + e.getMessage());
            return;
        }
        if (f == null) {
            OUT.printf("Unrecognised food with index name '%s'\n", spec.foodIndexName);
            return;
        }
        FileParser.processFpSpec(spec, toAddTo, f);
        if (spec.error != null) {
            OUT.println(spec.error);
            return;
        }
        assert (spec.createdObject != null) : "No object created but no error message either";
        toAddTo.addFoodPortion(spec.createdObject);

        try {
            db.saveFoodPortions(toAddTo);
        } catch (SQLException e) {
            OUT.println("Error saving food portion. Reason: " + e.getMessage());
            return;
        }
        OUT.println();
        OUT.println("Meal total:");
        MealPrinter.printMeal(toAddTo, OUT);

    }

}
