package com.machfour.macros.cli;

import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.util.DateStamp;

import java.io.PrintStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.machfour.macros.cli.CliMain.OUT;
import static com.machfour.macros.cli.CliMain.PROGNAME;

public class Meals extends CommandImpl {

    private static final String NAME = "meals";
    @Override
    public String name() {
        return NAME;
    }
    @Override
    public void printHelp(PrintStream out) {
        OUT.printf("Usage: %s %s [day]\n", PROGNAME, NAME);
    }
    @Override
    public void doAction(List<String> args) {
        if (args.contains("--help")) {
            printHelp(OUT);
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
                OUT.println("-d option requires a day specified");
                break;
            case ARG_FOUND:
                d = ArgParsing.dayStringParse(dateArgument.argument());
                break;
            default:
                OUT.println("Invalid date format: '" + dateArgument.argument() + "'.");
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
                OUT.println("No meals recorded on " + CliUtils.prettyDay(d));
            } else {
                OUT.println("Meals recorded on " + CliUtils.prettyDay(d) + ":");
                OUT.println();
                OUT.println(String.format("%-16s %-16s", "Name", "Last Modified"));
                OUT.println("=================================");
                SimpleDateFormat dateFormat = new SimpleDateFormat();
                for (Meal m : meals.values()) {
                    Date mealDate = new Date(m.modifyTime()*1000);
                    OUT.println(String.format("%-16s %16s", m.getName(), dateFormat.format(mealDate)));
                }
            }
        } catch (SQLException e) {
            OUT.println("SQL Exception: " + e.getMessage());
        }

    }
}
