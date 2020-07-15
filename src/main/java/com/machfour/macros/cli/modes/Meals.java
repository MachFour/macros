package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.cli.utils.ArgParsing;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.queries.MealQueries;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.util.DateStamp;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;



public class Meals extends CommandImpl {
    private static final String NAME = "meals";
    private static final String USAGE = String.format("Usage: %s %s [day]", config.getProgramName(), NAME);

    public Meals() {
        super(NAME, USAGE);
    }

    @Override
    public int doAction(List<String> args) {
        if (args.contains("--help")) {
            printHelp();
            return -1;
        }

        // cases: day not specified vs day specified
        MacrosDataSource ds =  config.getDataSourceInstance();
        ArgParsing.Result dateArgument = ArgParsing.findArgument(args, 1);
        DateStamp d;
        switch (dateArgument.status()) {
            case NOT_FOUND:
                d = DateStamp.currentDate();
                break;
            case OPT_ARG_MISSING:
                out.println("-d option requires a day specified");
                return 1;
            case ARG_FOUND:
                d = ArgParsing.dayStringParse(dateArgument.argument());
                break;
            default:
                out.println("Invalid date format: '" + dateArgument.argument() + "'.");
                return 1;
        }
        if (d != null) {
            return printMealList(ds, d);
        } else {
            err.println("Meal not found");
            return 1;
        }
        //Meal toEdit = mealSpec.processedObject();
    }

    private int printMealList(MacrosDataSource db, DateStamp d) {
        try {
            Map<Long, Meal> meals = MealQueries.getMealsForDay(db, d);
            if (meals.isEmpty()) {
                out.println("No meals recorded on " + DateStamp.prettyPrint(d));
            } else {
                out.println("Meals recorded on " + DateStamp.prettyPrint(d) + ":");
                out.println();
                out.println(String.format("%-16s %-16s", "Name", "Last Modified"));
                out.println("=================================");
                SimpleDateFormat dateFormat = new SimpleDateFormat();
                for (Meal m : meals.values()) {
                    Date mealDate = new Date(m.getModifyTime()*1000);
                    out.println(String.format("%-16s %16s", m.getName(), dateFormat.format(mealDate)));
                }
            }
        } catch (SQLException e) {
            out.println("SQL Exception: " + e.getMessage());
            return 1;
        }
        return 0;
    }
}
