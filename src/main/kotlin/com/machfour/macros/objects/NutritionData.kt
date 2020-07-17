package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.core.datatype.Types

import com.machfour.macros.core.Schema.NutritionDataTable.Companion.ALCOHOL
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.CALCIUM
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.CALORIES
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.CARBOHYDRATE
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.CARBOHYDRATE_BY_DIFF
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.DATA_SOURCE
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.DENSITY
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.FAT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.FIBRE
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.FOOD_ID
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.ID
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.IRON
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.KILOJOULES
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.MONOUNSATURATED_FAT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.OMEGA_3_FAT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.OMEGA_6_FAT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.POLYUNSATURATED_FAT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.POTASSIUM
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.PROTEIN
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.QUANTITY
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.QUANTITY_UNIT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.SALT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.SATURATED_FAT
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.SODIUM
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.STARCH
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.SUGAR
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.SUGAR_ALCOHOL
import com.machfour.macros.core.Schema.NutritionDataTable.Companion.WATER

// immutable class storing nutrition data for a food or meal
class NutritionData private constructor(dataMap: ColumnData<NutritionData>, objectSource: ObjectSource) : MacrosEntityImpl<NutritionData>(dataMap, objectSource) {

    /*
    private static final Set<Column<NutritionData, Double>> MILLIGRAMS_COLS = new HashSet<>(
            Arrays.asList(SODIUM, CALCIUM, POTASSIUM, IRON, OMEGA_3_FAT, OMEGA_6_FAT));
     */

    // keeps track of missing data for adding different instances of NutritionDataTable together
    // only NUTRIENT_COLUMNS are present in this map
    private val completeData: MutableMap<Column<NutritionData, Double>, Boolean>

    // measured in grams, per specified quantity
    val qtyUnit: QtyUnit
    var food: Food? = null
        set(f) {
            assert(this.food == null && f != null && foreignKeyMatches(this, FOOD_ID, f))
            field = f
        }
    override val factory: Factory<NutritionData>
        get() = factory()

    val foodId: Long
        get() = getData(FOOD_ID)!!

    override val table: Table<NutritionData>
        get() = Schema.NutritionDataTable.instance

    val dataSource: String?
        get() = getData(DATA_SOURCE)

    // hack for USDA foods
    // subtracts fibre from carbohydrate if necessary to produce a carbohydrate amount
    // If fibre is not present, returns just carbs by diff
    // If there is not enough data to do that, return 0.
    private val carbsBestEffort: Double
        get() = if (hasCompleteData(CARBOHYDRATE)) {
            amountOf(CARBOHYDRATE)!!
        } else if (hasCompleteData(CARBOHYDRATE_BY_DIFF) && hasCompleteData(FIBRE)) {
            amountOf(CARBOHYDRATE_BY_DIFF)!! - amountOf(FIBRE)!!
        } else if (hasCompleteData(CARBOHYDRATE_BY_DIFF)) {
            amountOf(CARBOHYDRATE_BY_DIFF)!!
        } else {
            0.0
        }


    val density: Double?
        get() = getData(DENSITY)

    val quantity: Double
        get() = getData(QUANTITY)!!

    init {
        // food ID is allowed to be null only if this NutritionData is computed from a sum
        assert(objectSource === ObjectSource.COMPUTED || dataMap[FOOD_ID] != null)
        completeData = HashMap(NUTRIENT_COLUMNS.size)
        // have to use temporary due to type parameterisation
        for (c in NUTRIENT_COLUMNS) {
            completeData[c] = dataMap.hasData(c)
        }
        // account for energy conversion
        val hasEnergy = completeData[CALORIES]!! || completeData[KILOJOULES]!!
        completeData[CALORIES] = hasEnergy
        completeData[KILOJOULES] = hasEnergy
        qtyUnit = QtyUnits.fromAbbreviation(dataMap[QUANTITY_UNIT]!!)
    }

    // allows keeping track of missing data when different nData instances are added up
    private constructor(dataMap: ColumnData<NutritionData>, objectSource: ObjectSource,
                        completeData: Map<Column<NutritionData, Double>, Boolean>) : this(dataMap, objectSource) {
        for (c in NUTRIENT_COLUMNS) {
            if (!completeData.getValue(c)) {
                this.completeData[c] = false
            } else {
                // we shouldn't need to correct this case, since if the thing is null (and so makeHasDataMap
                // said it was not present), then we shouldn't be correcting that to say that it is present!
                assert(this.completeData.getValue(c))
            }
        }
    }

    fun qtyUnitAbbr(): String {
        return getData(QUANTITY_UNIT)!!
    }

    private fun getEnergyAs(energyCol: Column<NutritionData, Double>): Double? {
        assert(ENERGY_COLS.contains(energyCol))
        val toValue = getData(energyCol)
        if (toValue != null) {
            return toValue
        } else if (energyCol == CALORIES) {
            val kjData = getData(KILOJOULES)
            return if (kjData != null) kjData / CAL_TO_KJ_FACTOR else null
        } else { // energyCol.equals(KILOJOULES)
            val calData = getData(CALORIES)
            return if (calData != null) calData * CAL_TO_KJ_FACTOR else null
        }
    }

    fun hasCompleteData(col: Column<NutritionData, Double>): Boolean {
        assert(completeData.containsKey(col))
        return completeData.getValue(col)
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
    private fun convertQuantityUnit(targetUnit: QtyUnit, density: Double, isDensityGuessed: Boolean): NutritionData {
        val currentUnit = qtyUnit
        if (currentUnit == targetUnit) {
            // TODO should it be a copy?
            return this
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
        val newQuantity = quantity * ratio
        val newData = copyDataForNew()
        newData.put(QUANTITY, newQuantity)
        newData.put(QUANTITY_UNIT, targetUnit.abbr)
        newData.put(DENSITY, density)
        // all other data remains the same
        val newHasData = HashMap(completeData)
        if (isDensityGuessed) {
            newHasData[QUANTITY] = false
        }
        val converted = NutritionData(newData, ObjectSource.COMPUTED, newHasData)
        if (hasFood()) {
            converted.food = food
        }
        return converted
    }

    private fun copyDataForNew(): ColumnData<NutritionData> {
        val copy = allData.copy()
        // have to remove ID since it's now a computed value
        copy.put(ID, MacrosEntity.NO_ID)
        return copy
    }

    fun makeEnergyProportionsMap(): Map<Column<NutritionData, Double>, Double> {
        // preserve iteration order
        val proportionMap = LinkedHashMap<Column<NutritionData, Double>, Double>()
        // energy from...
        val protein = amountOf(PROTEIN, 0.0) * CALS_PER_G_PROTEIN
        var fat = amountOf(FAT, 0.0) * CALS_PER_G_FAT
        var carb = amountOf(CARBOHYDRATE, 0.0) * CALS_PER_G_CARBOHYDRATE
        val sugar = amountOf(SUGAR, 0.0) * CALS_PER_G_CARBOHYDRATE
        val fibre = amountOf(FIBRE, 0.0) * CALS_PER_G_FIBRE
        val satFat = amountOf(SATURATED_FAT, 0.0) * CALS_PER_G_FAT
        // correct subtypes (sugar is part of carbs, saturated is part of fat)
        carb = Math.max(carb - sugar, 0.0)
        fat = Math.max(fat - satFat, 0.0)
        // if total energy is missing, fallback to summing over previous energy quantities
        val totalEnergy = amountOf(CALORIES, protein + fat + satFat + carb + sugar + fibre)
        if (totalEnergy > 0) {
            proportionMap[PROTEIN] = protein / totalEnergy * 100
            proportionMap[FAT] = fat / totalEnergy * 100
            proportionMap[SATURATED_FAT] = satFat / totalEnergy * 100
            proportionMap[CARBOHYDRATE] = carb / totalEnergy * 100
            proportionMap[SUGAR] = sugar / totalEnergy * 100
            proportionMap[FIBRE] = fibre / totalEnergy * 100
        } else {
            proportionMap[PROTEIN] = 0.0
            proportionMap[FAT] = 0.0
            proportionMap[SATURATED_FAT] = 0.0
            proportionMap[CARBOHYDRATE] = 0.0
            proportionMap[SUGAR] = 0.0
            proportionMap[FIBRE] = 0.0
        }

        return proportionMap
    }

    private fun convertToGramsIfNecessary(): NutritionData {
        return when (qtyUnit == QtyUnits.GRAMS) {
            true -> this
            else -> {
                // then convert to grams, guessing density if required
                val guessDensity = density == null
                val density = density ?: 1.0
                convertQuantityUnit(QtyUnits.GRAMS, density, guessDensity)
            }
        }
    }

    fun hasFood(): Boolean {
        return food != null
    }

    // For current nutrition values in this object, per given current quantity,
    // returns a new nData object with the nutrition values rescaled to
    // correspond to the new quantity, in the same unit
    fun rescale(newQuantity: Double): NutritionData {
        val conversionRatio = newQuantity / quantity
        val newData = copyDataForNew()
        for (c in NUTRIENT_COLUMNS) {
            //if (hasCompleteData(c)) {
            if (hasData(c)) {
                // hasData() check avoids NullPointerException
                newData.put(c, amountOf(c)!! * conversionRatio)
            }
        }
        val rescaled = NutritionData(newData, ObjectSource.COMPUTED)
        if (hasFood()) {
            rescaled.food = food
        }
        return rescaled
    }


    // For current nutrition values in this object, per given current quantity,
    // returns a new nData object with the nutrition values rescaled to
    // correspond to the new quantity, in the new unit
    fun rescale(newQuantity: Double, newUnit: QtyUnit): NutritionData {
        // default density for rescaling is 1.0
        val guessDensity = density == null
        val density = density ?: 1.0
        val convertedUnit = convertQuantityUnit(newUnit, density, guessDensity)
        return convertedUnit.rescale(newQuantity)
    }

    override fun equals(other: Any?): Boolean {
        return other is NutritionData && super.equals(other)
    }

    fun amountOf(col: Column<NutritionData, Double>): Double? {
        assert(NUTRIENT_COLUMNS.contains(col))
        return if (ENERGY_COLS.contains(col)) {
            // return any energy value, converting if necessary. Return null if neither column.
            getEnergyAs(col)
            // TODO sodium/salt
            // TODO carbs by difference / carbs
        } else {
            getData(col)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun hasData(col: Column<NutritionData, *>): Boolean {
        if (col.type === Types.REAL) {
            // ColumnType of REAL ensures the cast will work
            val doubleCol = col as Column<NutritionData, Double>
            if (NUTRIENT_COLUMNS.contains(doubleCol)) {
                return amountOf(doubleCol) != null
            }
        }
        // fall back to just checking the columnData
        return super.hasData(col)
    }

    fun amountOf(col: Column<NutritionData, Double>, defaultValue: Double): Double {
        val data = amountOf(col)
        return data ?: defaultValue
    }

    companion object {

        private val CAL_TO_KJ_FACTOR = 4.186
        private val CALS_PER_G_PROTEIN = 17 / CAL_TO_KJ_FACTOR
        private val CALS_PER_G_FAT = 37 / CAL_TO_KJ_FACTOR
        private val CALS_PER_G_CARBOHYDRATE = 17 / CAL_TO_KJ_FACTOR
        private val CALS_PER_G_FIBRE = 8 / CAL_TO_KJ_FACTOR

        // measured in the relevant FoodTable's QuantityUnits
        val DEFAULT_QUANTITY = 100.0

        val NUTRIENT_COLUMNS = listOf(
                QUANTITY,
                KILOJOULES,
                CALORIES,
                PROTEIN,
                FAT,
                SATURATED_FAT,
                CARBOHYDRATE,
                SUGAR,
                FIBRE,
                SODIUM,
                CALCIUM,
                POTASSIUM,
                IRON,
                MONOUNSATURATED_FAT,
                POLYUNSATURATED_FAT,
                OMEGA_3_FAT,
                OMEGA_6_FAT,
                CARBOHYDRATE_BY_DIFF,
                WATER,
                STARCH,
                SALT,
                ALCOHOL,
                SUGAR_ALCOHOL
        )

        // For units
        private val ENERGY_COLS = listOf(CALORIES, KILOJOULES)

        fun table(): Table<NutritionData> {
            return Schema.NutritionDataTable.instance
        }

        fun factory(): Factory<NutritionData> {
            return object : Factory<NutritionData> {
                override fun construct(dataMap: ColumnData<NutritionData>, objectSource: ObjectSource): NutritionData {
                    return NutritionData(dataMap, objectSource)
                }
            }
        }

        /* Result is always converted to grams. */
        @JvmOverloads
        fun sum(components: List<NutritionData>, combineDensities: Boolean = false): NutritionData {
            var sumQuantity = 0.0
            var unnormalisedDensity = 0.0 // need to divide by sumQuantity at the end

            val sumData = HashMap<Column<NutritionData, Double>, Double>(NUTRIENT_COLUMNS.size, 1f)
            val combinedHasData = HashMap<Column<NutritionData, Double>, Boolean>(NUTRIENT_COLUMNS.size, 1f)
            for (col in NUTRIENT_COLUMNS) {
                sumData[col] = 0.0
                combinedHasData[col] = true
            }
            for (nd in components) {
                val ndToSum = nd.convertToGramsIfNecessary()
                val quantity = ndToSum.quantity
                var density = 1.0 // default guess
                if (ndToSum.density == null || !ndToSum.hasCompleteData(QUANTITY)) {
                    // means we guessed the density
                    combinedHasData[QUANTITY] = false
                } else {
                    density = ndToSum.density!!
                }
                sumQuantity += quantity
                // gradually calculate overall density via weighted sum of densities
                unnormalisedDensity += density * quantity

                for (col in NUTRIENT_COLUMNS) {
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
            for (col in NUTRIENT_COLUMNS) {
                combinedDataMap.put(col, sumData[col])
            }
            val combinedDensity = unnormalisedDensity / sumQuantity
            if (combineDensities) {
                combinedDataMap.put(DENSITY, combinedDensity)
            } else {
                combinedDataMap.put(DENSITY, null)
            }
            combinedDataMap.put(QUANTITY, sumQuantity)
            combinedDataMap.put(QUANTITY_UNIT, QtyUnits.GRAMS.abbr)
            combinedDataMap.put(FOOD_ID, null)
            combinedDataMap.put(DATA_SOURCE, "Sum")

            // TODO add food if all of the ingredients were the same food?
            return NutritionData(combinedDataMap, ObjectSource.COMPUTED, combinedHasData)
        }

        // Uses data from the secondary object to fill in missing values from the first
        // Any mismatches are ignored; the primary data is used
        fun combine(primary: NutritionData, secondary: NutritionData): NutritionData {
            // TODO check this logic
            //if (secondary.getQuantity() != primary.getQuantity()) {
            //    secondary = secondary.rescale(primary.getQuantity());
            //}
            val combinedDataMap = primary.copyDataForNew()
            val combinedHasData = HashMap<Column<NutritionData, Double>, Boolean>(NUTRIENT_COLUMNS.size, 1f)

            for (col in NUTRIENT_COLUMNS) {
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
            combinedDataMap.put(DATA_SOURCE, "Composite data")
            return NutritionData(combinedDataMap, ObjectSource.COMPUTED, combinedHasData)

        }
    }
}



