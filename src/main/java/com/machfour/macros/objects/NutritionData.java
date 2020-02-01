package com.machfour.macros.objects;

import com.machfour.macros.core.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import com.machfour.macros.core.ObjectSource;
import com.machfour.macros.core.datatype.Types;

import static com.machfour.macros.core.Schema.NutritionDataTable.*;

// immutable class storing nutrition data for a food or meal
public class NutritionData extends MacrosEntity<NutritionData> {

    private static final double CAL_TO_KJ_FACTOR = 4.186;
    private static final double CALS_PER_G_PROTEIN = 17/CAL_TO_KJ_FACTOR;
    private static final double CALS_PER_G_FAT = 37/CAL_TO_KJ_FACTOR;
    private static final double CALS_PER_G_CARBOHYDRATE = 17/CAL_TO_KJ_FACTOR;
    private static final double CALS_PER_G_FIBRE = 8/CAL_TO_KJ_FACTOR;

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
        , POTASSIUM
        , CALCIUM
        , IRON
        , SALT
        , WATER
        , ALCOHOL
    );

    /*
     * For units
     */
    private static final List<Column<NutritionData, Double>> ENERGY_COLS = Arrays.asList(
            CALORIES, KILOJOULES
    );

    private static final Map<Column<NutritionData, Double>, String> unitString;
    static {
        unitString = new HashMap<>(NUTRIENT_COLUMNS.size());
        for (Column<NutritionData, Double> col : NUTRIENT_COLUMNS) {
            if (col.equals(SODIUM) || col.equals(CALCIUM) || col.equals(OMEGA_3_FAT) || col.equals(OMEGA_6_FAT)) {
                NutritionData.unitString.put(col, "mg");
            } else if (col.equals(CALORIES)) {
                NutritionData.unitString.put(col, "cal");
            } else if (col.equals(KILOJOULES)) {
                NutritionData.unitString.put(col, "kj");
            } else {
                NutritionData.unitString.put(col, "g");
            }
        }
    }

    // keeps track of missing data for adding different instances of NutritionDataTable together
    // only NUTRIENT_COLUMNS are present in this map
    private final Map<Column<NutritionData, Double>, Boolean> completeData;
    // measured in grams, per specified quantity
    @NotNull
    private final QtyUnit qtyUnit;
    private Food food;

    private NutritionData(ColumnData<NutritionData> dataMap, ObjectSource objectSource) {
        super(dataMap, objectSource);
        // food ID is allowed to be null only if this NutritionData is computed from a sum
        assert (objectSource == ObjectSource.COMPUTED || dataMap.get(FOOD_ID) != null);
        completeData = new HashMap<>(NUTRIENT_COLUMNS.size());
        // have to use temporary due to type parameterisation
        for (Column<NutritionData, Double> c : NUTRIENT_COLUMNS) {
            completeData.put(c, dataMap.hasData(c));
        }
        // account for energy conversion
        boolean hasEnergy = completeData.get(CALORIES) || completeData.get(KILOJOULES);
        completeData.put(CALORIES, hasEnergy);
        completeData.put(KILOJOULES, hasEnergy);
        qtyUnit = QtyUnit.fromAbbreviation(dataMap.get(QUANTITY_UNIT), true);
    }

    public static Table<NutritionData> table() {
        return Schema.NutritionDataTable.instance();
    }

    public static Factory<NutritionData> factory() {
        return NutritionData::new;
    }
    @Override
    public Factory<NutritionData> getFactory() {
        return factory();
    }

    // allows keeping track of missing data when different nData instances are added up
    private NutritionData(ColumnData<NutritionData> dataMap, ObjectSource objectSource,
                          Map<Column<NutritionData, Double>, Boolean> completeData) {
        this(dataMap, objectSource);
        for (Column<NutritionData, Double> c : NUTRIENT_COLUMNS) {
            if (!completeData.get(c)) {
                this.completeData.put(c, false);
            } else {
                // we shouldn't need to correct this case, since if the thing is null (and so makeHasDataMap
                // said it was not present), then we shouldn't be correcting that to say that it is present!
                assert (this.completeData.get(c));
            }
        }
    }

    @NotNull
    public static String getUnitForNutrient(Column<NutritionData, Double> col) {
        assert NUTRIENT_COLUMNS.contains(col);
        return unitString.get(col);
    }

    /*
      Result is always converted to grams.
     */
    public static NutritionData sum(List<NutritionData> components) {
        return sum(components, false);
    }
    public static NutritionData sum(List<NutritionData> components, boolean combineDensities) {
        double sumQuantity = 0;
        double unnormalisedDensity = 0; // need to divide by sumQuantity at the end

        Map<Column<NutritionData, Double>, Double> sumData = new HashMap<>(NUTRIENT_COLUMNS.size(), 1);
        Map<Column<NutritionData, Double>, Boolean> combinedHasData = new HashMap<>(NUTRIENT_COLUMNS.size(), 1);
        for (Column<NutritionData, Double> col : NUTRIENT_COLUMNS) {
            sumData.put(col, 0.0);
            combinedHasData.put(col, true);
        }
        for (NutritionData nd : components) {
            NutritionData ndToSum = nd.convertToGramsIfNecessary();
            double quantity = ndToSum.getQuantity();
            double density = 1.0; // default guess
            if (ndToSum.getDensity() == null || !ndToSum.hasCompleteData(QUANTITY)) {
                // means we guessed the density
                combinedHasData.put(QUANTITY, false);
            } else {
                density = ndToSum.getDensity();
            }
            sumQuantity += quantity;
            // gradually calculate overall density via weighted sum of densities
            unnormalisedDensity += density*quantity;

            for (Column<NutritionData, Double> col : NUTRIENT_COLUMNS) {
                // total has correct data for a field if and only if each component does
                // if the current component has no data for a field, we add nothing to the total,
                // implicitly treating it as zero
                if (!ndToSum.hasCompleteData(col)) {
                    combinedHasData.put(col, false);
                }
                Double colData = ndToSum.amountOf(col, 0.0);
                sumData.put(col, sumData.get(col) + colData);
            }
        }
        ColumnData<NutritionData> combinedDataMap = new ColumnData<>(Schema.NutritionDataTable.instance());
        for (Column<NutritionData, Double> col : NUTRIENT_COLUMNS) {
            combinedDataMap.put(col, sumData.get(col));
        }
        double combinedDensity = unnormalisedDensity/sumQuantity;
        if (combineDensities) {
            combinedDataMap.put(DENSITY, combinedDensity);
        } else {
            combinedDataMap.put(DENSITY, null);
        }
        combinedDataMap.put(QUANTITY, sumQuantity);
        combinedDataMap.put(QUANTITY_UNIT, QtyUnit.GRAMS.abbr());
        combinedDataMap.put(FOOD_ID, null);
        combinedDataMap.put(DATA_SOURCE, "Sum");

        // TODO add food if all of the ingredients were the same food?
        return new NutritionData(combinedDataMap, ObjectSource.COMPUTED, combinedHasData);
    }

    // Uses data from the secondary object to fill in missing values from the first
    // Any mismatches are ignored; the primary data is used
    public static NutritionData combine(NutritionData primary, NutritionData secondary) {
        // TODO check this logic
        //if (secondary.getQuantity() != primary.getQuantity()) {
        //    secondary = secondary.rescale(primary.getQuantity());
        //}
        ColumnData<NutritionData> combinedDataMap = primary.copyDataForNew();
        Map<Column<NutritionData, Double>, Boolean> combinedHasData = new HashMap<>(NUTRIENT_COLUMNS.size(), 1);

        for (Column<NutritionData, Double> col : NUTRIENT_COLUMNS) {
            // note: hasCompleteData is a stricter condition than hasData:
            // hasCompleteData can be false even if there is a non-null value for that column, when the
            // nData object was produced by summation and there was at least one food with missing data.
            // for this purpose, we'll only replace the primary data if it was null
            if (!primary.hasData(col) && secondary.hasData(col)) {
                combinedDataMap.put(col, secondary.getData(col));
                // !hasData implies !hasCompleteData, so we use the secondary value
                combinedHasData.put(col, secondary.hasCompleteData(col));
            } else {
                combinedHasData.put(col, primary.hasCompleteData(col));
            }
        }
        combinedDataMap.put(DATA_SOURCE, "Composite data");
        return new NutritionData(combinedDataMap, ObjectSource.COMPUTED, combinedHasData);

    }

    @Nullable
    public Long getFoodId() {
        return getData(FOOD_ID);
    }

    public Food getFood() {
        return food;
    }

    public void setFood(@NotNull Food f) {
        assert food == null && foreignKeyMatches(this, FOOD_ID, f);
        food = f;
    }

    @NotNull
    public QtyUnit qtyUnit() {
        return qtyUnit;
    }

    @NotNull
    public String qtyUnitAbbr() {
        return getData(QUANTITY_UNIT);
    }

    @Override
    public Table<NutritionData> getTable() {
        return instance();
    }

    @Nullable
    private Double getEnergyAs(Column<NutritionData, Double> energyCol) {
        assert ENERGY_COLS.contains(energyCol);
        Double toValue = getData(energyCol);
        if (toValue != null) {
            return toValue;
        } else if (energyCol.equals(CALORIES)) {
            Double kjData = getData(KILOJOULES);
            return kjData != null ? kjData / CAL_TO_KJ_FACTOR : null;
        } else { // energyCol.equals(KILOJOULES)
            Double calData = getData(CALORIES);
            return calData != null ? calData * CAL_TO_KJ_FACTOR : null;
        }
    }

    @Nullable
    public String getDataSource() {
        return getData(DATA_SOURCE);
    }

    // hack for USDA foods
    // subtracts fibre from carbohydrate if necessary to produce a carbohydrate amount
    // If fibre is not present, returns just carbs by diff
    // If there is not enough data to do that, return 0.
    @NotNull
    private Double getCarbsBestEffort() {
        if (hasCompleteData(CARBOHYDRATE)) {
            return amountOf(CARBOHYDRATE);
        } else if (hasCompleteData(CARBOHYDRATE_BY_DIFF) && hasCompleteData(FIBRE)) {
            return amountOf(CARBOHYDRATE_BY_DIFF) - amountOf(FIBRE);
        } else if (hasCompleteData(CARBOHYDRATE_BY_DIFF)) {
            return amountOf(CARBOHYDRATE_BY_DIFF);
        } else {
            return 0.0;
        }
    }

    public boolean hasCompleteData(Column<NutritionData, Double> col) {
        assert completeData.containsKey(col);
        return completeData.get(col);
    }

    /*
     * WONTFIX fundamental problem with unit conversion
     * In the database, nutrient quantities are always considered by-weight, while quantities
     * of a food (or serving, FoodPortionTable, etc.) can be either by-weight or by volume.
     * Converting from a by-weight quantity unit to a by-volume one, or vice-versa, for a
     * NutritionData object, then, must keep the actual (gram) values of the data the same,
     * and simply change the corresponding quantity, according to the given density value.
     *
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
     * 1. Just converting the quantity unit, which means only the value of the quantity column changes.
     *    All the nutrition data stays the same, in grams. [This is what we'll do now]
     * 2. Converting the entire row of data for display purposes. [This will come later on]
     *    (e.g. 30g nutrient X / 120g quantity --> 1 oz nutrient X / 4 oz quantity.)
     *    This only works for mass units, not when the quantity unit is in ml
     */
    // Unless the target unit is identical to the current unit
    // returns a new NutritionDataTable object (not from DB) with the converted data.
    // nutrient values always remain in grams.
    private NutritionData convertQuantityUnit(@NotNull QtyUnit targetUnit, double density, boolean isDensityGuessed) {
        QtyUnit currentUnit = qtyUnit();
        if (currentUnit.equals(targetUnit)) {
            // TODO should it be a copy?
            return this;
        }
        // if converting between volume and mass quantity units, we need to change the quantity value
        // according to the density.
        // e.g. per 100mL is the same as per 92g, when the density is 0.92
        double ratio = currentUnit.metricEquivalent() / targetUnit.metricEquivalent();
        if (!currentUnit.isVolumeUnit() && targetUnit.isVolumeUnit()) {
            ratio /= density;
        } else if (currentUnit.isVolumeUnit() && !targetUnit.isVolumeUnit()) { // liquid units to solid units
            ratio *= density;
        }
        // else ratio *= 1.0;
        double newQuantity = getQuantity() * ratio;
        ColumnData<NutritionData> newData = copyDataForNew();
        newData.put(QUANTITY, newQuantity);
        newData.put(QUANTITY_UNIT, targetUnit.abbr());
        newData.put(DENSITY, density);
        // all other data remains the same
        Map<Column<NutritionData, Double>, Boolean> newHasData = new HashMap<>(completeData);
        if (isDensityGuessed) {
            newHasData.put(QUANTITY, false);
        }
        NutritionData converted = new NutritionData(newData, ObjectSource.COMPUTED, newHasData);
        if (hasFood()) {
            converted.setFood(getFood());
        }
        return converted;
    }

    private ColumnData<NutritionData> copyDataForNew() {
        ColumnData<NutritionData> copy = getAllData().copy();
        // have to remove ID since it's now a computed value
        copy.put(ID, NO_ID);
        return copy;
    }

    public Map<Column<NutritionData, Double>, Double> makeEnergyProportionsMap() {
        // preserve iteration order
        Map<Column<NutritionData, Double>, Double> proportionMap = new LinkedHashMap<>();
        // energy from...
        double protein = amountOf(PROTEIN, 0.0)*CALS_PER_G_PROTEIN;
        double fat = amountOf(FAT, 0.0)*CALS_PER_G_FAT;
        double carb = amountOf(CARBOHYDRATE, 0.0)*CALS_PER_G_CARBOHYDRATE;
        double sugar = amountOf(SUGAR, 0.0)*CALS_PER_G_CARBOHYDRATE;
        double fibre = amountOf(FIBRE, 0.0)*CALS_PER_G_FIBRE;
        double satFat = amountOf(SATURATED_FAT, 0.0)*CALS_PER_G_FAT;
        // correct subtypes (sugar is part of carbs, saturated is part of fat)
        carb = Math.max(carb - sugar, 0);
        fat = Math.max(fat - satFat, 0);
        // if total energy is missing, fallback to summing over previous energy quantities
        double totalEnergy = amountOf(CALORIES, protein + fat + satFat + carb + sugar + fibre);
        if (totalEnergy > 0) {
            proportionMap.put(PROTEIN, protein/totalEnergy*100);
            proportionMap.put(FAT, fat/totalEnergy*100);
            proportionMap.put(SATURATED_FAT, satFat/totalEnergy*100);
            proportionMap.put(CARBOHYDRATE, carb/totalEnergy*100);
            proportionMap.put(SUGAR, sugar/totalEnergy*100);
            proportionMap.put(FIBRE, fibre/totalEnergy*100);
        } else {
            proportionMap.put(PROTEIN, 0.0);
            proportionMap.put(FAT, 0.0);
            proportionMap.put(SATURATED_FAT, 0.0);
            proportionMap.put(CARBOHYDRATE, 0.0);
            proportionMap.put(SUGAR, 0.0);
            proportionMap.put(FIBRE, 0.0);
        }

        return proportionMap;
    }


    @Nullable
    public Double getDensity() {
        return getData(DENSITY);
    }

    private NutritionData convertToGramsIfNecessary() {
        if (!qtyUnit().equals(QtyUnit.GRAMS)) {
            // then convert to grams, guessing density if required
            double density;
            boolean guessedDensity;
            // TODO why do we need to check hasFood()?
            if (/*hasFood() && */ getDensity() != null) {
                density = getDensity();
                guessedDensity = false;
            } else {
                density = 1.0;
                guessedDensity = true;

            }
            return convertQuantityUnit(QtyUnit.GRAMS, density, guessedDensity);
        } else {
            return this;
        }
    }

    public boolean hasFood() {
        return getFood() != null;
    }

    // For current nutrition values in this object, per given current quantity,
    // returns a new nData object with the nutrition values rescaled to
    // correspond to the new quantity, in the same unit
    @NotNull
    public NutritionData rescale(double newQuantity) {
        double conversionRatio = newQuantity / getQuantity();
        ColumnData<NutritionData> newData = copyDataForNew();
        for (Column<NutritionData, Double> c : NUTRIENT_COLUMNS) {
            //if (hasCompleteData(c)) {
            if (hasData(c)) {
                // hasData() check avoids NullPointerException
                newData.put(c, amountOf(c) * conversionRatio);
            }
        }
        NutritionData rescaled = new NutritionData(newData, ObjectSource.COMPUTED);
        if (hasFood()) {
            rescaled.setFood(getFood());
        }
        return rescaled;
    }


    // For current nutrition values in this object, per given current quantity,
    // returns a new nData object with the nutrition values rescaled to
    // correspond to the new quantity, in the new unit
    @NotNull
    public NutritionData rescale(double newQuantity, QtyUnit newUnit) {
        // default density for rescaling is 1.0
        double density = getDensity() != null ? getDensity() : 1.0;
        boolean isDensityGuessed = getDensity() == null;
        NutritionData convertedUnit = convertQuantityUnit(newUnit, density, isDensityGuessed);
        return convertedUnit.rescale(newQuantity);
    }

    public double getQuantity() {
        return getData(QUANTITY);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof NutritionData && super.equals(o);
    }

    @Nullable
    public Double amountOf(@NotNull Column<NutritionData, Double> col) {
        assert (NUTRIENT_COLUMNS.contains(col));
        if (ENERGY_COLS.contains(col)) {
            // return any energy value, converting if necessary. Return null if neither column.
            return getEnergyAs(col);
            // TODO sodium/salt
            // TODO carbs by difference / carbs
        } else {
            return getData(col);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean hasData(@NotNull Column<NutritionData, ?> col) {
        if (col.getType().equals(Types.REAL)) {
            // ColumnType of REAL ensures the cast will work
            Column<NutritionData, Double> doubleCol = (Column<NutritionData, Double>) col;
            if (NUTRIENT_COLUMNS.contains(doubleCol)) {
                return amountOf(doubleCol) != null;
            }
        }
        // fall back to just checking the columnData
        return super.hasData(col);
    }

    public double amountOf(Column<NutritionData, Double> col, double defaultValue) {
        Double data = amountOf(col);
        return data != null ? data : defaultValue;
    }
}



