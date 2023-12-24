package com.machfour.macros.insulin

import com.machfour.macros.entities.Nutrient
import com.machfour.macros.nutrients.CARBOHYDRATE
import com.machfour.macros.nutrients.FAT
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.nutrients.PROTEIN


// Implements calculation of insulin bolus dosages from nutrition data, given
// a specific insulin to carb ratio, and optionally fat and protein factors.
// NOTE: INSULIN BOLUS CALCULATION FOR FAT AND PROTEIN IS NOT CLINICALLY VERIFIED.
// USE AT YOUR OWN RISK!
data class BolusCalculator(
    val icRatio: Double,
    val fatF: Double? = null,
    val proteinF: Double? = null,
) {

    constructor(
        icRatio: Float,
        fatFactor: Float? = null,
        proteinFactor: Float? = null,
    ): this(
        icRatio = icRatio.toDouble(),
        fatF = fatFactor?.toDouble(),
        proteinF = proteinFactor?.toDouble(),
    )

    // direct conversions from fat and protein to insulin
    val ifRatio: Double? = fatF?.let { it * icRatio }
    val ipRatio: Double? = proteinF?.let { it * icRatio }

    // Calculates insulin dosage (in units) given a carb amount (in grams) and
    // this calculator's I/C ratio (in U/g). Returns null if the I/C ratio is 0.
    fun insulinForCarbs(carbs: Double) = divideOrNull(carbs, icRatio)

    // Calculates insulin dosage (in units) given a fat amount (in grams),
    // based on this calculator's I/C ratio (in U/g), and fat factor.
    // The fat factor estimates the ratio of glucose sensitivity between fat and carbs.
    // Returns null if fat factor is not set or 0.
    fun insulinForFat(fat: Double) = divideOrNull(fat, ifRatio)

    // Calculates insulin dosage (in units) given a protein amount (in grams),
    // based on this calculator's I/C ratio (in U/g), and protein factor.
    // The protein factor estimates the ratio of glucose sensitivity between protein and carbs.
    // Returns null if protein factor is not set or 0.
    fun insulinForProtein(protein: Double) = divideOrNull(protein, ipRatio)

    // Calculates the amount of insulin for each of carbs, fat and protein in the given
    // NutrientData object, based on the insulinFor{Carbs,Fat,Protein} functions.
    // The returned map contains a pair of (insulin amount, proportion of total) for each nutrient.
    // If the nutrient data object is missing data for any nutrient, or for fat and protein, if
    // this calculator does not have the factor set, the corresponding map value is a pair of nulls.
    // If the total amount of insulin is zero (or all data is missing), proportions are null.
    fun insulinForNutrientData(nd: FoodNutrientData): Map<Nutrient, Pair<Double?, Double?>> {
        val amounts = mutableMapOf(
            CARBOHYDRATE to nd.amountOf(CARBOHYDRATE)?.let { insulinForCarbs(it) },
            FAT to nd.amountOf(FAT)?.let { insulinForFat(it) },
            PROTEIN to nd.amountOf(PROTEIN)?.let { insulinForProtein(it) },
        )
        val total = amounts.asIterable().sumOf { it.value ?: 0.0 }
        return amounts.mapValues { (_, amount) -> amount to divideOrNull(amount, total) }
    }
}

private fun divideOrNull(dividend: Double?, divisor: Double?) : Double? {
    if (dividend == null) {
        return null
    }
    return when (divisor) {
        null, 0.0 -> null
        else -> dividend / divisor
    }
}

