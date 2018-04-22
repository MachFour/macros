package com.machfour.macros.core;

import com.machfour.macros.data.Column;
import com.machfour.macros.data.ColumnData;
import com.machfour.macros.data.Table;
import com.machfour.macros.data.Tables;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.machfour.macros.data.Columns.FoodCol.*;

public class Food extends MacrosEntity<Food> {

    public static final List<Column<Food, String>> DESCRIPTION_COLUMNS = Arrays.asList(
        BRAND
        , COMMERCIAL_NAME
        , VARIETY_PREFIX_1
        , VARIETY_PREFIX_2
        , NAME
        , VARIETY_SUFFIX
        , NOTES
        , INDEX_NAME
    );

    private final List<Serving> servings;

    private NutritionData nutritionData;
    private FoodType foodType;

    public Food(ColumnData<Food> dataMap, boolean isFromDb) {
        super(dataMap, isFromDb);
        servings = new ArrayList<>();
        foodType = null;
        nutritionData = null;

    }

    public FoodType getFoodType() {
        return foodType;
    }

    public void setFoodType(@NotNull FoodType f) {
        assert (foodType == null);
        assert (f.toString().equals(getTypedDataForColumn(FOOD_TYPE)));
        foodType = f;
    }

    @Override
    public Table<Food> getTable() {
        return Tables.FoodTable.instance();
    }

    public void addServing(@NotNull Serving s) {
        assert (getId().equals(s.getFoodId()));
        assert (equals(s.getFood()));
        assert (!servings.contains(s));
        servings.add(s);
    }

    @Nullable
    public Long getUsdaIndex() {
        return getTypedDataForColumn(USDA_INDEX);
    }

    @Nullable
    public String getNuttabIndex() {
        return getTypedDataForColumn(NUTTAB_INDEX);
    }

    @Nullable
    public Double getDensity() {
        return getTypedDataForColumn(DENSITY);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Food && super.equals(o);
    }

    private String getDescriptionData(Column<Food, String> fieldName) {
        assert (DESCRIPTION_COLUMNS.contains(fieldName));
        return getTypedDataForColumn(fieldName);
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

    public Long getCategoryId() {
        return getTypedDataForColumn(CATEGORY);
    }

    private String prettyFormat(boolean withBrand, boolean withDetails,
                                boolean sortable) {
        StringBuilder prettyName = new StringBuilder(getDescriptionData(NAME));

        String vp1 = getDescriptionData(VARIETY_PREFIX_1);
        String vp2 = getDescriptionData(VARIETY_PREFIX_2);
        String vs = getDescriptionData(VARIETY_SUFFIX);

        if (sortable) {
            if (hasDescriptionData(COMMERCIAL_NAME)) {
                prettyName.append(", ");
                if (withBrand && hasDescriptionData(BRAND)) {
                    prettyName.append(getDescriptionData(BRAND)).append(" ");
                }
                prettyName.append(getDescriptionData(COMMERCIAL_NAME));
            } else {
                if (hasDescriptionData(VARIETY_PREFIX_1) && hasDescriptionData(VARIETY_PREFIX_2)) {
                    prettyName.append(", ").append(vp1).append(" ").append(vp2);
                } else if (hasDescriptionData(VARIETY_PREFIX_1)) {
                    prettyName.append(", ").append(vp1);
                } else if (hasDescriptionData(VARIETY_PREFIX_2)) {
                    prettyName.append(", ").append(vp2);
                }
                if (hasDescriptionData(VARIETY_SUFFIX)) {
                    prettyName.append(", ").append(vs);
                }
            }
        } else {
            if (hasDescriptionData(COMMERCIAL_NAME)) {
                prettyName.insert(0, getDescriptionData(COMMERCIAL_NAME) + " ");
            } else {
                if (hasDescriptionData(VARIETY_PREFIX_2)) {
                    prettyName.insert(0, vp2 + " ");
                }
                if (hasDescriptionData(VARIETY_PREFIX_1)) {
                    prettyName.insert(0, vp1 + " ");
                }
                if (hasDescriptionData(VARIETY_SUFFIX)) {
                    prettyName.append(" ").append(vs);
                }
            }
            if (withBrand && hasDescriptionData(BRAND)) {
                prettyName.insert(0, getDescriptionData(BRAND) + " ");
            }
        }

        if (withDetails && hasDescriptionData(NOTES)) {
            prettyName.append(" (").append(getDescriptionData(NOTES)).append(")");
        }

        return prettyName.toString();
    }

    /*
     * Order of fields:
     * if sortable:
     *   if commercialName is present:
     *     <sqlName>, <brand> <commercialName> (<details>)
     *   else
     *     <sqlName>, <varPrefix1> <varPrefix2>, <varSuffix>, <brand> (<details>)
     * else:
     *   if commercialName is present:
     *      <brand> <commercialName> <sqlName> (<details>)
     *   else:
     *      <brand> <varPrefix1> <varPrefix2> <sqlName> <varSuffix> (<details>)
     */

    private Boolean hasDescriptionData(Column<Food, String> fieldName) {
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
        assert (nutritionData == null);
        assert (getId().equals(nd.getFoodId()));
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
