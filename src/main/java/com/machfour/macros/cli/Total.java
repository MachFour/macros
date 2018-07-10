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

class Total extends ModeImpl {
    private static final String NAME = "total";
    @Override
    public String name() {
        return NAME;
    }
    @Override
    public void printHelp(PrintStream out) {
        OUT.printf("Usage: %s %s [-m <meal name>] [-d <day>] [-v|--verbose]\n", PROGNAME, NAME);
    }
    @Override
    public void doAction(List<String> args) {
        if (args.contains("--help")) {
            printHelp(OUT);
            return;
        }
        boolean verbose = args.contains("--verbose") || args.contains("-v");
        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
        MealSpec mealSpec = CliUtils.makeMealSpec(args);
        process(mealSpec, db, verbose);

    }

    static void process(MealSpec mealSpec, MacrosDatabase db, boolean verbose) {
        /*
        if (!mealSpec.mealSpecified) {
            OUT.println("Assuming " + mealSpec.name + " on " + CliUtils.prettyDay(mealSpec.day));
            OUT.println();
        }
        */
        CliUtils.processMealSpec(mealSpec, db, false);
        if (mealSpec.error != null) {
            OUT.println(mealSpec.error);
            return;
        }
        OUT.println();
        MealPrinter.printMeal(mealSpec.createdObject, OUT);
    }

}
