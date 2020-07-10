package com.machfour.macros.objects;

import com.machfour.macros.core.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Ingredient extends MacrosEntityImpl<Ingredient> {

    /* These are not set on construction, but are only settable once: "pseudo-immutable".
     * This makes it easier to create the objects from the DB.
     */
    private Food compositeFood;
    private Food ingredientFood;
    private NutritionData nutritionData;
    @NotNull
    private final QtyUnit qtyUnit;
    // this is the only thing that may remain null after all initialisation is complete
    @Nullable
    private Serving serving;

    private Ingredient(ColumnData<Ingredient> data, ObjectSource objectSource) {
        super(data, objectSource);
        serving = null;
        qtyUnit = QtyUnits.fromAbbreviation(data.get(Schema.IngredientTable.QUANTITY_UNIT), true);
        compositeFood = null;
        ingredientFood = null;

    }

    public static Factory<Ingredient> factory() {
        return Ingredient::new;
    }
    @Override
    public Factory<Ingredient> getFactory() {
        return factory();
    }

    @Override
    public Table<Ingredient> getTable() {
        return table();
    }

    public static Table<Ingredient> table() {
        return Schema.IngredientTable.instance();
    }

    // we already use polymorphism to check the data is equal for subclasses of MacrosEntity;
    // the only thing that it misses out is checking that o is actually an instance of the subclass.
    @Override
    public boolean equals(Object o) {
        return o instanceof Ingredient && super.equals(o);
    }

    @NotNull
    public QtyUnit qtyUnit() {
        return qtyUnit;
    }

    @NotNull
    public String qtyUnitAbbr() {
        return getData(Schema.IngredientTable.QUANTITY_UNIT);
    }

    public Food getCompositeFood() {
        return compositeFood;
    }

    public void setCompositeFood(@NotNull Food f) {
        assert (compositeFood == null);
        assert f instanceof CompositeFood && f.getFoodType() == FoodType.COMPOSITE;
        assert (getCompositeFoodId().equals(f.getId()));
        compositeFood = f;
    }

    // not null only after setIngredientFood() is called
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
        assert ingredientFood == null && foreignKeyMatches(this, Schema.IngredientTable.INGREDIENT_FOOD_ID, f);
        ingredientFood = f;
        nutritionData = f.getNutritionData().rescale(quantity(), qtyUnit());
    }

    @NotNull
    public Long getIngredientFoodId() {
        return getData(Schema.IngredientTable.INGREDIENT_FOOD_ID);
    }

    @Nullable
    public Serving getServing() {
        return serving;
    }

    public void setServing(@NotNull Serving s) {
        assert serving == null && foreignKeyMatches(this, Schema.IngredientTable.SERVING_ID, s);
        assert (getIngredientFoodId().equals(s.getFoodId()));
        serving = s;
    }

    @Nullable
    public Long getServingId() {
        return getData(Schema.IngredientTable.SERVING_ID);
    }

    @NotNull
    public Double quantity() {
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
        return (serving != null) ? quantity() / serving.getQuantity() : 0;
    }
}

