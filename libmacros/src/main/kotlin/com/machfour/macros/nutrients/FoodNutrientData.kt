@file:Suppress("EqualsOrHashCode")

package com.machfour.macros.nutrients

import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.FoodNutrientValue.Companion.makeComputedValue
import com.machfour.macros.entities.Unit
import com.machfour.macros.nutrients.Quantity.Companion.NullQuantity
import com.machfour.macros.nutrients.Quantity.Companion.toQuantity
import com.machfour.macros.schema.FoodNutrientValueTable
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
        set(value) {
            this[QUANTITY] = makeComputedValue(value.amount, QUANTITY, value.unit)
        }

    val qtyUnit: Unit
        get() = perQuantity.unit

    val qtyAmount: Double
        get() = perQuantity.amount

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

    // Returns a new FoodNutrientData with all nutrient values multiplied
    // by the given ratio.
    override fun scale(ratio: Double) : FoodNutrientData {
        return FoodNutrientData(density = density).also { data ->
            // completeData is false by default so we can just skip the iteration for null nutrients
            for (n in AllNutrients) {
                if (this[n] != null) {
                    data[n] = getValue(n).scale(ratio)
                }
                if (hasIncompleteData(n)) {
                    data.markIncompleteData(n)
                }
            }
        }
    }

    // keeps all nutrient values the same, just changes the quantity unit and amount accordingly
    override fun withQuantityUnit(unit: Unit) : FoodNutrientData {
        if (unit == qtyUnit) {
            return this
        }
        val densityConversionNeeded = qtyUnit.type != unit.type

        return copy().also {
            it.perQuantityInternal = perQuantity.convertTo(unit, density ?: 1.0)
            it.markIncompleteData(QUANTITY, densityConversionNeeded && density == null)
        }
    }

    override fun withDefaultUnits(defaultUnits: NutrientUnits) : FoodNutrientData {
        // TODO construct all at once
        val withDesiredQtyUnit = copy().withQuantityUnit(defaultUnits[QUANTITY])
        for (nv in valuesExcludingQuantity) {
            val n = nv.nutrient
            val convertedValue = nv.convertValueTo(defaultUnits[n])
            withDesiredQtyUnit[n] = makeComputedValue(convertedValue, n, defaultUnits[n])
            withDesiredQtyUnit.markIncompleteData(n, hasIncompleteData(n))
        }
        return withDesiredQtyUnit
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
    return FoodNutrientValueTable.construct(toRowData().removeMetadata(), ObjectSource.COMPUTED)
}


