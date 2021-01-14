package com.machfour.macros.core

import com.machfour.macros.core.MacrosEntity.Companion.cloneWithoutMetadata
import com.machfour.macros.objects.Nutrient
import com.machfour.macros.objects.FoodNutrientValue
import com.machfour.macros.objects.Unit
import com.machfour.macros.objects.UnitType
import com.machfour.macros.objects.inbuilt.DefaultUnits
import com.machfour.macros.objects.inbuilt.Nutrients
import com.machfour.macros.objects.inbuilt.Units

// immutable class storing nutrition data for a food or meal

class NutrientData(
    val dataCompleteIfNotNull: Boolean = true
) {
    companion object {
        private const val KJ_PER_G_PROTEIN = 17.0
        private const val KJ_PER_G_FAT = 37.0
        private const val KJ_PER_G_CARBOHYDRATE = 17.0
        private const val KJ_PER_G_FIBRE = 8.27

        private const val CAL_TO_KJ_FACTOR = 4.186

        private val Nutrient.index : Int
            get() = id.toInt()


        val dummyQuantity by lazy {
            FoodNutrientValue.makeComputedValue(0.0, Nutrients.QUANTITY, Units.GRAMS)
        }

        val energyProportionNutrients by lazy {
            setOf(
                Nutrients.PROTEIN,
                Nutrients.FAT,
                Nutrients.SATURATED_FAT,
                Nutrients.MONOUNSATURATED_FAT,
                Nutrients.POLYUNSATURATED_FAT,
                Nutrients.CARBOHYDRATE,
                Nutrients.SUGAR,
                Nutrients.STARCH,
                Nutrients.FIBRE
            )
        }

        val totalEnergyNutrients by lazy {
            listOf(Nutrients.PROTEIN, Nutrients.FAT, Nutrients.CARBOHYDRATE, Nutrients.FIBRE)
        }

        // Sums each nutrition component
        // converts to default unit for each nutrient
        // Quantity is converted to mass using density provided, or using a default guess of 1
        fun sum(items: List<NutrientData>, densities: List<Double>? = null) : NutrientData {
            val sumData = NutrientData(dataCompleteIfNotNull = false)

            // treat quantity first
            var sumQuantity = 0.0
            var densityGuessed = false
            //var unnormalisedDensity = 0.0
            for ((index, data) in items.withIndex()) {
                val qtyObject = data.quantityObj
                val qtyUnit = qtyObject.unit
                val quantity = when (qtyUnit.type) {
                    UnitType.MASS -> {
                        qtyObject.convertValueTo(Units.GRAMS)
                    }
                    UnitType.VOLUME -> {
                        val density = when (densities != null) {
                            true -> densities[index]
                            false -> {
                                densityGuessed = true
                                1.0
                            }
                        }
                        qtyObject.convertValueTo(Units.GRAMS, density)
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

            sumData.quantityObj = FoodNutrientValue.makeComputedValue(sumQuantity, Nutrients.QUANTITY, Units.GRAMS)
            sumData.markCompleteData(Nutrients.QUANTITY, !densityGuessed)

            for (n in Nutrients.nutrientsExceptQuantity) {
                var completeData = true
                var existsData = false
                var sumValue = 0.0
                val unit = DefaultUnits.get(n)
                for (data in items) {
                    data[n]?.let {
                        sumValue += it.convertValueTo(unit)
                        existsData = true
                    }
                    if (!data.hasCompleteData(n)) {
                        completeData = false
                    }
                }

                if (existsData) {
                    sumData[n] = FoodNutrientValue.makeComputedValue(sumValue, n, unit)
                    sumData.markCompleteData(n, completeData)
                }

            }
            return sumData
        }


    }

    // energy from each individual macronutrient
    private fun makeEnergyComponentsMap(unit: Unit): Map<Nutrient, Double> {
        // preserve iteration order
        val componentMap = LinkedHashMap<Nutrient, Double>()

        require(unit.type == UnitType.ENERGY) { "Invalid energy unit" }

        // have to divide by kJ/calories ratio if desired unit is calories
        val unitDivisor = unit.metricEquivalent

        val g = Units.GRAMS
        // energy from...
        val satFat = amountOf(Nutrients.SATURATED_FAT, g, 0.0) * KJ_PER_G_FAT / unitDivisor
        val monoFat = amountOf(Nutrients.MONOUNSATURATED_FAT, g, 0.0) * KJ_PER_G_FAT / unitDivisor
        val polyFat = amountOf(Nutrients.POLYUNSATURATED_FAT, g, 0.0) * KJ_PER_G_FAT / unitDivisor
        val sugar = amountOf(Nutrients.SUGAR, g, 0.0) * KJ_PER_G_CARBOHYDRATE / unitDivisor
        val starch = amountOf(Nutrients.STARCH, g, 0.0) * KJ_PER_G_CARBOHYDRATE / unitDivisor

        val protein = amountOf(Nutrients.PROTEIN, g, 0.0) * KJ_PER_G_PROTEIN / unitDivisor
        val fibre = amountOf(Nutrients.FIBRE, g, 0.0) * KJ_PER_G_FIBRE / unitDivisor

        // fat must be >= sat + mono + poly
        val fat = (amountOf(Nutrients.FAT, g, 0.0) * KJ_PER_G_FAT / unitDivisor)
            .coerceAtLeast(satFat + monoFat + polyFat)
        // correct subtypes: carbs must be >= sugar + starch
        val carb = (amountOf(Nutrients.CARBOHYDRATE, g, 0.0) * KJ_PER_G_CARBOHYDRATE / unitDivisor)
            .coerceAtLeast(sugar + starch)

        componentMap[Nutrients.PROTEIN] = protein
        componentMap[Nutrients.FAT] = fat
        componentMap[Nutrients.SATURATED_FAT] = satFat
        componentMap[Nutrients.MONOUNSATURATED_FAT] = monoFat
        componentMap[Nutrients.POLYUNSATURATED_FAT] = polyFat
        componentMap[Nutrients.CARBOHYDRATE] = carb
        componentMap[Nutrients.SUGAR] = sugar
        componentMap[Nutrients.STARCH] = starch
        componentMap[Nutrients.FIBRE] = fibre

        return componentMap
    }


    private fun makeEnergyProportionsMap() : Map<Nutrient, Double> {
        // shouldn't matter whether we use KJ or calories here, as long as the amountOf() call below
        // uses the same unit
        val componentMap = makeEnergyComponentsMap(Units.KILOJOULES)

        // XXX DECISION: ignore the actual energy value of the nutritionData, just use the sum
        val totalEnergy = totalEnergyNutrients.fold(0.0, { s, n -> s + componentMap.getValue(n) })

        // previously: use total energy is missing, falling back to sum of energy components
        //val totalEnergy = nd.amountOf(ENERGY, unit, componentMap.values.sum())

        return if (totalEnergy > 0) {
            componentMap.mapValues { it.value/totalEnergy }
        } else {
            componentMap.mapValues { 0.0 }
        }
    }

    private val data: Array<FoodNutrientValue?> = arrayOfNulls(Nutrients.numNutrients)
    private val isDataComplete: Array<Boolean> = Array(Nutrients.numNutrients) { false }

    // TODO should this be passed in explicitly to show dependence?
    var isImmutable: Boolean = false
        private set
    fun setImmutable() {
        this.isImmutable = true
    }

    // using null coalescing means that hasData(QUANTITY) will still return false
    var quantityObj: FoodNutrientValue
        get() = this[Nutrients.QUANTITY] ?: dummyQuantity
        set(value) {
            this[Nutrients.QUANTITY] = value
        }

    val qtyUnit: Unit
        get() = quantityObj.unit

    val quantity: Double
        get() = quantityObj.value

    val qtyUnitAbbr: String
        get() = quantityObj.unit.abbr

    val nutrientValues: List<FoodNutrientValue>
        get() = data.filterNotNull()

    val nutrientValuesExcludingQuantity: List<FoodNutrientValue>
        get() = nutrientValues.filter { it !== quantityObj }

    // map of protein, fat, saturated fat, carbs, sugar, fibre to proportion of total energy
    private val energyProportionsMap: Map<Nutrient, Double> by lazy {
        makeEnergyProportionsMap()
    }
    // map of protein, fat, saturated fat, carbs, sugar, fibre to amount of energy
    private val energyComponentsMap: Map<Nutrient, Double> by lazy {
        makeEnergyComponentsMap(Units.CALORIES)
    }


    override fun equals(other: Any?): Boolean {
        return (other as? NutrientData)?.data?.contentDeepEquals(data) ?: false
    }

    override fun hashCode(): Int = data.contentHashCode()

    override fun toString(): String {
        val str = StringBuilder("NutrientData [")
        for (n in Nutrients.nutrients) {
            str.append("$n : ${get(n)}, ")
        }
        str.append("]")
        return str.toString()
    }

    // creates a mutable copy
    fun copy() : NutrientData {
        return NutrientData(dataCompleteIfNotNull).also { copy ->
            for (i in data.indices) {
                copy.data[i] = data[i]
                copy.isDataComplete[i] = isDataComplete[i]
            }
        }
    }

    fun clear() {
        for (i in data.indices) {
            data[i] = null
            isDataComplete[i] = false
        }
    }

    private fun assertMutable() {
        assert(!isImmutable) { "NutrientData has been made immutable" }
    }
    operator fun get(n: Nutrient): FoodNutrientValue? = data[n.index]

    fun amountOf(n: Nutrient, unit: Unit? = null): Double? {
        return if (unit != null) {
            require(n.isConvertibleTo(unit)) { "Cannot convert nutrient $n to $unit" }
            this[n]?.convertValueTo(unit)
        } else {
            this[n]?.value
        }
    }

    fun amountOf(n: Nutrient, unit: Unit? = null, defaultValue: Double): Double {
        return amountOf(n, unit) ?: defaultValue
    }

    fun hasNutrient(n: Nutrient) : Boolean {
        return this[n] != null
    }

    operator fun set(n: Nutrient, value: FoodNutrientValue?) {
        assertMutable()
        data[n.index] = value
        if (dataCompleteIfNotNull) {
            isDataComplete[n.index] = value != null
        }
    }

    fun hasCompleteData(n: Nutrient) = isDataComplete[n.index]

    internal fun markCompleteData(n: Nutrient, complete: Boolean) {
        isDataComplete[n.index] = complete
    }


    fun getUnitOrDefault(n: Nutrient) : Unit {
        return this[n]?.unit ?: DefaultUnits.get(n)
    }


    // hack for USDA foods
    // subtracts fibre from carbohydrate if necessary to produce a carbohydrate amount
    // If fibre is not present, returns just carbs by diff
    // If there is not enough data to do that, return 0.
    private val carbsBestEffort: Double
        get() = if (hasCompleteData(Nutrients.CARBOHYDRATE)) {
            amountOf(Nutrients.CARBOHYDRATE)!!
        } else if (hasCompleteData(Nutrients.CARBOHYDRATE_BY_DIFF) && hasCompleteData(Nutrients.FIBRE)) {
            amountOf(Nutrients.CARBOHYDRATE_BY_DIFF)!! - amountOf(Nutrients.FIBRE)!!
        } else if (hasCompleteData(Nutrients.CARBOHYDRATE_BY_DIFF)) {
            amountOf(Nutrients.CARBOHYDRATE_BY_DIFF)!!
        } else {
            0.0
        }

    val proteinEnergyProportion : Double
        get() = getEnergyProportion(Nutrients.PROTEIN)

    val carbsEnergyProportion : Double
        get() = getEnergyProportion(Nutrients.CARBOHYDRATE)

    val sugarEnergyProportion : Double
        get() = getEnergyProportion(Nutrients.SUGAR)

    val fatsEnergyProportion : Double
        get() = getEnergyProportion(Nutrients.FAT)

    val saturatedFatsEnergyProportion : Double
        get() = getEnergyProportion(Nutrients.SATURATED_FAT)

    val fibreEnergyProportion: Double
        get() = getEnergyProportion(Nutrients.FIBRE)

    fun getEnergyProportion(n: Nutrient) : Double {
        if (!energyProportionNutrients.contains(n)) {
            return 0.0
        }
        // the map is computed the first time this function is called
        return energyProportionsMap[n] ?: 0.0
    }

    private fun getEnergyComponent(n: Nutrient, unit: Unit = Units.CALORIES) : Double {
        if (!energyProportionNutrients.contains(n)) {
            return 0.0
        }

        require (unit.type == UnitType.ENERGY) { "Invalid energy unit" }

        // the map is computed the first time this function is called
        return (energyComponentsMap[n] ?: 0.0) * unit.metricEquivalent
    }

    // calculations

    fun rescale100() : NutrientData = rescale(100.0)

    fun rescale(newQuantity: Double) : NutrientData {
        val conversionRatio = newQuantity / quantityObj.value

        val newData = NutrientData(dataCompleteIfNotNull = true)
        // completeData is false by default so we can just skip the iteration for null nutrients
        for (n in Nutrients.nutrients) {
            this[n]?.let {
                newData[n] = it.rescale(conversionRatio)
            }
        }
        return newData
    }


    fun getEnergyAs(unit: Unit) : Double? {
        require(unit.type === UnitType.ENERGY) { "Unit $unit is not an energy unit "}
        return this[Nutrients.ENERGY]?.convertValueTo(unit)
    }

    /* ** OLD comment kept here for historical purposes **
     *
     * WONTFIX fundamental problem with unit conversion
     * In the database, nutrient quantities are always considered by-weight, while quantities
     * of a food (or serving, FoodPortionTable, etc.) can be either by-weight or by volume.
     * Converting from a by-weight quantity unit to a by-volume one, or vice-versa, for a
     * NutrientData object, then, must keep the actual (gram) values of the data the same,
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

    // mutates the NutrientData
    fun withQuantityUnit(newUnit: Unit, density: Double? = null, allowDefaultDensity: Boolean = false) : NutrientData {
        val densityConversionNeeded = qtyUnit.type !== newUnit.type
        if (!allowDefaultDensity) {
            assert (!(densityConversionNeeded && density == null)) {
                "Quantity unit conversion required but no density given."
            }
        }
        val fallbackDensity = (if (allowDefaultDensity) 1.0 else null)

        return copy().also {
            it.quantityObj = quantityObj.convert(newUnit, density ?: fallbackDensity)
            it.markCompleteData(Nutrients.QUANTITY, densityConversionNeeded && density == null)
        }
    }

    fun withDefaultUnits(includingQuantity: Boolean = false, density: Double? = null) : NutrientData {
        val convertedData = if (includingQuantity) {
            withQuantityUnit(DefaultUnits.get(Nutrients.QUANTITY), density, false)
        } else {
            copy()
        }
        for (nv in nutrientValuesExcludingQuantity) {
            val n = nv.nutrient
            convertedData[n] = nv.convert(DefaultUnits.get(n))
            convertedData.markCompleteData(n, hasCompleteData(n))
        }
        return convertedData
    }

    // Use data from the another NutrientObject object to complete missing values from this one
    // Any mismatches are ignored; this object's data is preferred in all cases
    // Nothing is mutated; a new NutrientData object is returned with data copies
    fun fillMissingData(other: NutrientData): NutrientData {
        //assert(one.nutrients == other.nutrients) { "Mismatch in nutrients"}
        val result = NutrientData(dataCompleteIfNotNull = false)

        val factory = FoodNutrientValue.factory

        for (n in Nutrients.nutrients) {
            // note: hasCompleteData is a stricter condition than hasData:
            // hasCompleteData can be false even if there is a non-null value for that column, when the
            // nData object was produced by summation and there was at least one food with missing data.
            // for this purpose, we'll only replace the primary data if it was null

            val thisValue = this[n]
            val otherValue = other[n]

            val resultValue = (thisValue?: otherValue)?.let { factory.cloneWithoutMetadata(it) }
            val resultIsDataComplete = if (thisValue != null) this.hasCompleteData(n) else other.hasCompleteData(n)

            result[n] = resultValue
            result.markCompleteData(n, resultIsDataComplete)
        }
        return result
    }
}



