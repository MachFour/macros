package com.machfour.macros.queries;

import com.machfour.macros.core.ColumnData;
import com.machfour.macros.core.ObjectSource;
import com.machfour.macros.core.Schema;
import com.machfour.macros.objects.Food;
import com.machfour.macros.objects.FoodPortion;
import com.machfour.macros.objects.Meal;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.util.DateStamp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.machfour.macros.core.MacrosUtils.getOrDefault;

public class MealQueries {
    public static void saveFoodPortions(MacrosDataSource ds, @NotNull Meal m) throws SQLException {
        for (FoodPortion fp : m.getFoodPortions()) {
            if (!fp.getObjectSource().equals(ObjectSource.DATABASE)) {
                Queries.saveObject(ds, fp);
            }
        }
    }

    @NotNull
    public static Meal getOrCreateMeal(MacrosDataSource ds, @NotNull DateStamp day, @NotNull String name) throws SQLException {
        Map<String, Meal> mealsForDay = getMealsForDay(ds, day);
        if (mealsForDay.containsKey(name)) {
            return mealsForDay.get(name);
        } else {
            ColumnData<Meal> newMealData = new ColumnData<>(Meal.table());
            newMealData.put(Schema.MealTable.DAY, day);
            newMealData.put(Schema.MealTable.NAME, name);
            Meal newMeal = Meal.factory().construct(newMealData, ObjectSource.USER_NEW);
            Queries.saveObject(ds, newMeal);
            // get it back again, so that it has an ID and stuff
            mealsForDay = getMealsForDay(ds, day);
            assert (mealsForDay.containsKey(name)) : "didn't find saved meal in meals for its day";
            return mealsForDay.get(name);
        }

    }

    // finds whether there is a 'current meal', and returns it if so.
    // defined as the most recently modified meal created for the current date
    // if no meals exist for the current date, returns null
    @Nullable
    public static Meal getCurrentMeal(MacrosDataSource ds) throws SQLException {
        Map<String, Meal> mealsForDay = getMealsForDay(ds, DateStamp.forCurrentDate());
        if (mealsForDay.isEmpty()) {
            return null;
        } else {
            // most recently modified -> largest modification time -> swap compare order
            return Collections.max(mealsForDay.values(),
                    (Meal a, Meal b) -> Long.compare(b.modifyTime(), a.modifyTime()));

        }
    }

    // key is the meal name, which is unique given a particular day
    @NotNull
    public static Map<String, Meal> getMealsForDay(MacrosDataSource ds, DateStamp day) throws SQLException {
        List<Long> mealIds = getMealIdsForDay(ds, day);
        Map<Long, Meal> mealsById = getMealsById(ds, mealIds);
        // sort by create time and put in tree map to preserve order
        List<Meal> mealsByCreateTime = new ArrayList<>(mealsById.values());
        // TODO API level: mealsByCreateTime.sort(Comparator.comparingLong(Meal::createTime));
        // TODO API level: Comparator.comparingLong(Meal::createTime);
        Collections.sort(mealsByCreateTime, (m1, m2) -> Long.compare(m1.getStartTime(), m2.getStartTime()));

        Map<String, Meal> mealsByName = new LinkedHashMap<>();
        for (Meal m : mealsByCreateTime) {
            assert !mealsByName.containsKey(m.getName());
            mealsByName.put(m.getName(), m);
        }
        return mealsByName;
    }

    @NotNull
    public static List<Long> getMealIdsForDay(MacrosDataSource ds, @NotNull DateStamp day) throws SQLException {
        return Queries.selectColumn(ds, Schema.MealTable.instance(), Schema.MealTable.ID, Schema.MealTable.DAY, Collections.singletonList(day));
        // TODO: need "DATE(" + Meal.Column.DAY + ") = DATE ( ? )"; ???
    }

    /* The get<Object>By(Id|Key) functions construct objects for all necessary entities that match the query,
     * as well as all other entities referenced by them.
     * For example, getMealsForDay constructs all of the MealTable objects for one particular day,
     * along with their FoodPortions, their Foods, and all of the Servings of those Foods.
     * It's probably worth caching the results of these!
     */
    @Nullable
    public static Meal getMealById(MacrosDataSource ds, @NotNull Long id) throws SQLException {
        Map<Long, Meal> resultMeals = getMealsById(ds, Collections.singletonList(id));
        return getOrDefault(resultMeals, id, null);
    }

    @NotNull
    public static Map<Long, Meal> getMealsById(MacrosDataSource ds, @NotNull List<Long> mealIds) throws SQLException {
        if (mealIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> foodIds = getFoodIdsForMeals(ds, mealIds);
        Map<Long, Meal> meals = QueryHelpers.getRawMealsById(ds, mealIds);
        // this check stops an unnecessary lookup of all foods, which happens if no IDs are passed
        // into getFoodsById;
        if (!foodIds.isEmpty()) {
            Map<Long, Food> foodMap = FoodQueries.getFoodsById(ds, foodIds);
            for (Meal meal : meals.values()) {
                QueryHelpers.applyFoodPortionsToRawMeal(ds, meal, foodMap);
            }
        }
        return meals;
    }

    @NotNull
    public static List<Long> getFoodIdsForMeals(MacrosDataSource ds, List<Long> mealIds) throws SQLException {
        return ds.selectColumn(FoodPortion.table(), Schema.FoodPortionTable.FOOD_ID, Schema.FoodPortionTable.MEAL_ID, mealIds, true);
    }
}