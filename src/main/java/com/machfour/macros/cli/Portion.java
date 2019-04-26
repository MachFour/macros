package com.machfour.macros.cli;

import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.FoodPortion;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.util.FoodPortionSpec;
import com.sun.org.apache.xpath.internal.Arg;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.machfour.macros.cli.CliMain.OUT;
import static com.machfour.macros.cli.CliMain.PROGNAME;

class Portion extends CommandImpl {
    private static final String NAME = "portion";
    @Override
    public String name() {
        return NAME;
    }
    @Override
    public void printHelp(PrintStream out) {
        out.printf("Usage: %s %s [ <meal name> [<day>] -s ] <portion spec> [<portion spec> ... ]\n", PROGNAME, NAME);
    }
    @Override
    public void doAction(List<String> args) {
        if (args.size() == 1 || args.contains("--help")) {
            printHelp(OUT);
            return;
        }
        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
        // MealSpec mealSpec = MealSpec.makeMealSpec(args);

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
                OUT.println("There can only be at most two arguments before '-s'");
                return;
        }

        if (!mealSpec.mealSpecified()) {
            OUT.printf("No meal specified, assuming %s on %s\n", mealSpec.name(), CliUtils.prettyDay(mealSpec.day()));
        }
        mealSpec.process(db, true);
        if (mealSpec.error() != null) {
            OUT.println(mealSpec.error());
            return;
        }

        // now read everything after the '-s' as food portion specs
        List<FoodPortionSpec> specs = new ArrayList<>(args.size() - 1 - separator);
        for (int index = separator + 1; index < args.size(); ++index) {
            specs.add(FileParser.makefoodPortionSpecFromLine(args.get(index)));
        }

        process(mealSpec.processedObject(), specs, db);

    }

    static void process(Meal toAddTo, List<FoodPortionSpec> specs, MacrosDatabase db) {
        if (specs.isEmpty()) {
            OUT.println("No food portions specified, nothing to do");
            return;
        }

        FoodPortionSpec spec = specs.get(0);

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
        MealPrinter.printMeal(toAddTo, false, OUT);

    }
}
