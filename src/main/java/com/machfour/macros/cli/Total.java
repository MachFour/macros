package com.machfour.macros.cli;

import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.storage.MacrosDatabase;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
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
        OUT.printf("Usage: %s %s (<meal name>|--all) [<day>] [-v|--verbose] [--per100]\n", PROGNAME, NAME);
    }

    private static MealSpec makeMealSpec(List<String> args, boolean isAllMeals) {
        /* logic:
         * if isAllMeals is true:
         *     day is the first thing that doesn't start with -- or -
         * if isAllMeals is false:
         *     meal name is the first thing that doesn't start with -- or -
         *     day is the second such thing
         */
        List<String> nonOptionArgs = new ArrayList<>(args.size() - 1);
        for (String arg : args) {
            // add everything after the first arg (which is the mode name) which doesn't start with a -
            if (!(arg.equals(args.get(0)) || arg.startsWith("-"))) {
                nonOptionArgs.add(arg);
            }
        }
        String mealName = null;
        String dayString = null;
        if (nonOptionArgs.size() >= 1) {
            if (isAllMeals) {
                // just look for day
                dayString = nonOptionArgs.get(0);
            } else {
                // look for day and meal
                mealName = nonOptionArgs.get(0);
                if (nonOptionArgs.size() >= 2) {
                    dayString = nonOptionArgs.get(1);
                }
            }
        }
        return MealSpec.makeMealSpec(mealName, dayString);
    }
    @Override
    public void doAction(List<String> args) {
        if (args.contains("--help")) {
            printHelp(OUT);
            return;
        }
        boolean verbose = args.contains("--verbose") || args.contains("-v");
        boolean per100 = args.contains("--per100");
        boolean allMeals = args.contains("--all");

        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
        MealSpec spec = makeMealSpec(args, allMeals);
        process(spec, db, allMeals, verbose, per100);

    }

    static void process(MealSpec mealSpec, MacrosDatabase db, boolean allMeals, boolean verbose, boolean per100) {
        if (!allMeals) {
            // total for specific meal
            mealSpec.process(db, false);
            if (mealSpec.error() != null) {
                OUT.println(mealSpec.error());
                return;
            }
            OUT.println();
            MealPrinter.printMeal(mealSpec.processedObject(), verbose, OUT);

        } else {
            try {
                Map<String, Meal> mealsForDay = db.getMealsForDay(mealSpec.day());
                if (mealsForDay.isEmpty()) {
                    OUT.println("No meals recorded on " + CliUtils.prettyDay(mealSpec.day()));
                } else {
                    MealPrinter.printMeals(mealsForDay.values(), OUT, verbose, per100, true);
                }
            } catch (SQLException e) {
                OUT.println("Error retrieving meals: " + e.getMessage());
                OUT.println();
            }
        }
    }

}
