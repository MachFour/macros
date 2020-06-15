package com.machfour.macros.cli.utils;

import com.machfour.macros.objects.Meal;
import com.machfour.macros.queries.MealQueries;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.util.DateStamp;
import com.machfour.macros.util.PrintFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.machfour.macros.cli.utils.ArgParsing.Status.OPT_ARG_MISSING;

/*
 * Stores information for a meal being input (specified) via text fields,
 * and implements default behaviour for retrieving the meal when not
 * all fields are fully specified
 */
public class MealSpec {

    private final String name;
    private final DateStamp day;
    // whether parameters were actually given (true) or the default value used (false)
    private final boolean mealSpecified;
    private final boolean daySpecified;
    private Meal processedObject;

    // has process() been called?
    private boolean processed;
    // whether the processedObject was newly created or already existed in the DB
    private boolean created;
    private String error;

    // default values
    private void defaultValues() {
        processed = false;
        created = false;
        error = null;
        processedObject = null;
    }
    private MealSpec(String name, DateStamp day) {
        defaultValues();
        this.name = name;
        this.day = day;
        this.mealSpecified = (name != null);
        this.daySpecified = (day != null);
    }

    private MealSpec(@Nullable String name, @Nullable String dayString) {
        defaultValues();
        DateStamp day = ArgParsing.dayStringParse(dayString);
        if (day == null) {
            error = String.format("Invalid day format: '%s'. ", dayString);
            error += "Must be a number (e.g. 0 for today, -1 for yesterday), or a date: yyyy-mm-dd";
        }
        this.day = day;
        this.daySpecified = (day != null) && dayString != null;

        this.name = name;
        this.mealSpecified = name != null;
    }

    private MealSpec(ArgParsing.Result mealArg, ArgParsing.Result dayArg) {
        this(mealArg.argument(), dayArg.argument());
        defaultValues();
        if (dayArg.status() == OPT_ARG_MISSING) {
            error = "-d option requires an argument: <day>";
        } else if (mealArg.status() == OPT_ARG_MISSING) {
            error = "-m option requires an argument: <meal>";
        }
    }


    public void process(MacrosDataSource ds, boolean create) {
        if (processed || error != null) {
            // skip processing if there are already errors
            return;
        }
        processed = true; // only let process() be called once
        assert day != null;

        // cases:
        // no meal specified -> use current meal (exists)
        // no meal specified -> no meal exists
        // meal specified that exists -> use it
        // meal specified that does not exist -> create it
        Map<String, Meal> mealsForDay;
        try {
            mealsForDay = MealQueries.getMealsForDay(ds, day);
        } catch (SQLException e) {
            error = String.format("Error retrieving meals for day %s: %s", day.toString(), e.getMessage());
            return;
        }
        if (!mealSpecified) {
            if (!mealsForDay.isEmpty()) {
                // use most recently modified meal today
                processedObject = Collections.max(mealsForDay.values(), Comparator.comparingLong(Meal::modifyTime));
            } else {
                error = "No meals recorded on " + PrintFormatting.prettyDay(day);
            }
        } else if (mealsForDay.containsKey(name)) {
            processedObject = mealsForDay.get(name);
            created = false;
        } else if (create) {
            try {
                processedObject = MealQueries.getOrCreateMeal(ds, day, name);
                created = true;
            } catch (SQLException e) {
                error = "Error retrieving meal: " + e.getMessage();
                return;
            }
        } else {
            // meal doesn't exist and not allowed to create new meal
            error = String.format("No meal with name '%s' found on %s", name, PrintFormatting.prettyDay(day));
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
    public static MealSpec makeMealSpec(List<String> args) {
        ArgParsing.Result dayArg = ArgParsing.findArgument(args, "-d");
        ArgParsing.Result mealArg = ArgParsing.findArgument(args, "-m");
        return new MealSpec(dayArg, mealArg);
    }
    @NotNull
    public static MealSpec makeMealSpec(@Nullable String name, @Nullable String dayString) {
        DateStamp day = ArgParsing.dayStringParse(dayString);
        return new MealSpec(name, day);
    }

    @NotNull
    public static MealSpec makeMealSpec(ArgParsing.Result nameArg, ArgParsing.Result dayArg) {
        return new MealSpec(nameArg, dayArg);
    }
    @NotNull
    public static MealSpec makeMealSpec(String name) {
        return MealSpec.makeMealSpec(name, null);
    }

    @NotNull
    public static MealSpec makeMealSpec() {
        return MealSpec.makeMealSpec((String)null);
    }

    public String name() {
        return name;
    }

    public DateStamp day() {
        return day;
    }

    public boolean mealSpecified() {
        return mealSpecified;
    }

    public boolean daySpecified() {
        return daySpecified;
    }

    public Meal processedObject() {
        return processedObject;
    }

    public boolean created() {
        return created;
    }

    public String error() {
        return error;
    }
}
