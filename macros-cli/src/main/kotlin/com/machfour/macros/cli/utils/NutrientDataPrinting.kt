package com.machfour.macros.cli.utils

import com.machfour.macros.formatting.fmt
import com.machfour.macros.formatting.toString
import com.machfour.macros.names.DefaultDisplayStrings
import com.machfour.macros.names.EnglishColumnNames
import com.machfour.macros.names.NutrientStrings
import com.machfour.macros.nutrients.*
import com.machfour.macros.units.LegacyNutrientUnits

fun printNutrientData(foodNutrientData: FoodNutrientData, verbose: Boolean) {
    val string = formatNutrientData(
        data = foodNutrientData,
        displayStrings = DefaultDisplayStrings,
        nutrientUnits = LegacyNutrientUnits,
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
    val qtyWidth = if (verbose) 5 else 4
    val qtyPrecision = if (verbose) 1 else 0
    for (n in energyProportionNutrientsToPrint) {
        val energyPercentage = foodNutrientData.getEnergyProportion(n) * 100
        print(colNames.getFullName(n).fmt(15) + ": ")
        println(energyPercentage.toString(qtyPrecision).fmt(qtyWidth) + " %")
    }
}

private val energyProportionNutrientsToPrint = setOf(
    PROTEIN,
    FAT,
    SATURATED_FAT,
    CARBOHYDRATE,
    SUGAR,
    FIBRE
)

