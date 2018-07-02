package com.machfour.macros.cli;


import com.machfour.macros.objects.Meal;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.util.DateStamp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CliUtils {
    private CliUtils () {}

    // extracts a meal specification from the argument list using the following rules:
    // -d <day> specifies a day to search for, or on which to create the meal if create = true
    // -m <name> specifies a name for the meal, which is created if create = true and it doesn't already exist.
    // Both options can be omitted under certain condititions:
    // If -d is omitted then the current day is used.
    // If there are no meals recorded for the day, then an error is given.

    @NotNull static MealSpec makeMealSpec(List<String> args) {
        MealSpec spec = new MealSpec();
        // get day
        int dayArg = args.indexOf("-d") + 1;
        if (dayArg == 0) {
            // arg not found
            spec.day = DateStamp.forCurrentDate();
            spec.daySpecified = false;
        } else if (dayArg >= args.size()) {
            spec.error = "-d option requires an argument: <day>";
            spec.daySpecified = false;
            return spec;
        } else {
            spec.day = getDateStamp(args.get(dayArg));
            spec.daySpecified = true;
            if (spec.day == null) {
                spec.error = String.format("Invalid day format: '%s'. ", args.get(dayArg));
                spec.error += "Must be a number (e.g. 0 for today, -1 for yesterday), or a date: yyyy-mm-dd";
                return spec;
            }
        }

        // get meal name
        int mealArg = args.indexOf("-m") + 1;
        if (mealArg == 0) {
            // current meal, current day
            spec.name = "current";
            spec.mealSpecified = false;
        } else if (mealArg >= args.size()) {
            spec.error = "-m option requires an argument: <meal>";
            return spec;
        } else {
            spec.name = args.get(mealArg);
            spec.mealSpecified = true;
        }
        return spec;
    }

    static void processMealSpec(@NotNull MealSpec spec, MacrosDatabase db, boolean create) throws SQLException {
        // cases:
        // no meal specified -> use current meal (exists)
        // no meal specified -> no meal exists
        // meal specified that exists -> use it
        // meal specified that does not exist -> create it
        Map<String, Meal> mealsForDay = db.getMealsForDay(spec.day);
        if (!spec.mealSpecified) {
            if (!mealsForDay.isEmpty()) {
                // use most recently modified meal today
                spec.createdObject = Collections.max(mealsForDay.values(), Comparator.comparingLong(Meal::getModifyTime));
            } else {
                spec.error = "No meals recorded on " + prettyDay(spec.day);
                // return;
            }
        } else if (mealsForDay.containsKey(spec.name)) {
            spec.createdObject = mealsForDay.get(spec.name);
        } else if (create) {
            spec.createdObject = db.getOrCreateMeal(spec.day, spec.name);
        } else {
            // meal doesn't exist and not allowed to create new meal
            spec.error = String.format("No meal with name '%s' found on %s", spec.name, prettyDay(spec.day));
        }
        // if adding code here, uncomment the return statement above
    }

    // returns null for invalid, today if flag not found, or otherwise decodes day from argument string
    private static DateStamp getDateStamp(String dayString) {
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

    static String prettyDay(@NotNull DateStamp day) {
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
