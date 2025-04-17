@file:Suppress("EqualsOrHashCode")

package com.machfour.macros.nutrients

import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.FoodNutrientValue.Companion.makeComputedValue
import com.machfour.macros.entities.Unit
import com.machfour.macros.nutrients.Quantity.Companion.NullQuantity
import com.machfour.macros.nutrients.Quantity.Companion.toQuantity
import com.machfour.macros.sql.rowdata.removeMetadata
import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.LegacyNutrientUnits
import com.machfour.macros.units.NutrientUnits
import com.machfour.macros.units.UnitType

// class storing nutrition data for a food or meal

// TODO make immutable
class FoodNutrientData(
    private val density: Double? = null,
): GenericNutrientData<FoodNutrientValue>(), NutrientData<FoodNutrientValue> {
    companion object {
        // Sums each nutrition component
        // converts to default unit for each nutrient
        // Quantity is converted to mass using density provided, or using a default guess of 1
        fun sum(items: List<BasicNutrientData<*>>): FoodNutrientData {
            val sumData = FoodNutrientData()

            // treat quantity first
            var totalAmountGrams = 0.0
            var densityGuessed = false
            //var unnormalisedDensity = 0.0
            for (data in items) {
                val quantity = data.perQuantity
                val amountGrams = when (quantity.unit.type == UnitType.MASS) {
                    true -> quantity.amount
                    else -> {
                        val density = data.foodDensity ?: 1.0
                        densityGuessed = (densityGuessed || data.foodDensity == null)
                        convertQuantity(quantity.amount, quantity.unit, GRAMS, density)
                    }
                }
                totalAmountGrams += amountGrams
                // gradually calculate overall density via weighted sum of densities
                //unnormalisedDensity += density * quantity
            }

            sumData.perQuantityInternal = Quantity(amount = totalAmountGrams, unit = GRAMS)
            sumData.markIncompleteData(QUANTITY, densityGuessed)

            for (n in AllNutrientsExceptQuantity) {
                var incompleteData = false
                var existsData = false
                var sumValue = 0.0
                val unit = LegacyNutrientUnits[n]
                for (data in items) {
                    data.amountOf(n, unit)?.let {
                        sumValue += it
                        existsData = true
                    }
                    if (data.hasIncompleteData(n)) {
                        incompleteData = true
                    }
                }

                if (existsData) {
                    sumData[n] = makeComputedValue(sumValue, n, unit)
                    sumData.markIncompleteData(n, incompleteData)
                }


            }
            return sumData
        }
    }
    override val perQuantity: Quantity
        get() = perQuantityInternal.toQuantity()

    // using null coalescing means that hasData(QUANTITY) will still return false
    private var perQuantityInternal: Quantity
        get() = this[QUANTITY]?.toQuantity() ?: NullQuantity
        private set(value) {
            this[QUANTITY] = makeComputedValue(value.amount, QUANTITY, value.unit)
        }

    val qtyUnit: Unit
        get() = perQuantity.unit

    val qtyAmount: Double
        get() = perQuantity.amount

    val qtyUnitAbbr: String
        get() = perQuantity.unit.abbr

    override val foodDensity: Double?
        get() = density

    override fun equals(other: Any?): Boolean {
        return (other as? FoodNutrientData)?.data?.equals(data) ?: false
    }

    override fun toString(): String {
        return nutrientDataToString(this)
    }

    // creates a mutable copy
    fun copy() : FoodNutrientData {
        return FoodNutrientData(density).also { copy ->
            for (i in data.indices) {
                copy.data[i] = data[i]
                copy.isDataIncomplete[i] = isDataIncomplete[i]
            }
        }
    }

    // calculations

    override fun rescale(amount: Double, unit: Unit): NutrientData<FoodNutrientValue> {
        return if (unit == qtyUnit) {
            rescale(amount)
        } else {
            withQuantityUnit(unit, foodDensity, allowDefaultDensity = true).rescale(amount)
        }
    }

    fun rescale100() : FoodNutrientData = rescale(100.0)

    private fun rescale(newQuantity: Double) : FoodNutrientData {
        val conversionRatio = if (qtyAmount == 0.0) Double.NaN else newQuantity / qtyAmount

        return FoodNutrientData(density = density).also { data ->
            // completeData is false by default so we can just skip the iteration for null nutrients
            for (n in AllNutrients) {
                this[n]?.let {
                    data[n] = it.scale(conversionRatio)
                }
            }
        }
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

    private fun withQuantityUnit(newUnit: Unit, density: Double? = null, allowDefaultDensity: Boolean = false) : FoodNutrientData {
        val densityConversionNeeded = qtyUnit.type !== newUnit.type
        if (!allowDefaultDensity) {
            assert (!(densityConversionNeeded && density == null)) {
                "Quantity unit conversion required but no density given."
            }
        }
        val fallbackDensity = (if (allowDefaultDensity) 1.0 else null)

        return copy().also {
            val newAmount = it.perQuantity.convertAmountTo(newUnit, density ?: fallbackDensity)
            it.perQuantityInternal = Quantity(amount = newAmount, unit = newUnit)
            it.markIncompleteData(QUANTITY, densityConversionNeeded && density == null)
        }
    }

    override fun withDefaultUnits(
        defaultUnits: NutrientUnits,
        includingQuantity: Boolean,
        density: Double?,
    ) : FoodNutrientData {
        // TODO construct all at once
        val convertedData = if (includingQuantity) {
            withQuantityUnit(defaultUnits[QUANTITY], density, false)
        } else {
            copy()
        }
        for (nv in valuesExcludingQuantity) {
            val n = nv.nutrient
            val convertedValue = nv.convertValueTo(defaultUnits[n])
            convertedData[n] = makeComputedValue(convertedValue, n, defaultUnits[n])
            convertedData.markIncompleteData(n, hasIncompleteData(n))
        }
        return convertedData
    }

    // Use data from the another NutrientObject object to complete missing values from this one
    // Any mismatches are ignored; this object's data is preferred in all cases
    // Nothing is mutated; a new NutrientData object is returned with data copies
    override fun fillMissingData(other: BasicNutrientData<FoodNutrientValue>): FoodNutrientData {
        val result = FoodNutrientData()

        for (n in AllNutrients) {
            // note: hasIncompleteData is a stricter condition than hasData:
            // hasIncompleteData can be true even if there is a non-null value for that column, when the
            // nData object was produced by summation and there was at least one food with missing data.
            // for this purpose, we'll only replace the primary data if it was null

            val thisValue = this[n]
            val otherValue = other.getValueOrNull(n)

            val resultValue = (thisValue ?: otherValue)?.cloneWithoutMetadata()
            val resultIsDataIncomplete = when (thisValue) {
                null -> other.hasIncompleteData(n)
                else -> this.hasIncompleteData(n)
            }

            result[n] = resultValue
            result.markIncompleteData(n, resultIsDataIncomplete)
        }
        return result
    }

}

private fun FoodNutrientValue.cloneWithoutMetadata(): FoodNutrientValue {
    return factory.construct(toRowData().removeMetadata(), ObjectSource.COMPUTED)
}


