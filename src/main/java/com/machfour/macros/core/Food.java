package com.machfour.macros.core;

import com.machfour.macros.data.*;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Food extends MacrosEntity<Food> {

    public static final List<Column<Food, String>> DESCRIPTION_COLUMNS = Arrays.asList(
        Schema.FoodTable.BRAND
        , Schema.FoodTable.VARIETY
        , Schema.FoodTable.NAME
        , Schema.FoodTable.NOTES
        , Schema.FoodTable.INDEX_NAME
    );

    private final List<Serving> servings;

    private NutritionData nutritionData;
    private FoodType foodType;
    private FoodCategory foodCategory;

    public Food(ColumnData<Food> dataMap, ObjectSource objectSource) {
        super(dataMap, objectSource);
        servings = new ArrayList<>();
        foodType = null;
        nutritionData = null;

    }

    public FoodType getFoodType() {
        return foodType;
    }

    public void setFoodType(@NotNull FoodType f) {
        assert foodType == null && f.toString().equals(getData(Schema.FoodTable.FOOD_TYPE));
        foodType = f;
    }

    public void setFoodCategory(@NotNull FoodCategory c) {
        assert foodCategory == null && foreignKeyMatches(this, Schema.FoodTable.CATEGORY, c);
        foodCategory = c;
    }
    public FoodCategory getFoodCategory() {
        return foodCategory;
    }

    @Override
    public Table<Food> getTable() {
        return Schema.FoodTable.instance();
    }

    public void addServing(@NotNull Serving s) {
        assert (!servings.contains(s)) && foreignKeyMatches(s, Schema.ServingTable.FOOD_ID, this);
        servings.add(s);
    }

    @Nullable
    public Long getUsdaIndex() {
        return getData(Schema.FoodTable.USDA_INDEX);
    }

    @Nullable
    public String getNuttabIndex() {
        return getData(Schema.FoodTable.NUTTAB_INDEX);
    }


    @Override
    public boolean equals(Object o) {
        return o instanceof Food && super.equals(o);
    }

    private String getDescriptionData(Column<Food, String> fieldName) {
        assert (DESCRIPTION_COLUMNS.contains(fieldName));
        return getData(fieldName);
    }

    public String getShortName() {
        return prettyFormat(false, false, false);
    }

    public String getLongName() {
        return prettyFormat(true, true, false);
    }

    public String getMediumName() {
        return prettyFormat(true, false, false);
    }

    public String getSortableName() {
        return prettyFormat(true, true, true);
    }

    public String getCategoryId() {
        return getData(Schema.FoodTable.CATEGORY);
    }

    private String prettyFormat(boolean withBrand, boolean withNotes, boolean sortable) {
        StringBuilder prettyName = new StringBuilder(getDescriptionData(Schema.FoodTable.NAME));

        String v = getDescriptionData(Schema.FoodTable.VARIETY);

        if (sortable) {
            if (withBrand && hasDescriptionData(Schema.FoodTable.BRAND)) {
                prettyName.append(getDescriptionData(Schema.FoodTable.BRAND)).append(", ");
            }
            if (hasDescriptionData(Schema.FoodTable.VARIETY)) {
                prettyName.append(", ").append(v);
            }
        } else {
            if (hasDescriptionData(Schema.FoodTable.VARIETY)) {
                if (getData(Schema.FoodTable.VARIETY_AFTER_NAME)) {
                    prettyName.append(" ").append(v);
                } else {
                    prettyName.insert(0, v + " ");
                }
            }
            if (withBrand && hasDescriptionData(Schema.FoodTable.BRAND)) {
                prettyName.insert(0, getDescriptionData(Schema.FoodTable.BRAND) + " ");
            }
        }

        if (withNotes && hasDescriptionData(Schema.FoodTable.NOTES)) {
            prettyName.append(" (").append(getDescriptionData(Schema.FoodTable.NOTES)).append(")");
        }

        return prettyName.toString();
    }

    /*
     * Order of fields:
     * if sortable:
     *     <name>, <brand>, <variety> (<notes>)
     * else if variety_after_name is present:
     *     <brand> <name> <variety> (<notes>)
     * else:
     *     <brand> <variety> <name> (<notes>)
     */

    private boolean hasDescriptionData(Column<Food, String> fieldName) {
        return hasData(fieldName);
    }

    @Nullable
    public Serving getDefaultServing() {
        Serving defaultServing = null;
        for (Serving s : servings) {
            if (s.isDefault()) {
                defaultServing = s;
            }
        }
        return defaultServing;
    }

    // quantity corresponds to that assumed by the data in the DB
    public NutritionData getNutritionData() {
        return nutritionData;
    }

    public void setNutritionData(@NotNull NutritionData nd) {
        assert nutritionData == null && foreignKeyMatches(nd, Schema.NutritionDataTable.FOOD_ID, this);
        nutritionData = nd;
    }

    public NutritionData getNutritionData(double quantity) {
        return nutritionData.rescale(quantity);
    }

    @Nullable
    public Serving getServingById(long servingId) {
        Serving serving = null;
        for (Serving s : servings) {
            if (servingId == s.getId()) {
                serving = s;
            }
        }
        return serving;
    }

    public List<Serving> getServings() {
        return new ArrayList<>(servings);
    }
}
