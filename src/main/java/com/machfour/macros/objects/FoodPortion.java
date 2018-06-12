package com.machfour.macros.objects;

import com.machfour.macros.core.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FoodPortion extends MacrosEntity<FoodPortion> {

    /* These are not set on construction, but are only settable once: "pseudo-immutable".
     * This makes it easier to create the objects from the DB.
     */
    private Food food;
    private NutritionData nutritionData;
    private Meal meal;
    private QtyUnit qtyUnit;
    // this is the only thing that may remain null after all initialisation is complete
    @Nullable
    private Serving serving;

    public FoodPortion(ColumnData<FoodPortion> data, ObjectSource objectSource) {
        super(data, objectSource);
        serving = null;
        qtyUnit = null;
        food = null;
        meal = null;
        nutritionData = null;
    }

    @Override
    public Table<FoodPortion> getTable() {
        return table();
    }
    public static Table<FoodPortion> table() {
        return Schema.FoodPortionTable.instance();
    }
    public static Factory<FoodPortion> factory() {
        return FoodPortion::new;
    }
    @Override
    public Factory<FoodPortion> getFactory() {
        return factory();
    }

    // we already use polymorphism to check the data is equal for subclasses of MacrosEntity;
    // the only thing that it misses out is checking that o is actually an instance of the subclass.
    @Override
    public boolean equals(Object o) {
        return o instanceof FoodPortion && super.equals(o);
    }

    public QtyUnit getQtyUnit() {
        return qtyUnit;
    }

    public void setQtyUnit(@NotNull QtyUnit q) {
        assert qtyUnit == null && foreignKeyMatches(this, Schema.FoodPortionTable.QUANTITY_UNIT, q);
        qtyUnit = q;

    }

    @NotNull
    public String getQuantityUnitAbbr() {
        return getData(Schema.FoodPortionTable.QUANTITY_UNIT);
    }

    public Meal getMeal() {
        return meal;
    }

    public void setMeal(@NotNull Meal m) {
        assert meal == null && foreignKeyMatches(this, Schema.FoodPortionTable.MEAL_ID, m);
        meal = m;
    }

    @NotNull
    public Long getMealId() {
        return getData(Schema.FoodPortionTable.MEAL_ID);
    }

    public Food getFood() {
        return food;
    }

    public void setFood(@NotNull Food f) {
        assert food == null && foreignKeyMatches(this, Schema.FoodPortionTable.FOOD_ID, f);
        food = f;
        nutritionData = f.getNutritionData(getQuantity());
    }

    @NotNull
    public Long getFoodId() {
        return getData(Schema.FoodPortionTable.FOOD_ID);
    }

    @Nullable
    public Serving getServing() {
        return serving;
    }

    // for use during construction
    public void setServing(@NotNull Serving s) {
        assert serving == null && foreignKeyMatches(this, Schema.FoodPortionTable.SERVING_ID, s);
        assert (getFoodId().equals(s.getFoodId()));
        serving = s;
    }

    @Nullable
    public Long getServingId() {
        return getData(Schema.FoodPortionTable.SERVING_ID);
    }

    @NotNull
    public Double getQuantity() {
        return getData(Schema.FoodPortionTable.QUANTITY);
    }

    @NotNull
    public NutritionData getNutritionData() {
        return nutritionData;
    }

    @Nullable
    public String getNotes() {
        return getData(Schema.FoodPortionTable.NOTES);
    }

    // returns a string containing the serving count. If the serving count is close to an integer,
    // it is formatted as an integer.
    public String servingCountString() {
        // test if can round
        if (Math.round(servingCount()) - servingCount() < 0.001) {
            return String.valueOf(Math.round(servingCount()));
        } else {
            return String.valueOf(servingCount());
        }
    }

    public double servingCount() {
        return (serving != null) ? getQuantity() / serving.getQuantity() : 0;
    }
}

