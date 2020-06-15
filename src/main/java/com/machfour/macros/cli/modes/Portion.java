package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.cli.utils.FileParser;
import com.machfour.macros.cli.utils.MealPrinter;
import com.machfour.macros.cli.utils.MealSpec;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.queries.FoodQueries;
import com.machfour.macros.queries.MealQueries;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.util.FoodPortionSpec;
import com.machfour.macros.util.PrintFormatting;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;



public class Portion extends CommandImpl {
    private static final String NAME = "portion";
    private static final String USAGE =
            String.format("Usage: %s %s [ <meal name> [<day>] -s ] <portion spec> [<portion spec> ... ]", config.getProgramName(), NAME);

    public Portion() {
        super(NAME, USAGE);
    }

    @Override
    public int doAction(List<String> args) {
        if (args.size() == 1 || args.contains("--help")) {
            printHelp();
            return 0;
        }
        MacrosDataSource ds =  config.getDataSourceInstance();
        // MealSpec mealSpec = MealSpec.makeMealSpec(args);

        // TODO use argParsing here
        // check position of -s to decide how to parse arguments
        MealSpec mealSpec;
        int separator = args.indexOf("-s");
        switch (separator) {
            case 1:
                // it's the first argument after 'portion'
                // so basically there's no meal info anyway
                // fall-through to no flag
            case -1:
                // no meal info specified
                mealSpec = MealSpec.makeMealSpec();
                break;
            case 2:
                // args looks like ["portion", "<meal name>", "-s", ...]
                mealSpec = MealSpec.makeMealSpec(args.get(1));
                break;
            case 3:
                // args looks like ["portion", "<meal name>", "<day>", "-s", ...]
                mealSpec = MealSpec.makeMealSpec(args.get(1), args.get(2));
                break;
            case 0:
                assert false : "'-s' is where the command name should be!";
            default:
                out.println("There can only be at most two arguments before '-s'");
                return 1;
        }

        if (!mealSpec.mealSpecified()) {
            out.printf("No meal specified, assuming %s on %s\n", mealSpec.name(), PrintFormatting.prettyDay(mealSpec.day()));
        }
        mealSpec.process(ds, true);
        if (mealSpec.error() != null) {
            err.println(mealSpec.error());
            return 1;
        }

        // now read everything after the '-s' as food portion specs
        List<FoodPortionSpec> specs = new ArrayList<>(args.size() - 1 - separator);
        for (int index = separator + 1; index < args.size(); ++index) {
            specs.add(FileParser.makefoodPortionSpecFromLine(args.get(index)));
        }

        return process(mealSpec.processedObject(), specs, ds, out, err);

    }

    static int process(Meal toAddTo, List<FoodPortionSpec> specs, MacrosDataSource ds, PrintStream out, PrintStream err) {
        if (specs.isEmpty()) {
            out.println("No food portions specified, nothing to do");
            return 0;
        }

        FoodPortionSpec spec = specs.get(0);

        Food f;

        try {
            f = FoodQueries.getFoodByIndexName(ds, spec.foodIndexName);
        } catch (SQLException e) {
            err.println("Could not retrieve food. Reason: " + e.getMessage());
            return 1;
        }
        if (f == null) {
            err.printf("Unrecognised food with index name '%s'\n", spec.foodIndexName);
            return 1;
        }
        FileParser.processFpSpec(spec, toAddTo, f);
        if (spec.error != null) {
            err.println(spec.error);
            return 1;
        }
        assert (spec.createdObject != null) : "No object created but no error message either";
        toAddTo.addFoodPortion(spec.createdObject);

        try {
            MealQueries.saveFoodPortions(ds, toAddTo);
        } catch (SQLException e) {
            err.println("Error saving food portion. Reason: " + e.getMessage());
            return 1;
        }
        out.println();
        MealPrinter.printMeal(toAddTo, false, out);
        return 0;
    }
}
