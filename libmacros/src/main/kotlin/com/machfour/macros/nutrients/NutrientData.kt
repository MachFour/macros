package com.machfour.macros.nutrients

import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.LegacyNutrientUnits
import com.machfour.macros.units.NutrientUnits

// Nutrient data that can be used in linear combinations, i.e. sums and scaling by constant factors.
interface NutrientData<E: INutrientValue>: BasicNutrientData<E> {

    fun rescale(newQuantity: IQuantity): NutrientData<E> {
        return rescale(newQuantity.amount, newQuantity.unit)
    }

    fun rescale(amount: Double, unit: com.machfour.macros.entities.Unit): NutrientData<E>

    fun rescale100g(): NutrientData<E> {
        return rescale(Quantity(amount = 100.0, unit = GRAMS))
    }

    fun withDefaultUnits(
        defaultUnits: NutrientUnits = LegacyNutrientUnits,
        includingQuantity: Boolean = false,
        density: Double? = null
    ): NutrientData<E>

    // Use data from the another NutrientObject object to complete missing values from this one
    // Any mismatches are ignored; this object's data is preferred in all cases
    // Nothing is mutated; a new NutrientData object is returned with data copies
    fun fillMissingData(other: BasicNutrientData<E>): NutrientData<E>
}