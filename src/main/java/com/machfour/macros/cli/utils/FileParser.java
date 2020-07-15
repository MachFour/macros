package com.machfour.macros.cli.utils;

import com.machfour.macros.queries.FoodQueries;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.objects.*;
import com.machfour.macros.core.ObjectSource;
import com.machfour.macros.core.ColumnData;
import com.machfour.macros.core.Schema;
import com.machfour.macros.util.DateStamp;
import com.machfour.macros.util.FoodPortionSpec;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FileParser {
    private static final Pattern mealPattern = Pattern.compile("\\[(?<mealdesc>.*)]");
    private static final String quantityRegex = "(?<qty>-?[0-9]+(?:.[0-9]+)?)";
    private static final String unitRegex = "(?<unit>[a-zA-Z]+)?";
    private static final Pattern servingCountPattern = Pattern.compile(quantityRegex);
    private static final Pattern quantityAndUnitPattern = Pattern.compile(quantityRegex + "\\s*" + unitRegex);

    private final Map<String, String> errorLines;

    public FileParser() {
        // use LinkedHashMap to maintain insertion order
        errorLines = new LinkedHashMap<>();
    }

    public Map<String, String> getErrorLines() {
        return Collections.unmodifiableMap(errorLines);
    }

    // returns an array holding MealSpec or FoodPortionSpec objects describing the objects that should be created
    // only checked for syntax, not whether those foods/servings actually exist
    // the first item is always a MealSpec
    private static Map<MealSpec, List<FoodPortionSpec>> createSpecFromLines(List<String> fileLines) {
        Map<MealSpec, List<FoodPortionSpec>> specMap = new LinkedHashMap<>();
        // if we encounter a food line before a meal title,
        // have to instantiate a dummy meal to hold it
        List<FoodPortionSpec> currentFpSpecs = null;
        for (int index = 0; index < fileLines.size(); ++index) {
            String line = fileLines.get(index).trim();
            Matcher mealTitle = mealPattern.matcher(line);
            if (mealTitle.find()) {
                // make a new meal
                MealSpec m = MealSpec.makeMealSpec(mealTitle.group("mealdesc"));
                currentFpSpecs = new ArrayList<>();
                specMap.put(m, currentFpSpecs);
            } else if (!line.isEmpty() && !line.startsWith("#")) {
                // ignore 'comment lines' and treat anything else as a FoodPortionSpec.
                // make a new meal if necessary
                if (currentFpSpecs == null) {
                    MealSpec m = MealSpec.makeMealSpec("Unnamed meal");
                    currentFpSpecs = new ArrayList<>();
                    specMap.put(m, currentFpSpecs);
                }
                FoodPortionSpec fpSpec = makefoodPortionSpecFromLine(line);
                fpSpec.lineIdx = index;
                currentFpSpecs.add(fpSpec);
            }
        }
        return specMap;
    }
    private static Set<String> getAllIndexNames(Collection<List<FoodPortionSpec>> allFpSpecs) {
        // assume around 8 food portions per list
        Set<String> foodIndexNames = new HashSet<>(8*allFpSpecs.size());
        for (List<FoodPortionSpec> portionSpecs : allFpSpecs) {
            for (FoodPortionSpec fps : portionSpecs) {
                foodIndexNames.add(fps.foodIndexName);
            }
        }
        return foodIndexNames;
    }

    // like Files.readAllLines() but for a reader input.
    // Make sure to close the reader afterwards
    private static List<String> readAllLines(Reader in) throws IOException {
        List<String> allLines = new ArrayList<>();
        BufferedReader r = new BufferedReader(in);
        for (String s = r.readLine(); s != null; s = r.readLine()) {
            allLines.add(s);
        }
        return allLines;

    }
    // make sure to close the reader afterwards
    public List<Meal> parseFile(Reader fileReader, MacrosDataSource db) throws IOException, SQLException {
        List<String> fileLines = readAllLines(fileReader);
        // also gets list of index names to retrieve
        Map<MealSpec, List<FoodPortionSpec>> mealSpecs = createSpecFromLines(fileLines);

        // get all the index names in one place so that we can grab them all at once from the DB
        Set<String> foodIndexNames = getAllIndexNames(mealSpecs.values());

        List<Meal> meals = new ArrayList<>();

        Map<String, Food> foods = FoodQueries.getFoodsByIndexName(db, foodIndexNames);
        DateStamp currentDay = DateStamp.currentDate();
        for (Map.Entry<MealSpec, List<FoodPortionSpec>> spec : mealSpecs.entrySet()) {
            Meal m = makeMeal(spec.getKey().name(), currentDay);
            for (FoodPortionSpec fps : spec.getValue()) {
                if (fps.error != null) {
                    // it was an error, log and then ignore
                    errorLines.put(fileLines.get(fps.lineIdx), fps.error);
                } else if (!foods.containsKey(fps.foodIndexName)) {
                    // no food found
                    String errormsg = String.format("unrecognised food index name: '%s'", fps.foodIndexName);
                    errorLines.put(fileLines.get(fps.lineIdx), errormsg);
                } else {
                    // everything seems okay
                    processFpSpec(fps, m, foods.get(fps.foodIndexName));
                    // was there a DB error?
                    if (fps.createdObject == null) {
                        assert fps.error != null : "No FoodPortion created but no error message";
                        errorLines.put(fileLines.get(fps.lineIdx), fps.error);
                    } else {
                        // finally!
                        m.addFoodPortion(fps.createdObject);
                    }
                }
            }
            meals.add(m);
        }
        return meals;
    }

    public static void processFpSpec(@NotNull FoodPortionSpec fps, @NotNull Meal m, @NotNull Food f) {
        assert (f.getIndexName().equals(fps.foodIndexName)) : "Food does not match index name of spec";
        Serving s = null;
        double quantity;
        QtyUnit unit;
        if (fps.isServingMode) {
            assert fps.servingName != null && fps.servingCount != 0;
            if (fps.servingName.equals("")) {
                // default serving
                s = f.getDefaultServing();
                if (s == null) {
                    fps.error = "food has no default serving";
                    return;
                }
            } else {
                s = f.getServingByName(fps.servingName);
                if (s == null) {
                    fps.error = "food has no serving named '" + fps.servingName + "'";
                    return;
                }
            }
            quantity = fps.servingCount * s.getQuantity();
            unit = s.getQtyUnit();
        } else {
            // not serving mode
            assert (fps.unit != null);
            quantity = fps.quantity;
            unit = fps.unit;
        }
        ColumnData<FoodPortion> fpData = new ColumnData<>(FoodPortion.table());
        fpData.put(Schema.FoodPortionTable.FOOD_ID, f.getId());
        fpData.put(Schema.FoodPortionTable.SERVING_ID, s == null ? null : s.getId());
        fpData.put(Schema.FoodPortionTable.MEAL_ID, m.getId());
        fpData.put(Schema.FoodPortionTable.QUANTITY_UNIT, unit.getAbbr());
        fpData.put(Schema.FoodPortionTable.QUANTITY, quantity);
        FoodPortion fp = FoodPortion.factory().construct(fpData, ObjectSource.USER_NEW);
        fp.setFood(f);
        if (s != null) {
            fp.setServing(s);
        }
        fps.createdObject = fp;
    }

    // returns a FoodPortionSpec object, from the given line of text
    // input forms (whitespace ignored):
    // 1.
    //   egg         ,          60
    //    ^          ^          ^
    //index name  separator  quantity (default metric units)
    //
    // 2.
    //   egg         ,        large     ,       1
    //    ^          ^          ^               ^
    //index name  separator  serving_name   num_servings
    // (serving name must be a valid serving for the food)
    //
    // 3.
    //   egg         ,        ,       1
    //    ^          ^                ^
    //index name  separator  number of servings
    // (default serving assumed, error if no default serving registered)

    // returns null if there was an error during parsing (not a DB error)
    public static FoodPortionSpec makefoodPortionSpecFromLine(String line) {
        // if you don't specify an array length limit, it won't match empty strings between commas
        String[] tokens = line.split(",", 4);
        for (int i = 0; i < tokens.length; ++i) {
            tokens[i] = tokens[i].trim();
        }
        FoodPortionSpec spec = new FoodPortionSpec();
        spec.foodIndexName = tokens[0];
        switch (tokens.length) {
            case 1:
                // 1 of default serving
                spec.isServingMode = true;
                spec.servingCount = 1;
                spec.servingName = "";
                break;
            case 2:
                // vanilla food and quantity, with optional unit, defaulting to grams
                spec.isServingMode = false;
                Matcher quantityMatch = quantityAndUnitPattern.matcher(tokens[1]);
                if (!quantityMatch.find()) {
                    // could not understand anything
                    spec.error = "invalid quantity or unit";
                    break;
                }
                try {
                    spec.quantity = Double.parseDouble(quantityMatch.group("qty"));
                } catch (NumberFormatException e) {
                    // invalid quantity
                    spec.error = "invalid quantity";
                    break;
                }
                String unitString = quantityMatch.group("unit");
                if (unitString == null) {
                    spec.unit = QtyUnits.GRAMS;
                } else {
                    spec.unit = QtyUnits.fromAbbreviationNoThrow(unitString);
                    if (spec.unit == null) {
                        // invalid unit
                        spec.error = "unrecognised unit";
                        break;
                    }
                }
                break;
            case 3:
                spec.isServingMode = true;
                spec.servingName = tokens[1];
                // get quantity, which defaults to 1 of serving if not included
                String servingCountStr = tokens[2];
                if (servingCountStr.isEmpty()) {
                    spec.servingCount = 1;
                } else {
                    Matcher servingCountMatch = servingCountPattern.matcher(tokens[2]);
                    if (servingCountMatch.find()) {
                        try {
                            spec.servingCount = Double.parseDouble(servingCountMatch.group("qty"));
                        } catch (NumberFormatException e) {
                            spec.error = "invalid serving count";
                            break;
                        }
                    } else {
                        spec.error = "invalid serving count";
                        break;
                    }
                }
                break;
            default:
                spec.error = "too many commas";
                break;
        }
        return spec;
    }

    private static Meal makeMeal(@NotNull String description, @NotNull DateStamp day) {
        ColumnData<Meal> mealData = new ColumnData<>(Meal.table());
        mealData.put(Schema.MealTable.NAME, description);
        mealData.put(Schema.MealTable.DAY, day);
        return Meal.factory().construct(mealData, ObjectSource.USER_NEW);

    }
}
