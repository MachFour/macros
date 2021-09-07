package com.machfour.macros.insulin

import com.github.keelar.exprk.Expressions
import com.machfour.macros.nutrients.*


fun interface CustomBolusCalculator {
    fun computeInsulin(nd: FoodNutrientData): Double

    companion object {
        fun fromString(s: String): CustomBolusCalculator {
            return CustomBolusCalculator { nd ->
                Expressions().run {
                    define("p", nd.amountOf(PROTEIN, defaultValue = 0.0))
                    define("f", nd.amountOf(FAT, defaultValue = 0.0))
                    define("c", nd.amountOf(CARBOHYDRATE, defaultValue = 0.0))
                    define("r", nd.amountOf(FIBRE, defaultValue = 0.0))
                    eval(s).toDouble()
                }
            }
        }
    }
}

