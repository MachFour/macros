package com.machfour.macros.insulin


// Implements calculation of insulin bolus dosages from nutrition data, given
// a specific insulin to carb ratio, and optionally fat and protein factors.
//
// The fat and protein factors model blood glucose sensitivity to fat and protein
// respectively, as a constant fraction of the sensitivity to carbs (i.e. I:C ratio).
//
// NOTE: INSULIN BOLUS CALCULATION FOR FAT AND PROTEIN IS NOT CLINICALLY VERIFIED.
// USE AT YOUR OWN RISK!
data class SimpleBolusCalculator(
    val icRatio: Double,
    val fatF: Double = 0.0,
    val proteinF: Double = 0.0,
) : BolusCalculator {

    constructor(
        icRatio: Float,
        fatF: Float = 0f,
        proteinF: Float = 0f,
    ): this(
        icRatio = icRatio.toDouble(),
        fatF = fatF.toDouble(),
        proteinF = proteinF.toDouble(),
    )

    // Direct conversion factor from fat (in grams) to insulin.
    // It is equivalent to the fat factor times the I:C ratio.
    // If the fat factor is unset (set to zero), this is also zero.
    val ifRatio: Double
        get() = fatF * icRatio

    // Direct conversion factor from protein (in grams) to insulin.
    // It is equivalent to the protein factor times the I:C ratio.
    // If the protein factor is unset (set to zero), this is also zero.
    val ipRatio: Double
        get() = proteinF * icRatio

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
    override fun insulinFor(
        carbs: Double?,
        fat: Double?,
        protein: Double?,
        precision: Int,
    ): InsulinAmounts {
        return insulinAmounts(
            carbsUpfront = carbs?.divideOrNull(icRatio),
            fatExtended = fat?.divideOrNull(ifRatio),
            proteinExtended = protein?.divideOrNull(ipRatio),
            precision = precision,
        )
    }
}

private fun Double.divideOrNull(divisor: Double) = if (divisor != 0.0) this / divisor else null
