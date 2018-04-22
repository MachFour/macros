package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Columns;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Tables;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

public class FoodPortion extends MacrosEntity<FoodPortion> {

    /* These are not set on construction, but are only settable once: "pseudo-immutable".
     * This makes it easier to create the objects from the DB.
     */
    private Food food;
    private NutritionData nutritionData;
    private Meal meal;
    private QuantityUnit quantityUnit;
    // this is the only thing that may remain null after all initialisation is complete
    @Nullable
    private Serving serving;

    public FoodPortion(ColumnData<FoodPortion> data, boolean isFromDb) {
        super(data, isFromDb);
        serving = null;
        quantityUnit = null;
        food = null;
        meal = null;
        nutritionData = null;
    }

    @Override
    public Table<FoodPortion> getTable() {
        return Tables.FoodPortionTable.instance();
    }

    // we already use polymorphism to check the data is equal for subclasses of MacrosEntity;
    // the only thing that it misses out is checking that o is actually an instance of the subclass.
    @Override
    public boolean equals(Object o) {
        return o instanceof FoodPortion && super.equals(o);
    }

    public QuantityUnit getQuantityUnit() {
        return quantityUnit;
    }

    public void setQuantityUnit(@NotNull QuantityUnit q) {
        assert (quantityUnit == null);
        assert (getQuantityUnitId().equals(q.getId()));
        quantityUnit = q;

    }

    @NotNull
    public Long getQuantityUnitId() {
        return getTypedDataForColumn(Columns.FoodPortionCol.QUANTITY_UNIT);
    }

    public Meal getMeal() {
        return meal;
    }

    public void setMeal(@NotNull Meal m) {
        assert (meal == null);
        assert (getMealId().equals(m.getId()));
        meal = m;
    }

    @NotNull
    public Long getMealId() {
        return getTypedDataForColumn(Columns.FoodPortionCol.MEAL_ID);
    }

    public Food getFood() {
        return food;
    }

    public void setFood(@NotNull Food f) {
        assert (food == null);
        assert (getFoodId().equals(f.getId()));
        food = f;
        nutritionData = f.getNutritionData(getQuantity());
    }

    @NotNull
    public Long getFoodId() {
        return getTypedDataForColumn(Columns.FoodPortionCol.FOOD_ID);
    }

    @Nullable
    public Serving getServing() {
        return serving;
    }

    // for use during construction
    public void setServing(@NotNull Serving s) {
        assert (serving == null);
        assert (getServingId().equals(s.getId()));
        assert (getFoodId().equals(s.getFoodId()));
        serving = s;
    }

    @Nullable
    public Long getServingId() {
        return getTypedDataForColumn(Columns.FoodPortionCol.SERVING_ID);
    }

    @NotNull
    public Double getQuantity() {
        return getTypedDataForColumn(Columns.FoodPortionCol.QUANTITY);
    }

    public NutritionData getNutritionData() {
        return nutritionData;
    }

    @Nullable
    public String getNotes() {
        return getTypedDataForColumn(Columns.FoodPortionCol.NOTES);
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

