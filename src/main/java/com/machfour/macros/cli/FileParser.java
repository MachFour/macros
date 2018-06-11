package com.machfour.macros.cli;

import com.machfour.macros.objects.*;
import com.machfour.macros.core.ObjectSource;
import com.machfour.macros.core.ColumnData;
import com.machfour.macros.core.Schema;
import com.machfour.macros.linux.MacrosLinuxDatabase;
import com.machfour.macros.util.DateStamp;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileParser {
    private static final Pattern mealPattern = Pattern.compile("\\[(?<mealdesc>.*)]");
    private static final String quantityRegex = "(?<qty>-?[0-9]+(?:.[0-9]+)?)";
    private static final String unitRegex = "(?<unit>[a-zA-Z]+)?";
    private static final Pattern servingCountPattern = Pattern.compile(quantityRegex);
    private static final Pattern quantityAndUnitPattern = Pattern.compile(quantityRegex + "\\s*" + unitRegex);

    private final MacrosLinuxDatabase db;
    private final Map<String, String> errorLines;
    private Meal currentMeal;
    // if we encounter a food line before a meal title,
    // have to instantiate a dummy meal to hold it
    private boolean haveCurrentMeal;

    FileParser(MacrosLinuxDatabase db) {
        this.db = db;
        // use LinkedHashMap to maintain insertion order
        errorLines = new LinkedHashMap<>();
        currentMeal = null;
        haveCurrentMeal = false;
    }

    Map<String, String> getErrorLines() {
        return Collections.unmodifiableMap(errorLines);
    }

    List<Meal> parseFile(String fileName) throws IOException, SQLException {
        List<Meal> meals = new ArrayList<>();
        Path filePath = Paths.get(fileName);
        List<String> fileLines = Files.readAllLines(filePath);

        DateStamp currentDay = DateStamp.forCurrentDate();

        for (String line : fileLines) {
            line = line.trim();
            Matcher mealTitle = mealPattern.matcher(line);
            if (mealTitle.find()) {
                // make a new meal
                String mealDescription = mealTitle.group("mealdesc");
                currentMeal = makeMeal(mealDescription, currentDay);
                meals.add(currentMeal);
                haveCurrentMeal = true;
            } else if (line.isEmpty() || line.startsWith("#")) {
                // ignore 'comment line'
            } else {
                // treat as FoodPortion spec
                // make a new meal if necessary
                if (!haveCurrentMeal) {
                    currentMeal = makeMeal("Unnamed meal", currentDay);
                    meals.add(currentMeal);
                    haveCurrentMeal = true;
                }
                makefoodPortionFromLine(line);
            }
        }
        return meals;
    }

    // returns a FoodPortionEntry object, from the given line of text
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
    private void makefoodPortionFromLine(String line) throws SQLException {
        String[] tokens = line.split(",");
        for (int i = 0; i < tokens.length; ++i) {
            tokens[i] = tokens[i].trim();
        }
        Food food = db.getFoodByIndexName(tokens[0]);
        if (food == null) {
            errorLines.put(line, "unrecognised food");
            return;
        }
        // TODO check for servings, default servings, quantity, etc.
        ColumnData<FoodPortion> fpData = new ColumnData<>(FoodPortion.table());
        Serving serving = null;
        double quantity = 0.0;
        QuantityUnit unit = null;
        boolean parseError = false;
        switch (tokens.length) {
            case 1:
                // 1 of default serving
                Serving defaultServing = food.getDefaultServing();
                if (defaultServing == null) {
                    errorLines.put(line, "food has no default serving");
                    parseError = true;
                } else {
                    serving = defaultServing;
                    quantity = serving.getQuantity();
                    unit = serving.getQuantityUnit();
                }
                break;
            case 2:
                // vanilla food and quantity, with optional unit, defaulting to grams
                serving = null;
                Matcher quantityMatch = quantityAndUnitPattern.matcher(tokens[1]);
                if (!quantityMatch.find()) {
                    // could not understand anything
                    errorLines.put(line, "invalid quantity or unit");
                    parseError = true;
                    break;
                }
                try {
                    quantity = Double.parseDouble(quantityMatch.group("qty"));
                } catch (NumberFormatException e) {
                    // invalid quantity
                    errorLines.put(line, "invalid quantity");
                    parseError = true;
                    break;
                }
                String unitString = quantityMatch.group("unit");
                if (unitString == null) {
                    unit = QuantityUnit.GRAMS;
                } else {
                    unit = QuantityUnit.fromAbbreviation(unitString);
                    if (unit == null) {
                        // invalid unit
                        errorLines.put(line, "unrecognised unit");
                        parseError = true;
                        break;
                    }
                }
                break;
            case 3:
                String servingName = tokens[1];
                if (servingName.isEmpty()) {
                    serving = food.getDefaultServing();
                } else {
                    serving = food.getServingByName(servingName);
                }
                if (serving == null) {
                    errorLines.put(line, "missing serving (no default) or unrecognised serving name");
                    parseError = true;
                    break;
                }
                unit = serving.getQuantityUnit();
                // get quantity, which defaults to 1 of serving if not included
                String servingCountStr = tokens[2];
                if (servingCountStr.isEmpty()) {
                    quantity = serving.getQuantity();
                } else {
                    Matcher servingCountMatch = servingCountPattern.matcher(tokens[2]);
                    if (servingCountMatch.find()) {
                        try {
                            double servingCount = Double.parseDouble(servingCountMatch.group("qty"));
                            quantity = servingCount*serving.getQuantity();
                        } catch (NumberFormatException e) {
                            errorLines.put(line, "invalid serving count");
                            parseError = true;
                            break;
                        }
                    } else {
                        errorLines.put(line, "invalid serving count");
                        parseError = true;
                        break;
                    }
                }
                break;
            default:
                errorLines.put(line, "too many commas");
                parseError = true;
        }
        if (!parseError) {
            fpData.put(Schema.FoodPortionTable.FOOD_ID, food.getId());
            fpData.put(Schema.FoodPortionTable.SERVING_ID, serving == null ? null : serving.getId());
            fpData.put(Schema.FoodPortionTable.MEAL_ID, currentMeal.getId());
            fpData.put(Schema.FoodPortionTable.QUANTITY_UNIT, unit.getAbbreviation());
            fpData.put(Schema.FoodPortionTable.QUANTITY, quantity);
            FoodPortion fp = new FoodPortion(fpData, ObjectSource.USER_NEW);
            fp.setFood(food);
            if (serving != null) {
                fp.setServing(serving);
            }
            currentMeal.addFoodPortion(fp);
        }
    }

    private static Meal makeMeal(@NotNull String description, @NotNull DateStamp day) {
        ColumnData<Meal> mealData = new ColumnData<>(Meal.table());
        mealData.put(Schema.MealTable.NAME, description);
        mealData.put(Schema.MealTable.DAY, day);
        return Meal.factory().construct(mealData, ObjectSource.USER_NEW);

    }
}
