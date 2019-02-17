package com.machfour.macros.cli;

import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.storage.MacrosDatabase;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.machfour.macros.cli.CliMain.OUT;
import static com.machfour.macros.cli.CliMain.PROGNAME;

class Total extends CommandImpl {
    private static final String NAME = "total";
    @Override
    public String name() {
        return NAME;
    }
    @Override
    public void printHelp(PrintStream out) {
        OUT.printf("Usage: %s %s [-m <meal name>] [-d <day>] [-v|--verbose] [--per100]\n", PROGNAME, NAME);
    }
    @Override
    public void doAction(List<String> args) {
        if (args.contains("--help")) {
            printHelp(OUT);
            return;
        }
        boolean verbose = args.contains("--verbose") || args.contains("-v");
        boolean per100 = args.contains("--per100");
        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
        MealSpec mealSpec = MealSpec.makeMealSpec(args);
        process(mealSpec, db, verbose, per100);

    }

    static void process(MealSpec mealSpec, MacrosDatabase db, boolean verbose, boolean per100) {
        if (mealSpec.mealSpecified()) {
            // total for specific meal
            mealSpec.processMealSpec(db, false);
            if (mealSpec.error() != null) {
                OUT.println(mealSpec.error());
                return;
            }
            OUT.println();
            MealPrinter.printMeal(mealSpec.processedObject(), verbose, OUT);

        } else {
            try {
                Map<String, Meal> mealsForDay = db.getMealsForDay(mealSpec.day());
                MealPrinter.printMeals(mealsForDay.values(), OUT, verbose, per100, true);
            } catch (SQLException e) {
                OUT.println("Error retrieving meals: " + e.getMessage());
                OUT.println();
            }
        }
    }

}
