package com.machfour.macros.cli.utils

import com.machfour.macros.core.FoodType
import com.machfour.macros.entities.CompositeFood
import com.machfour.macros.entities.Food
import com.machfour.macros.jvm.createInstant
import com.machfour.macros.jvm.modifyInstant
import com.machfour.macros.util.LOCALIZED_DATETIME_MEDIUM
import com.machfour.macros.util.displayLength
import com.machfour.macros.util.formatUnicodeString
import com.machfour.macros.util.stringJoin

fun printFoodList(foods: Collection<Food>) {
    // work out how wide the column should be
    val nameLength = foods.maxOf { it.mediumName.displayLength() }
    val space = "        "
    val formatStr = "%-${nameLength}s${space}%s"
    // horizontal line - extra spaces are for whitespace + index name length
    val hline = stringJoin(listOf("="), copies = nameLength + 8 + 14)

    println(formatStr.format("Food name", "index name"))
    println(hline)
    for (f in foods) {
        print(formatUnicodeString(f.mediumName, nameLength, true))
        print(space)
        println(f.indexName)
    }
}

fun printFoodSummary(f: Food) {
    val dateFormat = LOCALIZED_DATETIME_MEDIUM
    println("Name:          ${f.longName}")
    println("Notes:         ${f.notes ?: ""}")
    println("Category:      ${f.foodCategory}")
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
    val unit = nd.qtyUnitAbbr
    println("Nutrition data (source: ${f.dataSource})")
    println()

    if (f.density != null) {
        // width copied from printFoodSummary()
        println("Density:       %.2f (g/ml)".format(f.density))
        println()
    }

    // if entered not per 100g, print both original amount and per 100 g
    if (nd.quantity != 100.0) {
        println("Per %.0f%s:".format(nd.quantity, unit))
        printNutrientData(nd, verbose)
        println()
    }
    println("Per %.0f%s:".format(nd.quantity, unit)) // should now be 100
    printNutrientData(nd.rescale100(), verbose)
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
            println(" - ${s.name}: %.1f${s.qtyUnitAbbr}".format(s.quantity))
        }
    } else {
        println("(No servings recorded)")
    }

    println()

    /*
     * Ingredients
     */

    if (f.foodType != FoodType.COMPOSITE) {
        return
    }
    assert(f is CompositeFood)

    val cf = f as CompositeFood

    println("================================")
    println()
    println("Ingredients:")
    println()
    val ingredients = cf.ingredients
    if (ingredients.isNotEmpty()) {
        printIngredients(ingredients)
        println()
    } else {
        println("(No ingredients recorded (but there probably should be!)")
    }
    println("================================")
    println()
}
