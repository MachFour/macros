package com.machfour.macros.cli;

import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.util.FoodPortionSpec;
import com.machfour.macros.util.PrintFormatting;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.machfour.macros.cli.CliMain.OUT;
import static com.machfour.macros.cli.CliMain.PROGNAME;

class Portion extends CommandImpl {
    private static final String NAME = "portion";
    private static final String USAGE =
            String.format("Usage: %s %s [ <meal name> [<day>] -s ] <portion spec> [<portion spec> ... ]", PROGNAME, NAME);

    Portion() {
        super(NAME, USAGE);
    }

    @Override
    public void doAction(List<String> args) {
        if (args.size() == 1 || args.contains("--help")) {
            printHelp();
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
                out.println("There can only be at most two arguments before '-s'");
                return;
        }

        if (!mealSpec.mealSpecified()) {
            out.printf("No meal specified, assuming %s on %s\n", mealSpec.name(), PrintFormatting.prettyDay(mealSpec.day()));
        }
        mealSpec.process(db, true);
        if (mealSpec.error() != null) {
            out.println(mealSpec.error());
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
            out.println("No food portions specified, nothing to do");
            return;
        }

        FoodPortionSpec spec = specs.get(0);

        Food f;

        try {
            f = db.getFoodByIndexName(spec.foodIndexName);
        } catch (SQLException e) {
            out.println("Could not retrieve food. Reason: " + e.getMessage());
            return;
        }
        if (f == null) {
            out.printf("Unrecognised food with index name '%s'\n", spec.foodIndexName);
            return;
        }
        FileParser.processFpSpec(spec, toAddTo, f);
        if (spec.error != null) {
            out.println(spec.error);
            return;
        }
        assert (spec.createdObject != null) : "No object created but no error message either";
        toAddTo.addFoodPortion(spec.createdObject);

        try {
            db.saveFoodPortions(toAddTo);
        } catch (SQLException e) {
            out.println("Error saving food portion. Reason: " + e.getMessage());
            return;
        }
        out.println();
        MealPrinter.printMeal(toAddTo, false, OUT);

    }
}