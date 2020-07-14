package com.machfour.macros.objects;

import com.machfour.macros.core.ColumnData;
import com.machfour.macros.core.Factory;
import com.machfour.macros.core.MacrosEntityImpl;
import com.machfour.macros.core.ObjectSource;
import com.machfour.macros.core.Schema;
import com.machfour.macros.core.Table;
import com.machfour.macros.util.DateStamp;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Meal extends MacrosEntityImpl<Meal> {

    private final List<FoodPortion> foodPortions;

    private Meal(ColumnData<Meal> data, ObjectSource objectSource) {
        super(data, objectSource);
        foodPortions = new ArrayList<>();
    }

    public static NutritionData sumNutritionData(Collection<Meal> meals) {
        List<NutritionData> totalPerMeal = new ArrayList<>(meals.size());
        for (Meal m : meals) {
            totalPerMeal.add(m.getNutritionTotal());
        }
        return NutritionData.sum(totalPerMeal);
    }

    public static Factory<Meal> factory() {
        return Meal::new;
    }
    @Override
    public Factory<Meal> getFactory() {
        return factory();
    }
    @Override
    public Table<Meal> getTable() {
        return table();
    }
    public static Table<Meal> table() {
        return Schema.MealTable.instance();
    }

    public NutritionData getNutritionTotal() {
        List<NutritionData> nutritionComponents = new ArrayList<>(foodPortions.size());
        for (FoodPortion fp : foodPortions) {
            nutritionComponents.add(fp.getNutritionData());
        }
        return NutritionData.sum(nutritionComponents);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Meal && super.equals(o);
    }

    public String getName() {
        return getData(Schema.MealTable.NAME);
    }

    /*
     * 'Day' is the day for which the nutrition data should be counted.
     * i.e. the total nutrition value for day X is the sum of nutrition data
     * for all meals having that Day field
     */
    public DateStamp getDay() {
        return getData(Schema.MealTable.DAY);
    }

    /*
     * Start time is the time that the meal was actually consumed. Note that
     * it's a full timestamp - because for various reasons (timezones, night shifts, eating past midnight)
     * we allow the day that the meal is labelled with in the Day column to be different from
     * the calendar date on which it was actually eaten.
     */
    // returns time in Unix time, aka seconds since Jan 1 1970
    public Long getStartTime() {
        return getData(Schema.MealTable.START_TIME);
    }

    // in seconds, how long the meal lasted.
    public Long getDurationSeconds() {
        return getData(Schema.MealTable.DURATION);
    }
    public Long getDurationMinutes() {
        return getDurationSeconds() / 60;
    }

    public Instant getStartTimeInstant() {
        return Instant.ofEpochSecond(getStartTime());
    }

    public List<FoodPortion> getFoodPortions() {
        return new ArrayList<>(foodPortions);
    }

    public void addFoodPortion(@NotNull FoodPortion fp) {
        // can't assert !foodPortions.contains(fp) since user-created food portions can look identical
        assert foreignKeyMatches(fp, Schema.FoodPortionTable.MEAL_ID, this);
        foodPortions.add(fp);
    }
}
