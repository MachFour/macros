package com.machfour.macros.cli;

import com.machfour.macros.linux.Config;
import com.machfour.macros.linux.LinuxDatabase;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.util.DateStamp;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.machfour.macros.cli.CliMain.OUT;
import static com.machfour.macros.cli.CliMain.PROGNAME;

class Portion extends ModeImpl {
    private static final String NAME = "portion";
    @Override
    public String name() {
        return NAME;
    }
    @Override
    public void doAction(List<String> args) {
        if (args.size() == 1) {
            OUT.printf("Usage: %s %s [-m <meal name>] [-d <day>] <portion spec>\n", PROGNAME, args.get(0));
            OUT.println("Please enter a search keyword for the food database");
            return;
        }
        MacrosDatabase db = LinuxDatabase.getInstance(Config.DB_LOCATION);
        DateStamp day;
        String mealName;

        // get day
        int dayArg = args.indexOf("-d") + 1;
        if (dayArg == 0) {
            // arg not found
            day = DateStamp.forCurrentDate();
        } else if (dayArg >= args.size()) {
            OUT.println("-d option requires an argument: <day>");
            return;
        } else {
            day = getDateStamp(args.get(dayArg));
            if (day == null) {
                OUT.printf("Invalid day format: '%s'\n", args.get(dayArg));
                OUT.println("Must be a number (e.g. 0 for today, -1 for yesterday), or a date: yyyy-mm-dd");
                return;
            }
        }

        // get meal name
        int mealArg = args.indexOf("-m") + 1;
        if (mealArg == 0) {
            // current meal, current day
            mealName = "current";
        } else if (mealArg >= args.size()) {
            OUT.println("-m option requires an argument: <meal>");
            return;
        } else {
            mealName = args.get(mealArg);
        }

        Meal toAddTo;

        // cases:
        // no meal specified -> use current meal (exists)
        // no meal specified -> no meal exists
        // meal specified that exists -> use it
        // meal specified that does not exist -> create it
        if (mealName.equals("current")) {
            Map<String, Meal> mealsForDay;
            try {
                mealsForDay = db.getMealsForDay(day);
                if (!mealsForDay.isEmpty()) {
                    // use most recently modified meal today
                    toAddTo = Collections.max(mealsForDay.values(), Comparator.comparingLong(Meal::getModifyTime));
                    OUT.println("No meal specified, assuming " + toAddTo.getName() + " on " + prettyDay(day));
                } else {
                    OUT.print("No meals exist on " + prettyDay(day) + ". Please specify one using -m <meal name>");
                    return;
                }
            } catch (SQLException e) {
                OUT.println("Could not get meals for " + prettyDay(day));
                OUT.println("Reason: " + e.getMessage());
                return;
            }
        } else {
            try {
                toAddTo = db.getOrCreateMeal(day, mealName);
            } catch (SQLException e) {
                OUT.println("Could not create meal. Reason: " + e.getMessage());
                return;
            }
        }

        // now read the food portion
        // assume it's last arg
        String foodPortionArg = args.get(args.size() - 1);
        FoodPortionSpec spec = FileParser.makefoodPortionSpecFromLine(foodPortionArg);
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
        OUT.println("Meal total:");
        MealPrinter.printMeal(toAddTo, OUT);
    }

    // returns null for invalid, today if flag not found, or otherwise decodes day from argument string
    private DateStamp getDateStamp(String dayString) {
        try {
            // enter day as '-1' for yesterday, '0' for today, '1' for tomorrow, etc.
            int daysAgo = Integer.parseInt(dayString);
            return DateStamp.forDaysAgo(-daysAgo);
        } catch (NumberFormatException ignore) {}
        try {
            return DateStamp.fromIso8601String(dayString);
        } catch (IllegalArgumentException ignore) {}
        // invalid
        return null;
    }

    private String prettyDay(@NotNull DateStamp day) {
        StringBuilder prettyStr = new StringBuilder(day.toString());
        DateStamp today = DateStamp.forCurrentDate();
        if (day.equals(today)) {
            prettyStr.append(" (today)");
        } else if (day.equals(today.step(-1))) {
            prettyStr.append(" (yesterday)");
        }
        return prettyStr.toString();
    }
}
