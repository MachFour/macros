package com.machfour.macros.insulin

import com.machfour.macros.formatting.fmt
import com.machfour.macros.formatting.toString
import com.machfour.macros.nutrients.*

// parses a string in one of the following forms:
// 1. <ic ratio>
// 2. <ic ratio>:<fat factor>:<protein factor>
// where all quantities are positive floating point numbers
fun parseInsulinArgument(arg: String) : SimpleBolusCalculator? {
    val params = arg
        .split(":")
        .also { if (it.size !in listOf(1, 3)) { return null } }
        .map { it.toDoubleOrNull()?.takeIf { d -> d > 0.0 } ?: return null }

    return SimpleBolusCalculator(
        icRatio = params[0],
        fatF = params.getOrNull(1) ?: 0.0,
        proteinF = params.getOrNull(2) ?: 0.0,
    )
}

private const val labelPrintWidth = 8
private const val unitsPrintWidth = 6

fun printInsulin(nd: FoodNutrientData, params: SimpleBolusCalculator) {
    println("========")
    println("Insulin:")
    println("========")
    println()

    val insulin = params.insulinFor(nd).totalMap()
    val data = listOf(
        "Carbs" to insulin[CARBOHYDRATE],
        "Fat" to insulin[FAT],
        "Protein" to insulin[PROTEIN],
        "Total" to insulin[QUANTITY]
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
