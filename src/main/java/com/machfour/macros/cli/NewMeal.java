package com.machfour.macros.cli;

import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.util.DateStamp;

import java.io.PrintStream;
import java.util.List;

import static com.machfour.macros.cli.CliMain.OUT;
import static com.machfour.macros.cli.CliMain.PROGNAME;

public class NewMeal extends CommandImpl {

    private static final String NAME = "newmeal";
    @Override
    public String name() {
        return NAME;
    }
    @Override
    public void printHelp(PrintStream out) {
        out.printf("Usage: %s %s <meal name> [<day>]\n", PROGNAME, NAME);
    }
    @Override
    public void doAction(List<String> args) {
        if (args.size() == 1 || args.contains("--help")) {
            printHelp(OUT);
            return;
        }

        // cases: day not specified vs day specified
        // meal exists with that name, or doesn't exist
        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);

        ArgParsing.Result mealNameArg = ArgParsing.findArgument(args, 1);
        ArgParsing.Result dayArg = ArgParsing.findArgument(args, 2);
        MealSpec mealSpec = MealSpec.makeMealSpec(mealNameArg, dayArg);

        mealSpec.process(db, true);

        if (mealSpec.error() != null) {
            OUT.println(mealSpec.error());
            return;
        }
        if (mealSpec.created()) {
            String prettyDay = CliUtils.prettyDay(mealSpec.day());
            OUT.println(String.format("Created meal '%s' on %s", mealSpec.name(), prettyDay));
        }
        //Meal toEdit = mealSpec.processedObject();
    }
}
