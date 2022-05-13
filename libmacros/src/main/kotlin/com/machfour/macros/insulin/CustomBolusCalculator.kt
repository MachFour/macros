package com.machfour.macros.insulin

import com.machfour.macros.nutrients.FoodNutrientData


fun interface CustomBolusCalculator {
    fun computeInsulin(nd: FoodNutrientData): Double

    companion object {
        fun fromString(s: String): CustomBolusCalculator {
            return CustomBolusCalculator { nd ->
                0.0
                // Was using ExprK library but it's not multiplatform (needs work to port BigDecimal from Java)
                //Expressions().run {
                //    define("p", nd.amountOf(PROTEIN, defaultValue = 0.0))
                //    define("f", nd.amountOf(FAT, defaultValue = 0.0))
                //    define("c", nd.amountOf(CARBOHYDRATE, defaultValue = 0.0))
                //    define("r", nd.amountOf(FIBRE, defaultValue = 0.0))
                //    eval(s).toDouble()
                //}
            }
        }
    }
}

