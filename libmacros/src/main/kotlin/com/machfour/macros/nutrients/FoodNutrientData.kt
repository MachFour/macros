@file:Suppress("EqualsOrHashCode")

package com.machfour.macros.nutrients

import com.machfour.macros.core.MacrosEntity.Companion.cloneWithoutMetadata
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.Unit
import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.LegacyNutrientUnits
import com.machfour.macros.units.NutrientUnits
import com.machfour.macros.units.UnitType

// class storing nutrition data for a food or meal

class FoodNutrientData(
    dataCompleteIfNotNull: Boolean = true,
    private val density: Double? = null,
): GenericNutrientData<FoodNutrientValue>(dataCompleteIfNotNull) {
    companion object {
        // lazy because otherwise having it here messes up static initialisation
        val dummyQuantity by lazy {
            FoodNutrientValue.makeComputedValue(0.0, QUANTITY, GRAMS)
        }

        // Sums each nutrition component
        // converts to default unit for each nutrient
        // Quantity is converted to mass using density provided, or using a default guess of 1
        fun sum(items: List<NutrientData>) : FoodNutrientData {
            val sumData = FoodNutrientData(dataCompleteIfNotNull = false)

            // treat quantity first
            var sumQuantity = 0.0
            var densityGuessed = false
            //var unnormalisedDensity = 0.0
            for (data in items) {
                val qtyUnit = data.getUnit(QUANTITY)
                val quantity = if (qtyUnit != null && qtyUnit.type == UnitType.VOLUME) {
                    val volumeQty = data.amountOf(QUANTITY, qtyUnit, 0.0)
                    val density = data.foodDensity ?: run {
                        densityGuessed = true
                        1.0
                    }
                    convertUnit(QUANTITY, volumeQty, qtyUnit, GRAMS, density)
                } else {
                    data.amountOf(QUANTITY, GRAMS, 0.0)
                }
                sumQuantity += quantity
                // gradually calculate overall density via weighted sum of densities
                //unnormalisedDensity += density * quantity
            }

            sumData.quantityObjInternal = FoodNutrientValue.makeComputedValue(
                sumQuantity,
                QUANTITY,
                GRAMS
            )
            sumData.markCompleteData(QUANTITY, !densityGuessed)

            for (n in AllNutrientsExceptQuantity) {
                var completeData = true
                var existsData = false
                var sumValue = 0.0
                val unit = LegacyNutrientUnits[n]
                for (data in items) {
                    data.amountOf(n, unit)?.let {
                        sumValue += it
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
    val quantityObj: FoodNutrientValue
        get() = quantityObjInternal

    // using null coalescing means that hasData(QUANTITY) will still return false
    private var quantityObjInternal: FoodNutrientValue
        get() = this[QUANTITY] ?: dummyQuantity
        private set(value) {
            this[QUANTITY] = value
        }

    val qtyUnit: Unit
        get() = quantityObj.unit

    val quantity: Double
        get() = quantityObj.value

    val qtyUnitAbbr: String
        get() = quantityObj.unit.abbr


    override val foodDensity: Double?
        get() = density

    override fun equals(other: Any?): Boolean {
        return (other as? FoodNutrientData)?.data?.equals(data) ?: false
    }

    override fun toString(): String {
        val str = StringBuilder("NutrientData [")
        for (n in AllNutrients) {
            str.append("$n : ${get(n)}, ")
        }
        str.append("]")
        return str.toString()
    }

    // creates a mutable copy
    override fun copy() : FoodNutrientData {
        return FoodNutrientData(dataCompleteIfNotNull, density).also { copy ->
            for (i in data.indices) {
                copy.data[i] = data[i]
                copy.isDataComplete[i] = isDataComplete[i]
            }
        }
    }

    // calculations

    fun rescale100() : FoodNutrientData = rescale(100.0)

    fun rescale(newQuantity: Double) : FoodNutrientData {
        val conversionRatio = newQuantity / quantityObj.value

        val newData = FoodNutrientData(dataCompleteIfNotNull = true, density = density)
        // completeData is false by default so we can just skip the iteration for null nutrients
        for (n in AllNutrients) {
            this[n]?.let {
                newData[n] = it.rescale(conversionRatio)
            }
        }
        return newData
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

    fun withQuantityUnit(newUnit: Unit, density: Double? = null, allowDefaultDensity: Boolean = false) : FoodNutrientData {
        val densityConversionNeeded = qtyUnit.type !== newUnit.type
        if (!allowDefaultDensity) {
            assert (!(densityConversionNeeded && density == null)) {
                "Quantity unit conversion required but no density given."
            }
        }
        val fallbackDensity = (if (allowDefaultDensity) 1.0 else null)

        return copy().also {
            it.quantityObjInternal = quantityObj.convert(newUnit, density ?: fallbackDensity)
            it.markCompleteData(QUANTITY, densityConversionNeeded && density == null)
        }
    }

    fun withDefaultUnits(
        defaultUnits: NutrientUnits = LegacyNutrientUnits,
        includingQuantity: Boolean = false,
        density: Double? = null
    ) : FoodNutrientData {
        val convertedData = if (includingQuantity) {
            withQuantityUnit(defaultUnits[QUANTITY], density, false)
        } else {
            copy()
        }
        for (nv in valuesExcludingQuantity) {
            val n = nv.nutrient
            convertedData[n] = nv.convert(defaultUnits[n])
            convertedData.markCompleteData(n, hasCompleteData(n))
        }
        return convertedData
    }

    // Use data from the another NutrientObject object to complete missing values from this one
    // Any mismatches are ignored; this object's data is preferred in all cases
    // Nothing is mutated; a new NutrientData object is returned with data copies
    fun fillMissingData(other: FoodNutrientData): FoodNutrientData {
        //check(one.nutrients == other.nutrients) { "Mismatch in nutrients"}
        val result = FoodNutrientData(dataCompleteIfNotNull = false)

        val factory = FoodNutrientValue.factory

        for (n in AllNutrients) {
            // note: hasCompleteData is a stricter condition than hasData:
            // hasCompleteData can be false even if there is a non-null value for that column, when the
            // nData object was produced by summation and there was at least one food with missing data.
            // for this purpose, we'll only replace the primary data if it was null

            val thisValue = this[n]
            val otherValue = other[n]

            val resultValue = (thisValue ?: otherValue)?.let { factory.cloneWithoutMetadata(it) }
            val resultIsDataComplete =
                if (thisValue != null) this.hasCompleteData(n) else other.hasCompleteData(n)

            result[n] = resultValue
            result.markCompleteData(n, resultIsDataComplete)
        }
        return result
    }

}



