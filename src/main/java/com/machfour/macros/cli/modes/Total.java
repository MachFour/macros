package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.cli.utils.MealPrinter;
import com.machfour.macros.cli.utils.MealSpec;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.queries.MealQueries;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.util.PrintFormatting;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



/*
 * Prints out totals for all DB recorded meals in a day
 */
public class Total extends CommandImpl {
    private static final String NAME = "total";
    private static final String USAGE = String.format("Usage: %s %s (<meal name>|--all) [<day>] [-v|--verbose] [--per100]", config.getProgramName(), NAME);

    public Total() {
        super(NAME, USAGE);
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
    public int doAction(List<String> args) {
        if (args.contains("--help")) {
            printHelp();
            return 0;
        }
        boolean verbose = args.contains("--verbose") || args.contains("-v");
        boolean per100 = args.contains("--per100");
        boolean allMeals = args.contains("--all");

        MacrosDataSource ds =  config.getDataSourceInstance();
        MealSpec spec = makeMealSpec(args, allMeals);
        return process(spec, ds, allMeals, verbose, per100);

    }

    int process(MealSpec mealSpec, MacrosDataSource ds, boolean allMeals, boolean verbose, boolean per100) {
        if (!allMeals) {
            // total for specific meal
            mealSpec.process(ds, false);
            if (mealSpec.error() != null) {
                err.println(mealSpec.error());
                return 1;
            }
            out.println();
            MealPrinter.printMeal(mealSpec.processedObject(), verbose, out);

        } else {
            try {
                Map<String, Meal> mealsForDay = MealQueries.getMealsForDay(ds, mealSpec.day());
                if (mealsForDay.isEmpty()) {
                    out.println("No meals recorded on " + PrintFormatting.prettyDay(mealSpec.day()));
                } else {
                    MealPrinter.printMeals(mealsForDay.values(), out, verbose, per100, true);
                }
            } catch (SQLException e) {
                out.println();
                err.println("Error retrieving meals: " + e.getMessage());
                return 1;
            }
        }
        return 0;
    }

}
