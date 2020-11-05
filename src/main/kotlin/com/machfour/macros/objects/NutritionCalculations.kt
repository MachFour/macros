package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.objects.inbuilt.DefaultUnits
import com.machfour.macros.objects.inbuilt.Nutrients
import com.machfour.macros.objects.inbuilt.Nutrients.ENERGY
import com.machfour.macros.objects.inbuilt.Nutrients.QUANTITY
import com.machfour.macros.objects.inbuilt.Units

object NutritionCalculations {

    internal const val KJ_PER_G_PROTEIN = 17.0
    internal const val KJ_PER_G_FAT = 37.0
    internal const val KJ_PER_G_CARBOHYDRATE = 17.0
    internal const val KJ_PER_G_FIBRE = 8.27

    internal const val CAL_TO_KJ_FACTOR = 4.186

    internal fun getEnergyAs(nd: NutrientData, unit: Unit) : Double? {
        require(unit.type === UnitType.ENERGY) { "Unit $unit is not an energy unit "}
        return nd[ENERGY]?.convertValue(unit)
    }

    /* ** OLD comment kept here for historical purposes **
     *
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

    // For current nutrition values in this object, per given current quantity,
    // returns a new nData object with the nutrition values rescaled to
    // correspond to the new quantity, in the new unit

    // mutates the NutrientData
    fun convertQuantityUnit(nd: NutrientData, newUnit: Unit, density: Double? = null, guessDensity: Boolean = false) {
        // TODO do we need to duplicate the NutrientData too?
        nd.quantityObj = nd.quantityObj.convert(newUnit, density)
        nd.markCompleteData(QUANTITY, density == null || guessDensity)
    }

    @Deprecated("Do rescaling and unit conversion separately")
    fun rescale(nd: NutrientData, newQuantity: Double, newUnit: Unit, density: Double? = null): NutrientData {
        convertQuantityUnit(nd, newUnit, density, density == null)
        return rescale(nd, newQuantity)
    }

    @Deprecated("Use NutrientData")
    fun rescale(nd: NutritionData, newQuantity: Double, newUnit: Unit, density: Double? = null): NutritionData {
        convertQuantityUnit(nd.nutrientData, newUnit, density, density == null)
        return NutritionData(rescale(nd.nutrientData, newQuantity))
    }


    @Deprecated("Use NutrientData")
    fun combine(one: NutritionData, other: NutritionData): NutritionData {
        return NutritionData(combine(one.nutrientData, other.nutrientData))

    }

    @Deprecated("Use NutrientData")
    fun sum(items: List<NutritionData>): NutritionData {
        return NutritionData(sum(items.map { it.nutrientData }, null))
    }

    @Deprecated("Use NutrientData")
    fun rescale(data: NutritionData, newQuantity: Double) : NutritionData {
        return NutritionData(rescale(data.nutrientData, newQuantity))
    }

    fun convertToDefaultUnits(data: NutrientData) : NutrientData {
        val convertedData = NutrientData(dataCompleteIfNotNull = true)
        for (nv in data.nutrientValues) {
            val n = nv.nutrient
            convertedData[n] = nv.convert(DefaultUnits.get(n))
            convertedData.markCompleteData(n, data.hasCompleteData(n))
        }
        return convertedData
    }

    fun rescale(data: NutrientData, newQuantity: Double) : NutrientData {
        val conversionRatio = newQuantity / data.quantityObj.value

        val newData = NutrientData(dataCompleteIfNotNull = true)
        // completeData is false by default so we can just skip the iteration for null nutrients
        for (n in Nutrients.nutrients) {
            data[n]?.let {
                newData[n] = it.rescale(conversionRatio)
            }
        }
        return newData
    }

    // Sums each nutrition component
    // converts to default unit for each nutrient
    // Quantity is converted to mass using density provided, or using a default guess of 1
    fun sum(items: List<NutrientData>, densities: List<Double>?) : NutrientData {
        val sumData = NutrientData(dataCompleteIfNotNull = false)

        // treat quantity first
        run {
            var sumQuantity = 0.0
            var densityGuessed = false
            //var unnormalisedDensity = 0.0
            for ((index, data) in items.withIndex()) {
                val qtyObject = data.quantityObj
                val qtyUnit = qtyObject.unit
                val quantity = when (qtyUnit.type) {
                    UnitType.MASS -> {
                        qtyObject.convertValue(Units.GRAMS)
                    }
                    UnitType.VOLUME -> {
                        val density = densities?.get(index) ?: run {
                            densityGuessed = true
                            1.0
                        }
                        qtyObject.convertValue(Units.GRAMS, density)
                    }
                    else -> {
                        assert(false) { "Invalid unit type for quantity value object $qtyObject" }
                        0.0
                    }
                }
                sumQuantity += quantity
                // gradually calculate overall density via weighted sum of densities
                //unnormalisedDensity += density * quantity
            }

            sumData.quantityObj = NutrientValue.makeComputedValue(sumQuantity, QUANTITY, Units.GRAMS)
            sumData.markCompleteData(QUANTITY, !densityGuessed)
        }

        for (n in Nutrients.nutrientsExceptQuantity) {
            var completeData = true
            var existsData = false
            var sumValue = 0.0
            val unit = DefaultUnits.get(n)
            for (data in items) {
                data[n]?.let {
                    sumValue += it.convertValue(unit)
                    existsData = true
                }
                if (!data.hasCompleteData(n)) {
                    completeData = false
                }
            }

            if (existsData) {
                sumData[n] = NutrientValue.makeComputedValue(sumValue, n, unit)
                sumData.markCompleteData(n, completeData)
            }

        }
        return sumData
    }

    // Use data from the another NutrientObject object to complete missing values from this one
    // Any mismatches are ignored; this object's data is preferred in all cases
    // Nothing is mutated; a new NutrientData object is returned with data copies
    fun combine(one: NutrientData, other: NutrientData): NutrientData {
        //assert(one.nutrients == other.nutrients) { "Mismatch in nutrients"}
        val result = NutrientData(dataCompleteIfNotNull = false)

        val createNewNutrientValue: (ColumnData<NutrientValue>) -> NutrientValue = {
            NutrientValue.factory.construct(it, ObjectSource.COMPUTED)
        }

        for (n in Nutrients.nutrients) {
            // note: hasCompleteData is a stricter condition than hasData:
            // hasCompleteData can be false even if there is a non-null value for that column, when the
            // nData object was produced by summation and there was at least one food with missing data.
            // for this purpose, we'll only replace the primary data if it was null

            val thisValue = one[n]
            val otherValue = other[n]

            val resultValue = (thisValue?.dataCopy ?: otherValue?.dataCopy)?.let { createNewNutrientValue(it) }
            val resultIsDataComplete = if (thisValue != null) one.hasCompleteData(n) else other.hasCompleteData(n)

            result[n] = resultValue
            result.markCompleteData(n, resultIsDataComplete)
        }
        return result
    }
    // energy from each individual macronutrient
    internal fun makeEnergyComponentsMap(nd: NutritionData, unit: Unit): Map<Nutrient, Double> {
        // preserve iteration order
        val componentMap = LinkedHashMap<Nutrient, Double>()

        require(unit.type == UnitType.ENERGY) { "Invalid energy unit" }

        // have to divide by kJ/calories ratio if desired unit is calories
        val unitDivisor = unit.metricEquivalent

        val g = Units.GRAMS
        // energy from...
        val protein = nd.amountOf(Nutrients.PROTEIN, g, 0.0) * KJ_PER_G_PROTEIN / unitDivisor
        var fat = nd.amountOf(Nutrients.FAT, g, 0.0) * KJ_PER_G_FAT / unitDivisor
        val satFat = nd.amountOf(Nutrients.SATURATED_FAT, g, 0.0) * KJ_PER_G_FAT / unitDivisor
        var carb = nd.amountOf(Nutrients.CARBOHYDRATE, g, 0.0) * KJ_PER_G_CARBOHYDRATE / unitDivisor
        val sugar = nd.amountOf(Nutrients.SUGAR, g, 0.0) * KJ_PER_G_CARBOHYDRATE / unitDivisor
        val fibre = nd.amountOf(Nutrients.FIBRE, g, 0.0) * KJ_PER_G_FIBRE / unitDivisor
        // correct subtypes (sugar is part of carbs, saturated is part of fat)
        carb = (carb - sugar).coerceAtLeast(0.0)
        fat = (fat - satFat).coerceAtLeast(0.0)

        componentMap[Nutrients.PROTEIN] = protein
        componentMap[Nutrients.FAT] = fat
        componentMap[Nutrients.SATURATED_FAT] = satFat
        componentMap[Nutrients.CARBOHYDRATE] = carb
        componentMap[Nutrients.SUGAR] = sugar
        componentMap[Nutrients.FIBRE] = fibre
        
        return componentMap
    }

    internal fun makeEnergyProportionsMap(nd: NutritionData) : Map<Nutrient, Double> {
        // shouldn't matter whether we use KJ or calories here, as long as the amountOf() call below
        // uses the same unit
        val componentMap = makeEnergyComponentsMap(nd, Units.KILOJOULES)

        // if total energy is missing, fallback to summing over previous energy quantities
        //val totalEnergy = nd.amountOf(ENERGY, unit, componentMap.values.sum())

        // XXX DECISION: ignore the actual energy value of the nutritionData, just use the sum
        val totalEnergy = componentMap.values.sum()

        return if (totalEnergy > 0) {
            componentMap.mapValues { it.value/totalEnergy }
        } else {
            componentMap.mapValues { 0.0 }
        }
    }
}
