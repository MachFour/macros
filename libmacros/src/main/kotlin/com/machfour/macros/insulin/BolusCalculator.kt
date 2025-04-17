package com.machfour.macros.insulin

import com.machfour.macros.entities.INutrient
import com.machfour.macros.nutrients.BasicNutrientData
import com.machfour.macros.nutrients.CARBOHYDRATE
import com.machfour.macros.nutrients.FAT
import com.machfour.macros.nutrients.PROTEIN
import com.machfour.macros.units.GRAMS

// Interface for calculation of insulin bolus dosages from nutrition data.
// Implementing classes define a particular calculation method and set of parameters.
//
// NOTE: INSULIN BOLUS CALCULATION FOR FAT AND PROTEIN IS NOT CLINICALLY VERIFIED.
// USE AT YOUR OWN RISK!
interface BolusCalculator {
    /*
     Calculates insulin bolus in units, given amounts *in grams* of carbs,
     fat and protein, using this calculator's I:C ratio (in U/g) and fat
     and protein factors (if set). The carbs value should include sugar but
     not include fibre.

     If precision is a non-negative number less than the precision of a double,
     the insulin amounts are rounded to that number of decimal places.
     The default value for precision is -1 (rounding disabled).

     If this calculator's I:C ratio is set to zero, the returned InsulinAmounts
     will have all values set to null except the totals.
    */
    fun insulinFor(
        carbs: Double? = null,
        fat: Double? = null,
        protein: Double? = null,
        precision: Int = -1,
    ): InsulinAmounts

    /*
     Calculates insulin bolus in units, given nutrient amounts *in grams*
     using this calculator's I:C ratio (in U/g) and fat/protein factors (if set).
     Currently, only CARBOHYDRATE, FAT and PROTEIN are considered.
     The carbs value should include sugar but not include fibre.

     If precision is a non-negative number less than the precision of a double,
     the insulin amounts are rounded to that number of decimal places.
     The default value for precision is -1 (rounding disabled).

     If this calculator's I:C ratio is set to zero, the returned InsulinAmounts
     will have all values set to null except the totals.
    */
    fun insulinFor(
        amounts: Map<INutrient, Double>,
        precision: Int = -1,
    ): InsulinAmounts {
        return insulinFor(
            precision = precision,
            carbs = amounts[CARBOHYDRATE],
            fat = amounts[FAT],
            protein = amounts[PROTEIN],
        )
    }

    // Same as above except values are extracted from the given FoodNutrientData,
    // converting to grams if necessary.
    fun insulinFor(
        nd: BasicNutrientData<*>,
        precision: Int = -1
    ): InsulinAmounts {
        return insulinFor(
            precision = precision,
            carbs = nd.amountOf(CARBOHYDRATE, GRAMS),
            fat = nd.amountOf(FAT, GRAMS),
            protein = nd.amountOf(PROTEIN, GRAMS),
        )
    }
}

