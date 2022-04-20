package com.machfour.macros.insulin

import com.machfour.macros.nutrients.CARBOHYDRATE
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.nutrients.PROTEIN

// Calculates insulin dosage (in units) given a carb amount (in grams) and I/C ratio (in U/g).
// Returns zero if I/C ratio is zero.
private fun insulinForCarbs(carbs: Double, icRatio: Double) : Double {
    return if (icRatio != 0.0) carbs / icRatio else 0.0
}

fun insulinForCarbs(carbs: Double, params: InsulinParams) = insulinForCarbs(carbs, params.icRatio)

// Calculates insulin dosage (in units) given a protein amount (in grams), I/C ratio (in U/g), and
// protein factor which estimates the ratio of glucose sensitivity between protein and carbs.
// Returns zero if I/C ratio or protein factor are zero.
fun insulinForProtein(protein: Double, params: InsulinParams) = insulinForCarbs(protein, params.ipRatio)

// returns insulinFromCarbs for nd's amount of carbs, or null if it does not have any data for carbs
fun insulinForCarbs(nd: FoodNutrientData, params: InsulinParams) : Double? {
    val carbs = nd.amountOf(CARBOHYDRATE)
    return carbs?.let { insulinForCarbs(it, params) }
}


// returns insulinFromProtein for nd's amount of protein, or null if it does not have any data for protein
fun insulinForProtein(nd: FoodNutrientData, params: InsulinParams) : Double? {
    val protein = nd.amountOf(PROTEIN)
    return protein?.let { insulinForProtein(it, params) }
}