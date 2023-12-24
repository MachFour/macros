package com.machfour.macros.insulin

import com.machfour.macros.nutrients.CARBOHYDRATE
import com.machfour.macros.nutrients.FAT
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.nutrients.PROTEIN
import com.machfour.macros.util.fmt
import com.machfour.macros.util.toString

// parses a string in one of the following forms:
// 1. <ic ratio>
// 2. <ic ratio>:<fat factor>:<protein factor>
// where all quantities are positive floating point numbers
fun parseInsulinArgument(arg: String) : BolusCalculator? {
    val params = arg
        .split(":")
        .also { if (it.size !in listOf(1, 3)) { return null } }
        .map { it.toDoubleOrNull()?.takeIf { d -> d > 0.0 } ?: return null }

    return BolusCalculator(
        icRatio = params[0],
        fatF = params.getOrNull(1),
        proteinF = params.getOrNull(2),
    )
}

private const val labelPrintWidth = 8
private const val unitsPrintWidth = 6

fun printInsulin(nd: FoodNutrientData, params: BolusCalculator) {
    println("========")
    println("Insulin:")
    println("========")
    println()

    val insulin = params.insulinForNutrientData(nd)
    val data = listOf(
        "Carbs" to insulin[CARBOHYDRATE]?.first,
        "Fat" to insulin[FAT]?.first,
        "Protein" to insulin[PROTEIN]?.first,
        "Total" to insulin.asIterable().sumOf { it.value.first ?: 0.0 },
    )

    for ((label, value) in data) {
        print(label.fmt(labelPrintWidth) + "  ")
        println((value?.toString(2) ?: "").fmt(unitsPrintWidth) + "U")
    }

    println()

    println("IC Ratio: ${params.icRatio}")
    println("Fat factor: ${params.fatF}")
    println("Protein factor: ${params.proteinF}")
}
