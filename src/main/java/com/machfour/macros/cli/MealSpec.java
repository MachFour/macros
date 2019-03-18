package com.machfour.macros.cli;

import com.machfour.macros.objects.Meal;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.util.DateStamp;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.machfour.macros.cli.ArgParsing.Status.OPT_ARG_MISSING;
import static com.machfour.macros.cli.ArgParsing.Status.ARG_FOUND;

/*
 * Stores information for a meal being input (specified) via text fields,
 * and implements default behaviour for retrieving the meal when not
 * all fields are fully specified
 */
class MealSpec {

    private final String name;
    private final DateStamp day;
    // whether parameters were actually given (true) or the default value used (false)
    private final boolean mealSpecified;
    private final boolean daySpecified;
    private Meal processedObject;
    // whether the processedObject was newly created or already existed in the DB
    private boolean created;
    private String error;

    private MealSpec(String name, DateStamp day) {
        this.name = name;
        this.day = day;
        this.mealSpecified = (name != null);
        this.daySpecified = (day != null);
    }

    MealSpec(ArgParsing.Result dayArg, ArgParsing.Result mealArg) {
        DateStamp day = ArgParsing.dayStringParse(dayArg.argument());
        if (day == null) {
            error = String.format("Invalid day format: '%s'. ", dayArg.argument());
            error += "Must be a number (e.g. 0 for today, -1 for yesterday), or a date: yyyy-mm-dd";
        } else if (dayArg.status() == OPT_ARG_MISSING) {
            error = "-d option requires an argument: <day>";
        }
        this.day = day;
        this.daySpecified = (day != null) && dayArg.status() == ARG_FOUND;

        String mealName = mealArg.argument();
        this.name = mealName;
        this.mealSpecified = mealName != null && mealArg.status() == ARG_FOUND;
        if (mealArg.status() == OPT_ARG_MISSING) {
            error = "-m option requires an argument: <meal>";
        }
    }


    void processMealSpec(MacrosDatabase db, boolean create) {
        if (error != null) {
            // skip processing if there are already errors
            return;
        }
        // cases:
        // no meal specified -> use current meal (exists)
        // no meal specified -> no meal exists
        // meal specified that exists -> use it
        // meal specified that does not exist -> create it
        Map<String, Meal> mealsForDay;
        try {
            mealsForDay = db.getMealsForDay(day);
        } catch (SQLException e) {
            error = String.format("Error retrieving meals for day %s: %s", day.toString(), e.getMessage());
            return;
        }
        if (!mealSpecified) {
            if (!mealsForDay.isEmpty()) {
                // use most recently modified meal today
                processedObject = Collections.max(mealsForDay.values(), Comparator.comparingLong(Meal::modifyTime));
            } else {
                error = "No meals recorded on " + CliUtils.prettyDay(day);
            }
        } else if (mealsForDay.containsKey(name)) {
            processedObject = mealsForDay.get(name);
            created = false;
        } else if (create) {
            try {
                processedObject = db.getOrCreateMeal(day, name);
                created = true;
            } catch (SQLException e) {
                error = "Error retrieving meal: " + e.getMessage();
                return;
            }
        } else {
            // meal doesn't exist and not allowed to create new meal
            error = String.format("No meal with name '%s' found on %s", name, CliUtils.prettyDay(day));
        }
        assert (error != null || processedObject != null) : "No error message but no created object";
        if (error != null) {
            return;
        }
    }


    // extracts a meal specification from the argument list using the following rules:
    // -d <day> specifies a day to search for, or on which to create the meal if create = true
    // -m <name> specifies a name for the meal, which is created if create = true and it doesn't already exist.
    // Both options can be omitted under certain condititions:
    // If -d is omitted then the current day is used.
    // If there are no meals recorded for the day, then an error is given.
    @NotNull
    static MealSpec makeMealSpec(List<String> args) {
        ArgParsing.Result dayArg = ArgParsing.findArgument(args, "-d");
        ArgParsing.Result mealArg = ArgParsing.findArgument(args, "-m");
        return new MealSpec(dayArg, mealArg);
    }
    @NotNull
    static MealSpec makeMealSpec(String name, DateStamp day) {
        return new MealSpec(name, day);
    }

    @NotNull
    static MealSpec makeMealSpec(ArgParsing.Result nameArg, ArgParsing.Result dayArg) {
        return new MealSpec(nameArg, dayArg);
    }
    @NotNull
    static MealSpec makeMealSpec(String name) {
        return new MealSpec(name, DateStamp.forCurrentDate());
    }

    String name() {
        return name;
    }

    DateStamp day() {
        return day;
    }

    boolean mealSpecified() {
        return mealSpecified;
    }

    boolean daySpecified() {
        return daySpecified;
    }

    Meal processedObject() {
        return processedObject;
    }

    boolean created() {
        return created;
    }

    String error() {
        return error;
    }
}
