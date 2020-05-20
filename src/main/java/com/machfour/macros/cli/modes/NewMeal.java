package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.cli.utils.ArgParsing;
import com.machfour.macros.cli.utils.MealSpec;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.util.PrintFormatting;

import java.util.List;



public class NewMeal extends CommandImpl {
    private static final String NAME = "newmeal";
    private static final String USAGE = String.format("Usage: %s %s <meal name> [<day>]", config.getProgramName(), NAME);

    public NewMeal() {
        super(NAME, USAGE);
    }

    @Override
    public void doActionNoExitCode(List<String> args) {
        if (args.size() == 1 || args.contains("--help")) {
            printHelp();
            return;
        }

        // cases: day not specified vs day specified
        // meal exists with that name, or doesn't exist
        MacrosDataSource ds =  config.getDataSourceInstance();

        ArgParsing.Result mealNameArg = ArgParsing.findArgument(args, 1);
        ArgParsing.Result dayArg = ArgParsing.findArgument(args, 2);
        MealSpec mealSpec = MealSpec.makeMealSpec(mealNameArg, dayArg);

        mealSpec.process(ds, true);

        if (mealSpec.error() != null) {
            out.println(mealSpec.error());
            return;
        }
        if (mealSpec.created()) {
            String prettyDay = PrintFormatting.prettyDay(mealSpec.day());
            out.println(String.format("Created meal '%s' on %s", mealSpec.name(), prettyDay));
        }
        //Meal toEdit = mealSpec.processedObject();
    }
}
