package com.machfour.macros.insulin

import com.machfour.macros.core.Schema
import com.machfour.macros.objects.NutritionData

object InsulinCalculations {
    // Calculates insulin dosage (in units) given a carb amount (in grams) and I/C ratio (in U/g).
    // Returns zero if I/C ratio is zero.
    @Deprecated("use InsulinParams")
    fun insulinForCarbs(carbs: Double, icRatio: Double) : Double {
        return if (icRatio != 0.0) carbs / icRatio else 0.0
    }
    fun insulinForCarbs(carbs: Double, params: InsulinParams) = insulinForCarbs(carbs, params.icRatio)

    // Calculates insulin dosage (in units) given a protein amount (in grams), I/C ratio (in U/g), and
    // protein factor which estimates the ratio of glucose sensitivity between protein and carbs.
    // Returns zero if I/C ratio or protein factor are zero.
    @Deprecated("use InsulinParams")
    fun insulinForProtein(protein: Double, icRatio: Double, proteinFactor: Double) : Double {
        return insulinForCarbs(protein, icRatio*proteinFactor)
    }
    fun insulinForProtein(protein: Double, params: InsulinParams) = insulinForCarbs(protein, params.ipRatio)

    // returns insulinFromCarbs for nd's amount of carbs, or null if it does not have any data for carbs
    @Deprecated("use InsulinParams")
    fun insulinForCarbs(nd: NutritionData, icRatio: Double) : Double? {
        val carbs = nd.getData(Schema.NutritionDataTable.CARBOHYDRATE)
        return carbs?.let { insulinForCarbs(it, icRatio) }
    }

    // returns insulinFromCarbs for nd's amount of carbs, or null if it does not have any data for carbs
    fun insulinForCarbs(nd: NutritionData, params: InsulinParams) : Double? {
        val carbs = nd.getData(Schema.NutritionDataTable.CARBOHYDRATE)
        return carbs?.let { insulinForCarbs(it, params) }
    }


    // returns insulinFromProtein for nd's amount of protein, or null if it does not have any data for protein
    @Deprecated("use InsulinParams")
    fun insulinForProtein(nd: NutritionData, icRatio: Double, proteinFactor: Double) : Double? {
        val protein = nd.getData(Schema.NutritionDataTable.PROTEIN)
        return protein?.let { insulinForProtein(it, icRatio, proteinFactor) }

    }
    // returns insulinFromProtein for nd's amount of protein, or null if it does not have any data for protein
    fun insulinForProtein(nd: NutritionData, params: InsulinParams) : Double? {
        val protein = nd.getData(Schema.NutritionDataTable.PROTEIN)
        return protein?.let { insulinForProtein(it, params) }
    }

}

