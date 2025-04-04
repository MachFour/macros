package com.machfour.macros.insulin

import com.machfour.macros.entities.Nutrient

// InsulinAmounts stores a calculated insulin amount returned by BolusCalculator.insulinFor
// given a particular instance (set of parameters) and nutrient values
// The total insulin quantity is broken up into components by nutrient and also by
// nominal 'upfront' and 'extended' doses.
// If a particular nutrient was not involved in the calculation of the total amount,
// values for that nutrient will return null.
interface InsulinAmounts {
    // Returns the sum of all components
    fun total(): Double

    // Returns the sum of all upfront amounts for each nutrient
    fun upfrontTotal(): Double
    // Returns the sum of all extended amounts for each nutrient
    fun extendedTotal(): Double

    // Returns the total contribution of the given nutrient to the total amount,
    // or null if that nutrient was not part of the calculation.
    fun totalFor(n: Nutrient): Double?

    // Returns the total contribution of the given nutrient to the upfront amount,
    // or null if that nutrient was not part of the calculation.
    fun upfrontFor(n: Nutrient): Double?

    // Returns the total contribution of the given nutrient to the extended amount,
    // or null if that nutrient was not part of the calculation.
    fun extendedFor(n: Nutrient): Double?

    // Returns a map from nutrient to the totalFor() value for that nutrient.
    // Additionally, the QUANTITY nutrient contains the total() value, which is never null.
    fun totalMap() : Map<Nutrient, Double?>

    // Returns a map from nutrient to the upfrontFor() value for that nutrient.
    // Additionally, the QUANTITY nutrient contains the upfrontTotal() value, which is never null.
    fun upfrontMap() : Map<Nutrient, Double?>

    // Returns a map from nutrient to the extendedFor() value for that nutrient.
    // Additionally, the QUANTITY nutrient contains the extendedTotal() value, which is never null.
    fun extendedMap() : Map<Nutrient, Double?>

    // Converts values returned by totalFor() from absolute amounts to proportions of the
    // total() amount. Any null values for individual nutrients are coalesced to 0.
    // If total() is zero, the returned proportions are zero, except for QUANTITY, which is always 1.
    fun totalNutrientProportions(): Map<Nutrient, Double>

    // Like totalProportions() except for upfrontFor() / upfrontTotal()
    fun upfrontProportions(): Map<Nutrient, Double>

    // Like totalProportions() except for extendedFor() / extendedTotal()
    fun extendedProportions(): Map<Nutrient, Double>

    fun upfrontProportionFor(n: Nutrient): Double?
    fun extendedProportionFor(n: Nutrient): Double?
    fun totalProportionFor(n: Nutrient): Double?

    // Returns the relative proportions of upfront and extended boluses, respectively
    fun upfrontProportion(): Double
    fun extendedProportion(): Double
    fun splitProportions(): Pair<Double, Double>

    // Returns true if all quantities in other are close in both absolute and
    // relative tolerances to the quantities in this InsulinAmounts
    fun equals(other: InsulinAmounts, absTol: Double = 1e-3, relTol: Double = 1e-3): Boolean
}
