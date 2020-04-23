package com.machfour.macros.objects;

import com.machfour.macros.core.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FoodPortion extends MacrosEntityImpl<FoodPortion> {

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

    private FoodPortion(ColumnData<FoodPortion> data, ObjectSource objectSource) {
        super(data, objectSource);
        serving = null;
        food = null;
        meal = null;
        nutritionData = null;
        qtyUnit = QtyUnits.fromAbbreviationNoThrow(data.get(Schema.FoodPortionTable.QUANTITY_UNIT));
    }

    public String prettyFormat(boolean withNotes) {
        StringBuilder sb = new StringBuilder();
        sb.append(food == null ? "<no food>" : food.getMediumName());
        sb.append(", ");
        sb.append(String.format("%.1f", getData(Schema.FoodPortionTable.QUANTITY)));
        sb.append(getQtyUnit().abbr());
        if (serving != null) {
            sb.append(" (").append(servingCountString()).append(" ").append(serving.name()).append(")");
        }
        if (withNotes) {
            String notes = getNotes();
            if (notes != null && !notes.isEmpty()) {
                sb.append(" [").append(notes).append("]");
            }
        }
        return sb.toString();
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

    @NotNull
    public QtyUnit getQtyUnit() {
        return qtyUnit;
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
        nutritionData = f.getNutritionData().rescale(getQuantity(), getQtyUnit());
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
    private String servingCountString() {
        // test if can round
        if (Math.round(servingCount()) - servingCount() < 0.001) {
            return String.valueOf(Math.round(servingCount()));
        } else {
            return String.valueOf(servingCount());
        }
    }

    private double servingCount() {
        return (serving != null) ? getQuantity() / serving.quantity() : 0;
    }
}

