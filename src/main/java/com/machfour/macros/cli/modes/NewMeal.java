package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.cli.utils.ArgParsing;
import com.machfour.macros.cli.utils.MealSpec;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.util.DateStamp;

import java.util.List;



public class NewMeal extends CommandImpl {
    private static final String NAME = "newmeal";
    private static final String USAGE = String.format("Usage: %s %s <meal name> [<day>]", getProgramName(), NAME);

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

        if (mealSpec.getError() != null) {
            out.println(mealSpec.getError());
            return;
        }
        if (mealSpec.isCreated()) {
            String prettyDay = DateStamp.prettyPrint(mealSpec.getDay());
            out.println(String.format("Created meal '%s' on %s", mealSpec.getName(), prettyDay));
        }
        //Meal toEdit = mealSpec.processedObject();
    }
}
