package com.machfour.macros.insulin

import com.github.keelar.exprk.Expressions
import com.machfour.macros.nutrients.Nutrients
import com.machfour.macros.nutrients.FoodNutrientData


fun interface CustomBolusCalculator {
    fun computeInsulin(nd: FoodNutrientData): Double

    companion object {
        fun fromString(s: String): CustomBolusCalculator {
            return CustomBolusCalculator { nd ->
                Expressions().run {
                    define("p", nd.amountOf(Nutrients.PROTEIN, defaultValue = 0.0))
                    define("f", nd.amountOf(Nutrients.FAT, defaultValue = 0.0))
                    define("c", nd.amountOf(Nutrients.CARBOHYDRATE, defaultValue = 0.0))
                    define("r", nd.amountOf(Nutrients.FIBRE, defaultValue = 0.0))
                    eval(s).toDouble()
                }
            }
        }
    }
}

