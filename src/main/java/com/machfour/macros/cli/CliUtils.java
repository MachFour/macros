package com.machfour.macros.cli;


import com.machfour.macros.core.Column;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.objects.NutritionData;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.util.DateStamp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.*;

import static com.machfour.macros.core.Schema.NutritionDataTable.*;

public class CliUtils {
    static final List<Column<NutritionData, Double>> allNutrientsToPrint;
    static final Map<Column<NutritionData, Double>, String> briefNames;
    static final Map<Column<NutritionData, Double>, String> prettyNames;


    static {
        briefNames = new HashMap<>();
        briefNames.put(CALORIES, "Cals");
        briefNames.put(PROTEIN, "Prot");
        briefNames.put(FAT, "Fat");
        briefNames.put(CARBOHYDRATE, "Carb");
        briefNames.put(QUANTITY, "Qty");
        prettyNames = new HashMap<>();
        prettyNames.put(KILOJOULES, "Kilojoules");
        prettyNames.put(CALORIES, "Calories");
        prettyNames.put(PROTEIN, "Protein");
        prettyNames.put(FAT, "Fat");
        prettyNames.put(SATURATED_FAT, "Saturated");
        prettyNames.put(CARBOHYDRATE, "Carbohydrate");
        prettyNames.put(SUGAR, "Sugar");
        prettyNames.put(FIBRE, "Fibre");
        prettyNames.put(SODIUM, "Sodium");
        prettyNames.put(QUANTITY, "Quantity");
        allNutrientsToPrint = Arrays.asList(
                KILOJOULES
                , CALORIES
                , PROTEIN
                , FAT
                , SATURATED_FAT
                , CARBOHYDRATE
                , SUGAR
                , FIBRE
                , SODIUM
        );
    }

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

    static void processMealSpec(@NotNull MealSpec spec, MacrosDatabase db, boolean create) {
        if (spec.error != null) {
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
            mealsForDay = db.getMealsForDay(spec.day);
        } catch (SQLException e) {
            spec.error = String.format("Error retrieving meals for day %s: %s", spec.day.toString(), e.getMessage());
            return;
        }
        if (!spec.mealSpecified) {
            if (!mealsForDay.isEmpty()) {
                // use most recently modified meal today
                spec.createdObject = Collections.max(mealsForDay.values(), Comparator.comparingLong(Meal::getModifyTime));
            } else {
                spec.error = "No meals recorded on " + prettyDay(spec.day);
            }
        } else if (mealsForDay.containsKey(spec.name)) {
            spec.createdObject = mealsForDay.get(spec.name);
        } else if (create) {
            try {
                spec.createdObject = db.getOrCreateMeal(spec.day, spec.name);
            } catch (SQLException e) {
                spec.error = "Error retrieving meal: " + e.getMessage();
                return;
            }
        } else {
            // meal doesn't exist and not allowed to create new meal
            spec.error = String.format("No meal with name '%s' found on %s", spec.name, prettyDay(spec.day));
        }
        assert (spec.error != null || spec.createdObject != null) : "No error message but no created object";
        if (spec.error != null) {
            return;
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

    static void printPer100g(NutritionData nd, boolean verbose, PrintStream out) {
        printNutritionData(nd.rescale(100), verbose, out);
    }

    static void printNutritionData(NutritionData nd, boolean verbose, PrintStream out) {
        String lineFormat = "%15s: %4.0f %s";
        for (Column<NutritionData, Double> col: allNutrientsToPrint) {
            Double value = nd.amountOf(col, 0.0);
            String unit = NutritionData.getUnitForNutrient(col);
            if (!nd.hasCompleteData(col)) {
                // mark incomplete
                unit += " **";
            }
            out.println(String.format(lineFormat, prettyNames.get(col), value, unit));
        }
    }

    static void printEnergyProportions(NutritionData nd, boolean verbose, PrintStream out) {
        out.println("Energy proportions (approx.)");
        Map<Column<NutritionData, Double>, Double> proportionMap = nd.makeEnergyProportionsMap();
        for (Column<NutritionData, Double> col: proportionMap.keySet()) {
            out.printf("%15s: %5.1f%%\n", prettyNames.get(col), proportionMap.get(col));
        }
    }
}
