package com.machfour.macros.nutrients

import com.machfour.macros.units.GRAMS
import com.machfour.macros.units.NutrientUnits

// Nutrient data that can be used in linear combinations, i.e. sums and scaling by constant factors.
interface NutrientData<E: INutrientValue>: BasicNutrientData<E> {

    // Returns a NutrientData with all values multiplied by the given ration
    fun scale(ratio: Double): NutrientData<E>

    fun rescale(newQuantity: IQuantity): NutrientData<E> {
        return rescale(newQuantity.amount, newQuantity.unit)
    }

    fun rescale(amount: Double, unit: com.machfour.macros.entities.Unit): NutrientData<E> {
        return withQuantityUnit(unit).run {
            val oldAmount = perQuantity.amount
            val ratio = if (oldAmount != 0.0) amount / oldAmount else 0.0
            scale(ratio)
        }
    }

    fun rescale100g(): NutrientData<E> {
        return rescale(Quantity(amount = 100.0, unit = GRAMS))
    }

    // Keeps all nutrient values the same, just changes the quantity amount and unit
    // to the given one. If this NutrientData already has the given unit, this is returned.
    fun withQuantityUnit(unit: com.machfour.macros.entities.Unit): NutrientData<E>

    fun withDefaultUnits(defaultUnits: NutrientUnits): NutrientData<E>

    // Use data from the another NutrientObject object to complete missing values from this one
    // Any mismatches are ignored; this object's data is preferred in all cases
    // Nothing is mutated; a new NutrientData object is returned with data copies
    fun fillMissingData(other: BasicNutrientData<E>): NutrientData<E>
}