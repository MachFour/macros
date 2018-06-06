package com.machfour.macros.core;

import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Schema;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

public class Ingredient extends MacrosEntity<Ingredient> {

    /* These are not set on construction, but are only settable once: "pseudo-immutable".
     * This makes it easier to create the objects from the DB.
     */
    private Food compositeFood;
    private Food ingredientFood;
    private QuantityUnit quantityUnit;
    private NutritionData nutritionData;
    // this is the only thing that may remain null after all initialisation is complete
    @Nullable
    private Serving serving;

    public Ingredient(ColumnData<Ingredient> data, ObjectSource objectSource) {
        super(data, objectSource);
        serving = null;
        quantityUnit = null;
        compositeFood = null;
        ingredientFood = null;

    }

    @Override
    public Table<Ingredient> getTable() {
        return Schema.IngredientTable.instance();
    }

    // we already use polymorphism to check the data is equal for subclasses of MacrosEntity;
    // the only thing that it misses out is checking that o is actually an instance of the subclass.
    @Override
    public boolean equals(Object o) {
        return o instanceof Ingredient && super.equals(o);
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
        return getData(Schema.IngredientTable.QUANTITY_UNIT);
    }

    public Food getCompositeFood() {
        return compositeFood;
    }

    public void setCompositeFood(@NotNull Food f) {
        assert (compositeFood == null);
        assert (getCompositeFoodId().equals(f.getId()));
        compositeFood = f;
    }

    public NutritionData getNutritionData() {
        return nutritionData;
    }

    @NotNull
    public Long getCompositeFoodId() {
        return getData(Schema.IngredientTable.COMPOSITE_FOOD_ID);
    }

    public Food getIngredientFood() {
        return ingredientFood;
    }

    public void setIngredientFood(@NotNull Food f) {
        assert (ingredientFood == null);
        assert (getIngredientFoodId().equals(f.getId()));
        ingredientFood = f;
        nutritionData = f.getNutritionData(getQuantity());
    }

    @NotNull
    public Long getIngredientFoodId() {
        return getData(Schema.IngredientTable.INGREDIENT_FOOD_ID);
    }

    @Nullable
    public Serving getServing() {
        return serving;
    }

    // for use during construction
    public void setServing(@NotNull Serving s) {
        assert (serving == null);
        assert (getServingId().equals(s.getId()));
        assert (getIngredientFoodId().equals(s.getFoodId()));
        serving = s;
    }

    @Nullable
    public Long getServingId() {
        return getData(Schema.IngredientTable.SERVING_ID);
    }

    @NotNull
    public Double getQuantity() {
        return getData(Schema.IngredientTable.QUANTITY);
    }

    @Nullable
    public String getNotes() {
        return getData(Schema.IngredientTable.NOTES);
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

