package com.machfour.macros.cli.utils

import com.machfour.macros.core.FoodType
import com.machfour.macros.entities.CompositeFood
import com.machfour.macros.entities.Food
import com.machfour.macros.formatting.fmt
import com.machfour.macros.formatting.toString
import com.machfour.macros.jvm.LOCALIZED_DATETIME_MEDIUM
import com.machfour.macros.jvm.createInstant
import com.machfour.macros.jvm.modifyInstant

fun printFoodList(foods: Collection<Food>) {
    // work out how wide the column should be
    val nameLength = foods.maxOf { it.mediumName.displayLength() }
    val space = "        "
    // horizontal line - extra spaces are for whitespace + index name length
    val hline = "=".repeat(nameLength + 8 + 14)

    println("Food name".fmt(nameLength, true) + space + "index name")
    println(hline)
    for (f in foods) {
        println(f.mediumName.fmtUnicode(nameLength, true) + space + f.indexName)
    }
}

fun printFoodSummary(f: Food) {
    val dateFormat = LOCALIZED_DATETIME_MEDIUM
    println("Name:          ${f.longName}")
    println("Notes:         ${f.notes ?: ""}")
    println("Category:      ${f.category}")
    println()
    println("Type:          ${f.foodType}")
    println("Created on:    ${dateFormat.format(f.createInstant)}")
    println("Last modified: ${dateFormat.format(f.modifyInstant)}")
}

fun printFood(f: Food, verbose: Boolean) {
    println("============")
    println(" Food Data  ")
    println("============")
    println()
    println()
    printFoodSummary(f)

    println("================================")
    println()
    /*
     * Nutrition data
     */
    val nd = f.nutrientData
    val qty = nd.perQuantity
    val unit = qty.unit
    val dataSource = f.dataSource ?: (if (f.foodType === FoodType.COMPOSITE) "recipe" else "unknown")
    println("Nutrition data (source: $dataSource)")
    println()

    if (f.density != null) {
        // width copied from printFoodSummary()
        println("Density:       ${f.density?.toString(2)} (g/ml)")
        println()
    }

    // if entered not per 100g, print both original amount and per 100 g
    if (qty.amount != 100.0) {
        println("Per ${qty.amount.toString(0)}${unit.abbr}:")
        printNutrientData(nd, verbose)
        println()
    }
    println("Per 100${unit.abbr}:")
    printNutrientData(nd.rescale(100.0, unit), verbose)
    println()

    /*
     * Servings
     */
    println("================================")
    println()
    println("Servings:")
    println()

    val servings = f.servings
    if (servings.isNotEmpty()) {
        for (s in servings) {
            println(" - ${s.name}: ${s.amount.toString(1)}${s.qtyUnitAbbr}")
        }
    } else {
        println("(No servings recorded)")
    }

    println()

    /*
     * Ingredients
     */

    if (f.foodType == FoodType.COMPOSITE) {
        check(f is CompositeFood)

        println("================================")
        println()
        println("Ingredients:")
        println()
        val ingredients = f.ingredients
        if (ingredients.isNotEmpty()) {
            printIngredients(ingredients)
            println()
        } else {
            println("(No ingredients recorded (but there probably should be!)")
        }
        println("================================")
        println()
    }
}
