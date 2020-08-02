package com.machfour.macros.objects

import com.machfour.macros.core.*

object NutritionCalculations {

    private fun convertToGramsIfNecessary(nd: NutritionData): NutritionData {
        return when (nd.qtyUnit == QtyUnits.GRAMS) {
            true -> nd
            else -> {
                // then convert to grams, guessing density if required
                val guessDensity = nd.density == null
                val density = nd.density ?: 1.0
                convertQuantityUnit(nd, QtyUnits.GRAMS, density, guessDensity)
            }
        }
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
    internal fun convertQuantityUnit(nd: NutritionData, targetUnit: QtyUnit, density: Double, isDensityGuessed: Boolean): NutritionData {
        val currentUnit = nd.qtyUnit
        if (currentUnit == targetUnit) {
            // TODO should it be a copy?
            return nd
        }
        // if converting between volume and mass quantity units, we need to change the quantity value
        // according to the density.
        // e.g. per 100mL is the same as per 92g, when the density is 0.92
        var ratio = currentUnit.metricEquivalent() / targetUnit.metricEquivalent()
        if (!currentUnit.isVolumeUnit && targetUnit.isVolumeUnit) {
            ratio /= density
        } else if (currentUnit.isVolumeUnit && !targetUnit.isVolumeUnit) { // liquid units to solid units
            ratio *= density
        }
        // else ratio *= 1.0;
        val newQuantity = nd.quantity * ratio
        val newData = copyDataForNew(nd)
        newData.put(Schema.NutritionDataTable.QUANTITY, newQuantity)
        newData.put(Schema.NutritionDataTable.QUANTITY_UNIT, targetUnit.abbr)
        newData.put(Schema.NutritionDataTable.DENSITY, density)
        // all other data remains the same
        val newHasData = nd.completeDataMap()
        if (isDensityGuessed) {
            newHasData[Schema.NutritionDataTable.QUANTITY] = false
        }
        val converted = NutritionData(newData, ObjectSource.COMPUTED, newHasData)
        if (nd.hasFood()) {
            converted.food = nd.food
        }
        return converted
    }

    // For current nutrition values in this object, per given current quantity,
    // returns a new nData object with the nutrition values rescaled to
    // correspond to the new quantity, in the new unit
    fun rescale(nd: NutritionData, newQuantity: Double, newUnit: QtyUnit): NutritionData {
        // default density for rescaling is 1.0
        val guessDensity = nd.density == null
        val density = nd.density ?: 1.0
        val convertedUnit = convertQuantityUnit(nd, newUnit, density, guessDensity)
        return rescale(convertedUnit, newQuantity)
    }


    /* Result is always converted to grams. */
    fun sum(components: List<NutritionData>, combineDensities: Boolean = false): NutritionData {
        var sumQuantity = 0.0
        var unnormalisedDensity = 0.0 // need to divide by sumQuantity at the end

        val sumData = HashMap<Column<NutritionData, Double>, Double>(NutritionData.NUTRIENT_COLUMNS.size, 1f)
        val combinedHasData = HashMap<Column<NutritionData, Double>, Boolean>(NutritionData.NUTRIENT_COLUMNS.size, 1f)
        for (col in NutritionData.NUTRIENT_COLUMNS) {
            sumData[col] = 0.0
            combinedHasData[col] = true
        }
        for (nd in components) {
            val ndToSum =
                    convertToGramsIfNecessary(
                        nd)
            val quantity = ndToSum.quantity
            var density = 1.0 // default guess
            if (ndToSum.density == null || !ndToSum.hasCompleteData(Schema.NutritionDataTable.QUANTITY)) {
                // means we guessed the density
                combinedHasData[Schema.NutritionDataTable.QUANTITY] = false
            } else {
                density = ndToSum.density!!
            }
            sumQuantity += quantity
            // gradually calculate overall density via weighted sum of densities
            unnormalisedDensity += density * quantity

            for (col in NutritionData.NUTRIENT_COLUMNS) {
                // total has correct data for a field if and only if each component does
                // if the current component has no data for a field, we add nothing to the total,
                // implicitly treating it as zero
                if (!ndToSum.hasCompleteData(col)) {
                    combinedHasData[col] = false
                }
                val colData = ndToSum.amountOf(col, 0.0)
                sumData[col] = sumData[col]!! + colData
            }
        }
        val combinedDataMap = ColumnData(Schema.NutritionDataTable.instance)
        for (col in NutritionData.NUTRIENT_COLUMNS) {
            combinedDataMap.put(col, sumData[col])
        }
        val combinedDensity = unnormalisedDensity / sumQuantity
        if (combineDensities) {
            combinedDataMap.put(Schema.NutritionDataTable.DENSITY, combinedDensity)
        } else {
            combinedDataMap.put(Schema.NutritionDataTable.DENSITY, null)
        }
        combinedDataMap.put(Schema.NutritionDataTable.QUANTITY, sumQuantity)
        combinedDataMap.put(Schema.NutritionDataTable.QUANTITY_UNIT, QtyUnits.GRAMS.abbr)
        combinedDataMap.put(Schema.NutritionDataTable.FOOD_ID, null)
        combinedDataMap.put(Schema.NutritionDataTable.DATA_SOURCE, "Sum")

        // TODO add food if all of the ingredients were the same food?
        return NutritionData(combinedDataMap, ObjectSource.COMPUTED, combinedHasData)
    }


    private fun copyDataForNew(nd: NutritionData): ColumnData<NutritionData> {
        val copy = nd.allData.copy()
        // have to remove ID since it's now a computed value
        copy.put(Schema.NutritionDataTable.ID, MacrosEntity.NO_ID)
        return copy
    }


    // For current nutrition values in this object, per given current quantity,
    // returns a new nData object with the nutrition values rescaled to
    // correspond to the new quantity, in the same unit
    fun rescale(nd: NutritionData, newQuantity: Double): NutritionData {
        val conversionRatio = newQuantity / nd.quantity
        val newData = copyDataForNew(nd)
        for (c in NutritionData.NUTRIENT_COLUMNS) {
            //if (hasCompleteData(c)) {
            if (nd.hasData(c)) {
                // hasData() check avoids NullPointerException
                newData.put(c, nd.amountOf(c)!! * conversionRatio)
            }
        }

        val rescaled = NutritionData.factory.construct(newData, ObjectSource.COMPUTED)
        if (nd.hasFood()) {
            rescaled.food = nd.food
        }
        return rescaled
    }

    // Uses data from the secondary object to fill in missing values from the first
    // Any mismatches are ignored; the primary data is used
    fun combine(primary: NutritionData, secondary: NutritionData): NutritionData {
        // TODO check this logic
        //if (secondary.getQuantity() != primary.getQuantity()) {
        //    secondary = secondary.rescale(primary.getQuantity());
        //}
        val combinedDataMap = copyDataForNew(primary)
        val combinedHasData = HashMap<Column<NutritionData, Double>, Boolean>(NutritionData.NUTRIENT_COLUMNS.size, 1f)

        for (col in NutritionData.NUTRIENT_COLUMNS) {
            // note: hasCompleteData is a stricter condition than hasData:
            // hasCompleteData can be false even if there is a non-null value for that column, when the
            // nData object was produced by summation and there was at least one food with missing data.
            // for this purpose, we'll only replace the primary data if it was null
            if (!primary.hasData(col) && secondary.hasData(col)) {
                combinedDataMap.put(col, secondary.getData(col))
                // !hasData implies !hasCompleteData, so we use the secondary value
                combinedHasData[col] = secondary.hasCompleteData(col)
            } else {
                combinedHasData[col] = primary.hasCompleteData(col)
            }
        }
        combinedDataMap.put(Schema.NutritionDataTable.DATA_SOURCE, "Composite data")
        return NutritionData(combinedDataMap, ObjectSource.COMPUTED, combinedHasData)

    }

    internal fun makeEnergyProportionsMap(nd: NutritionData) : Map<Column<NutritionData, Double>, Double> {
        // preserve iteration order
        val proportionMap = LinkedHashMap<Column<NutritionData, Double>, Double>()
        // energy from...
        val protein = nd.amountOf(Schema.NutritionDataTable.PROTEIN, 0.0) * NutritionData.CALS_PER_G_PROTEIN
        var fat = nd.amountOf(Schema.NutritionDataTable.FAT, 0.0) * NutritionData.CALS_PER_G_FAT
        var carb = nd.amountOf(Schema.NutritionDataTable.CARBOHYDRATE, 0.0) * NutritionData.CALS_PER_G_CARBOHYDRATE
        val sugar = nd.amountOf(Schema.NutritionDataTable.SUGAR, 0.0) * NutritionData.CALS_PER_G_CARBOHYDRATE
        val fibre = nd.amountOf(Schema.NutritionDataTable.FIBRE, 0.0) * NutritionData.CALS_PER_G_FIBRE
        val satFat = nd.amountOf(Schema.NutritionDataTable.SATURATED_FAT, 0.0) * NutritionData.CALS_PER_G_FAT
        // correct subtypes (sugar is part of carbs, saturated is part of fat)
        carb = (carb - sugar).coerceAtLeast(0.0)
        fat = (fat - satFat).coerceAtLeast(0.0)
        // if total energy is missing, fallback to summing over previous energy quantities
        val totalEnergy = nd.amountOf(Schema.NutritionDataTable.CALORIES, protein + fat + satFat + carb + sugar + fibre)
        if (totalEnergy > 0) {
            proportionMap[Schema.NutritionDataTable.PROTEIN] = protein / totalEnergy
            proportionMap[Schema.NutritionDataTable.FAT] = fat / totalEnergy
            proportionMap[Schema.NutritionDataTable.SATURATED_FAT] = satFat / totalEnergy
            proportionMap[Schema.NutritionDataTable.CARBOHYDRATE] = carb / totalEnergy
            proportionMap[Schema.NutritionDataTable.SUGAR] = sugar / totalEnergy
            proportionMap[Schema.NutritionDataTable.FIBRE] = fibre / totalEnergy
        } else {
            proportionMap[Schema.NutritionDataTable.PROTEIN] = 0.0
            proportionMap[Schema.NutritionDataTable.FAT] = 0.0
            proportionMap[Schema.NutritionDataTable.SATURATED_FAT] = 0.0
            proportionMap[Schema.NutritionDataTable.CARBOHYDRATE] = 0.0
            proportionMap[Schema.NutritionDataTable.SUGAR] = 0.0
            proportionMap[Schema.NutritionDataTable.FIBRE] = 0.0
        }

        return proportionMap
    }
}