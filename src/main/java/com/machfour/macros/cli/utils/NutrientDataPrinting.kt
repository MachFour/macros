package com.machfour.macros.cli.utils

import com.machfour.macros.names.DefaultDisplayStrings
import com.machfour.macros.names.EnglishColumnNames
import com.machfour.macros.names.NutrientStrings
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.nutrients.Nutrients
import com.machfour.macros.util.defaultNutrientsToPrint
import com.machfour.macros.util.formatNutrientData

fun printNutrientData(foodNutrientData: FoodNutrientData, verbose: Boolean) {
    val string = formatNutrientData(
        data = foodNutrientData,
        displayStrings = DefaultDisplayStrings,
        nutrients = defaultNutrientsToPrint,
        withDp = verbose,
        monoSpaceAligned = true
    )
    println(string)
}

fun printEnergyProportions(
    foodNutrientData: FoodNutrientData,
    verbose: Boolean,
    colNames: NutrientStrings = EnglishColumnNames
) {
    println("Energy proportions (approx.)")
    val fmt = if (verbose) "%15s: %5.1f%%\n" else "%15s: %4.0f %%"
    for (n in energyProportionNutrientsToPrint) {
        println(fmt.format(colNames.getFullName(n), foodNutrientData.getEnergyProportion(n) *100))
    }
}

private val energyProportionNutrientsToPrint = setOf(
    Nutrients.PROTEIN,
    Nutrients.FAT,
    Nutrients.SATURATED_FAT,
    Nutrients.CARBOHYDRATE,
    Nutrients.SUGAR,
    Nutrients.FIBRE
)

