package com.machfour.macros.core;

import com.machfour.macros.data.*;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.machfour.macros.data.Columns.NutritionDataCol.*;

// immutable class storing nutrition data for a food or meal
public class NutritionData extends MacrosEntity<NutritionData> {

    public static final double CAL_TO_KJ_FACTOR = 4.186;

    // measured in the relevant FoodTable's QuantityUnits
    public static final double DEFAULT_QUANTITY = 100;
    public static final List<Column<NutritionData, Double>> NUTRIENT_COLUMNS = Arrays.asList(
        QUANTITY
        , KILOJOULES
        , CALORIES
        , PROTEIN
        , CARBOHYDRATE
        , CARBOHYDRATE_BY_DIFF
        , SUGAR
        , SUGAR_ALCOHOL
        , STARCH
        , FAT
        , SATURATED_FAT
        , MONOUNSATURATED_FAT
        , POLYUNSATURATED_FAT
        , OMEGA_3_FAT
        , OMEGA_6_FAT
        , FIBRE
        , SODIUM
        , CALCIUM
        , SALT
        , WATER
        , ALCOHOL
    );
    // keeps track of missing data for adding different instances of NutritionDataTable together
    // only NUTRIENT_COLUMNS are present in this map
    private final Map<Column<NutritionData, Double>, Boolean> hasNutrient;
    // measured in grams, per specified quantity
    private QuantityUnit quantityUnit;
    private Food food;

    public NutritionData(ColumnData<NutritionData> dataMap, boolean isFromDb) {
        super(dataMap, isFromDb);
        hasNutrient = new HashMap<>(NUTRIENT_COLUMNS.size());
        // have to use temporary due to type parameterisation
        for (Column<NutritionData, Double> c : NUTRIENT_COLUMNS) {
            hasNutrient.put(c, dataMap.hasData(c));
        }
    }

    // allows keeping track of missing data when different nutritionData instances are added up
    private NutritionData(ColumnData<NutritionData> dataMap, boolean isFromDb, Map<Column<NutritionData, Double>, Boolean> reliableData) {
        this(dataMap, isFromDb);
        for (Column<NutritionData, Double> c : NUTRIENT_COLUMNS) {
            if (!reliableData.get(c)) {
                hasNutrient.put(c, false);
            } else {
                // we shouldn't need to correct this case, since if the thing is null (and so makeHasDataMap
                // said it was not present), then we shouldn't be correcting that to say that it is present!
                assert (hasNutrient.get(c));
            }
        }
    }

    public static NutritionData sum(List<NutritionData> components) {
        double sumQuantity = 0;

        Map<Column<NutritionData, Double>, Double> sumData = new HashMap<>(NUTRIENT_COLUMNS.size(), 1);
        Map<Column<NutritionData, Double>, Boolean> combinedHasData = new HashMap<>(NUTRIENT_COLUMNS.size(), 1);
        for (Column<NutritionData, Double> col : NUTRIENT_COLUMNS) {
            sumData.put(col, 0.0);
            combinedHasData.put(col, true);
        }
        for (NutritionData nd : components) {
            NutritionData ndToSum = nd.convertToGramsIfNecessary();
            sumQuantity += ndToSum.getQuantity();
            if (!ndToSum.hasNutrient(QUANTITY)) {
                // means we guessed the density
                combinedHasData.put(QUANTITY, false);
            }
            for (Column<NutritionData, Double> col : NUTRIENT_COLUMNS) {
                // total has correct data for a field if and only if each component does
                // if the current component has no data for a field, we add nothing to the total,
                // implicitly treating it as zero
                if (!ndToSum.hasNutrient(col)) {
                    combinedHasData.put(col, false);
                } else {
                    // don't need to modify combinedHasData because X & True == X;
                    sumData.put(col, sumData.get(col) + ndToSum.getNutrientData(col));
                }
            }
        }
        ColumnData<NutritionData> combinedDataMap = new ColumnData<>(Tables.NutritionDataTable.instance());
        for (Column<NutritionData, Double> col : NUTRIENT_COLUMNS) {
            combinedDataMap.putData(col, sumData.get(col));
        }
        combinedDataMap.putData(QUANTITY, sumQuantity);
        combinedDataMap.putData(QUANTITY_UNIT, QuantityUnit.GRAMS.getId());
        combinedDataMap.putData(FOOD_ID, null);
        combinedDataMap.putData(Columns.NutritionDataCol.DATA_SOURCE, "Sum");
        return new NutritionData(combinedDataMap, false, combinedHasData);
    }

    @Nullable
    public Long getFoodId() {
        return getTypedDataForColumn(Columns.NutritionDataCol.FOOD_ID);
    }

    public Food getFood() {
        return food;
    }

    public void setFood(@NotNull Food f) {
        assert (food == null);
        assert (getFoodId().equals(f.getId()));
        food = f;
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
        return getTypedDataForColumn(Columns.NutritionDataCol.QUANTITY_UNIT);
    }

    @Override
    public Table<NutritionData> getTable() {
        return Tables.NutritionDataTable.instance();
    }

    @NotNull
    // converts the energy quantity in units of the 'From' column to that of the 'to' column.
    // If the 'from' column has a null value, returns 0.
    private Double convertEnergy(Column<NutritionData, Double> from, Column<NutritionData, Double> to) {
        assert (from == CALORIES || from == KILOJOULES);
        assert (to == CALORIES || to == KILOJOULES);
        if (!hasNutrient(from)) {
            return 0.0;
        } else if (from == to) {
            return getNutrientData(from);
        } else if (from == KILOJOULES) { // to == CALORIES
            return getNutrientData(from) / CAL_TO_KJ_FACTOR;
        } else { // from == CALORIES && to == KILOJOULES
            return getNutrientData(from) * CAL_TO_KJ_FACTOR;
        }
    }

    // hack for USDA foods
    // subtracts fibre from carbohydrate if necessary to produce a carbohydrate amount
    // If fibre is not present, returns just carbs by diff
    // If there is not enough data to do that, return 0.
    @NotNull
    private Double getCarbsBestEffort() {
        if (hasNutrient(CARBOHYDRATE)) {
            return getNutrientData(CARBOHYDRATE);
        } else if (hasNutrient(CARBOHYDRATE_BY_DIFF) && hasNutrient(FIBRE)) {
            return getNutrientData(CARBOHYDRATE_BY_DIFF) - getNutrientData(FIBRE);
        } else if (hasNutrient(CARBOHYDRATE_BY_DIFF)) {
            return getNutrientData(CARBOHYDRATE_BY_DIFF);
        } else {
            return 0.0;
        }
    }

    /*
     * WONTFIX fundamental problem with unit conversion
     * In the database, nutrient quantities are stored with the implicit assumption that they
     * are always a by-weight quantity, while the quantity of the food (or serving, FoodPortionTable, etc.)
     * can be in either weight or volume measurements.
     * Converting from a by-weight quantity unit to a by-volume one, or vice-versa, for a
     * NutritionDataTable object, still keeps the actual (gram) values of the data the same,
     * but simply means changing the corresponding quantity, according to the given density value.
     * Converting between different units of the same measurement (either weight or volume), then,
     * only makes sense if it means changing the actual numerical data in each column, such that,
     * when interpreted in the new unit, still means the same as the old one, when both are converted to grams.
     * But this makes no sense for calculations, since the unit has to be the same when adding things together.
     *
     * For now, we'll say that as far as data storage and calculations are concerned,
     * the only unit of mass used is grams, and the only unit of volume used will be ml.
     * NOTE, however, that this does not mean that the units used for input and output of data
     * to/from the user needs to be in these units.
     * Later on, we'll need a separate system to convert units for user display.
     * So I guess there are two distinct 'unit convert' operations that need to be considered.
     * 1. Just converting the quantity unit, which means the only numerical value changed is the quantity column.
     *    All the nutrition data stays the same, in grams. [This is what we'll do now]
     * 2. Converting the entire row of data for display purposes. [This will come later on]
     *    (e.g. 30g nutrient X / 120g quantity --> 1 oz nutrient X / 4 oz quantity.)
     *    This only works for mass units, not when the quantity unit is in ml
     *
     */

    public boolean hasNutrient(Column<NutritionData, Double> col) {
        return hasNutrient.get(col);
    }

    // Unless the target unit is identical to the current unit
    // returns a new NutritionDataTable object (not from DB) with the converted data.
    // nutrient values always remain in grams.
    private NutritionData convertQuantityUnit(@NotNull QuantityUnit targetUnit, double density, boolean isDensityGuessed) {
        QuantityUnit currentUnit = getQuantityUnit();
        if (currentUnit.equals(targetUnit)) {
            // TODO should it be a copy?
            return this;
        }
        // if converting between volume and mass quantity units, we need to change the quantity value
        // according to the density.
        double ratio = currentUnit.metricEquivalent() / targetUnit.metricEquivalent();
        if (!currentUnit.isVolumeUnit() && targetUnit.isVolumeUnit()) {
            ratio *= density;
        } else if (currentUnit.isVolumeUnit() && !targetUnit.isVolumeUnit()) { // liquid units to solid units
            ratio /= density;
        }
        double newQuantity = getQuantity() * ratio;
        ColumnData<NutritionData> newData = getAllData(); // all other data remains the same
        newData.putData(QUANTITY, newQuantity);
        newData.putData(QUANTITY_UNIT, targetUnit.getId());
        Map<Column<NutritionData, Double>, Boolean> newHasData = new HashMap<>(hasNutrient);
        if (isDensityGuessed) {
            newHasData.put(QUANTITY, false);
        }
        NutritionData converted = new NutritionData(newData, false, newHasData);
        if (hasFood()) {
            converted.setFood(getFood());
        }
        converted.setQuantityUnit(targetUnit);
        return converted;
    }

    private NutritionData convertToGramsIfNecessary() {
        if (!getQuantityUnit().equals(QuantityUnit.GRAMS)) {
            // then convert to grams, guessing density if required
            double density;
            boolean guessedDensity;
            if (hasFood() && getFood().getDensity() != null) {
                density = getFood().getDensity();
                guessedDensity = false;
            } else {
                density = 1.0;
                guessedDensity = true;

            }
            return convertQuantityUnit(QuantityUnit.GRAMS, density, guessedDensity);
        } else {
            return this;
        }
    }

    public boolean hasFood() {
        return getFood() == null;
    }

    // Sums the nutrition data components, converting them to grams first, if necessary.

    public NutritionData rescale(double quantity) {
        double conversionRatio = quantity / getQuantity();
        ColumnData<NutritionData> newData = getAllData();
        for (Column<NutritionData, Double> c : NUTRIENT_COLUMNS) {
            if (hasNutrient(c)) {
                newData.putData(c, getNutrientData(c) * conversionRatio);
            }
        }
        return new NutritionData(newData, false);
    }

    @NotNull
    public Double getQuantity() {
        return getTypedDataForColumn(Columns.NutritionDataCol.QUANTITY);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof NutritionData && super.equals(o);
    }

    @Nullable
    public Double getNutrientData(Column<NutritionData, Double> col) {
        return getNutrientData(col, null);
    }

    public Double getNutrientData(Column<NutritionData, Double> col, Double defaultValue) {
        assert (NUTRIENT_COLUMNS.contains(col));
        return hasNutrient(col) ? getTypedDataForColumn(col) : defaultValue;
    }
}


