package com.machfour.macros.cli.modes;

import com.machfour.macros.cli.CommandImpl;
import com.machfour.macros.cli.utils.ArgParsing;
import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.util.DateStamp;
import com.machfour.macros.util.PrintFormatting;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.machfour.macros.linux.Config.PROGNAME;

public class Meals extends CommandImpl {
    private static final String NAME = "meals";
    private static final String USAGE = String.format("Usage: %s %s [day]", PROGNAME, NAME);

    public Meals() {
        super(NAME, USAGE);
    }

    @Override
    public void doActionNoExitCode(List<String> args) {
        if (args.contains("--help")) {
            printHelp();
            return;
        }

        // cases: day not specified vs day specified
        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
        ArgParsing.Result dateArgument = ArgParsing.findArgument(args, 1);
        DateStamp d = null;
        switch (dateArgument.status()) {
            case NOT_FOUND:
                d = DateStamp.forCurrentDate();
                break;
            case OPT_ARG_MISSING:
                out.println("-d option requires a day specified");
                break;
            case ARG_FOUND:
                d = ArgParsing.dayStringParse(dateArgument.argument());
                break;
            default:
                out.println("Invalid date format: '" + dateArgument.argument() + "'.");
                break;
        }
        if (d != null) {
            printMealList(db, d);
        }

        //Meal toEdit = mealSpec.processedObject();
    }

    private void printMealList(MacrosDataSource db, DateStamp d) {
        try {
            Map<String, Meal> meals = db.getMealsForDay(d);
            if (meals.isEmpty()) {
                out.println("No meals recorded on " + PrintFormatting.prettyDay(d));
            } else {
                out.println("Meals recorded on " + PrintFormatting.prettyDay(d) + ":");
                out.println();
                out.println(String.format("%-16s %-16s", "Name", "Last Modified"));
                out.println("=================================");
                SimpleDateFormat dateFormat = new SimpleDateFormat();
                for (Meal m : meals.values()) {
                    Date mealDate = new Date(m.modifyTime()*1000);
                    out.println(String.format("%-16s %16s", m.getName(), dateFormat.format(mealDate)));
                }
            }
        } catch (SQLException e) {
            out.println("SQL Exception: " + e.getMessage());
        }

    }
}
