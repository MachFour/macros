package com.machfour.macros.insulin

import com.machfour.macros.entities.Nutrient
import com.machfour.macros.formatting.fprounding.round
import com.machfour.macros.nutrients.CARBOHYDRATE
import com.machfour.macros.nutrients.FAT
import com.machfour.macros.nutrients.PROTEIN
import com.machfour.macros.nutrients.QUANTITY
import kotlin.math.abs
import kotlin.math.max

private data class InsulinAmountsImpl(
    val carbsUpfront: Double?,
    val carbsExtended: Double?,
    val fatUpfront: Double?,
    val fatExtended: Double?,
    val proteinUpfront: Double?,
    val proteinExtended: Double?,
): InsulinAmounts {

    override fun total(): Double {
        return upfrontTotal() + extendedTotal()
    }

    override fun upfrontTotal(): Double {
        return (carbsUpfront ?: 0.0) + (fatUpfront ?: 0.0) + (proteinUpfront  ?: 0.0)
    }

    override fun extendedTotal(): Double {
        return (carbsExtended ?: 0.0) + (fatExtended ?: 0.0) + (proteinExtended  ?: 0.0)
    }

    override fun totalFor(n: Nutrient): Double? {
        return when (n) {
            CARBOHYDRATE -> sumUnlessBothNull(carbsUpfront, carbsExtended)
            FAT -> sumUnlessBothNull(fatUpfront, fatExtended)
            PROTEIN -> sumUnlessBothNull(proteinUpfront, proteinExtended)
            QUANTITY -> total()
            else -> null
        }
    }

    override fun upfrontFor(n: Nutrient): Double? {
        return when (n) {
            CARBOHYDRATE -> carbsUpfront
            FAT -> fatUpfront
            PROTEIN -> proteinUpfront
            QUANTITY -> upfrontTotal()
            else -> null
        }
    }

    override fun extendedFor(n: Nutrient): Double? {
        return when (n) {
            CARBOHYDRATE -> carbsExtended
            FAT -> fatExtended
            PROTEIN -> proteinExtended
            QUANTITY -> extendedTotal()
            else -> null
        }
    }

    override fun totalMap(): Map<Nutrient, Double?> {
        return nutrientMapKeys.associateWith { totalFor(it) }
    }

    override fun upfrontMap(): Map<Nutrient, Double?> {
        return nutrientMapKeys.associateWith { upfrontFor(it) }
    }

    override fun extendedMap(): Map<Nutrient, Double?> {
        return nutrientMapKeys.associateWith { extendedFor(it) }
    }

    override fun totalNutrientProportions(): Map<Nutrient, Double> {
        return nutrientMapKeys.associateWith { totalProportionFor(it) ?: 0.0 }
    }

    override fun upfrontProportions(): Map<Nutrient, Double> {
        return nutrientMapKeys.associateWith { upfrontProportionFor(it) ?: 0.0 }
    }

    override fun extendedProportions(): Map<Nutrient, Double> {
        return nutrientMapKeys.associateWith { extendedProportionFor(it) ?: 0.0 }
    }

    override fun equals(other: InsulinAmounts, absTol: Double, relTol: Double) : Boolean {
        return amountsEqual(carbsUpfront, other.upfrontFor(CARBOHYDRATE), absTol, relTol)
                && amountsEqual(carbsExtended, other.extendedFor(CARBOHYDRATE), absTol, relTol)
                && amountsEqual(fatUpfront, other.upfrontFor(FAT), absTol, relTol)
                && amountsEqual(fatExtended, other.extendedFor(FAT), absTol, relTol)
                && amountsEqual(proteinUpfront, other.upfrontFor(PROTEIN), absTol, relTol)
                && amountsEqual(proteinExtended, other.extendedFor(PROTEIN), absTol, relTol)
    }

    override fun totalProportionFor(n: Nutrient): Double? {
        return if (n === QUANTITY) 1.0 else divideOrNull(totalFor(n), total())
    }

    override fun upfrontProportionFor(n: Nutrient): Double? {
        return if (n === QUANTITY) 1.0 else divideOrNull(totalFor(n), total())
    }

    override fun extendedProportionFor(n: Nutrient): Double? {
        return if (n === QUANTITY) 1.0 else divideOrNull(extendedFor(n), extendedTotal())
    }

    override fun upfrontProportion(): Double {
        return upfrontTotal() / total()
    }

    override fun extendedProportion(): Double {
        return extendedTotal() / total()
    }

    override fun splitProportions(): Pair<Double, Double> {
        return upfrontProportion() to extendedProportion()
    }
}

private val nutrientMapKeys = listOf(CARBOHYDRATE, FAT, PROTEIN, QUANTITY)

private fun sumUnlessBothNull(a: Double?, b: Double?): Double? {
    return if (a == null && b == null) null else (a ?: 0.0) + (b ?: 0.0)
}

// Constructs a new InsulinAmounts instance with the given precision.
// If precision is a non-negative number, less than the precision of a double,
// the input amounts will be rounded to that many decimal places.
fun insulinAmounts(
    carbsUpfront: Double? = null,
    carbsExtended: Double? = null,
    fatUpfront: Double? = null,
    fatExtended: Double? = null,
    proteinUpfront: Double? = null,
    proteinExtended: Double? = null,
    precision: Int = -1,
): InsulinAmounts {
    return InsulinAmountsImpl(
        carbsUpfront = carbsUpfront?.round(precision),
        carbsExtended = carbsExtended?.round(precision),
        fatUpfront = fatUpfront?.round(precision),
        fatExtended = fatExtended?.round(precision),
        proteinUpfront = proteinUpfront?.round(precision),
        proteinExtended = proteinExtended?.round(precision),
    )
}

private fun divideOrNull(dividend: Double?, divisor: Double) : Double? {
    if (divisor == 0.0) {
        return null
    }
    return dividend?.div(divisor)
}

private fun amountsEqual(d1: Double?, d2: Double?, absTol: Double, relTol: Double): Boolean {
    if (d1 == null || d2 == null) {
        return d1 == null && d2 == null
    }
    if (d1.isNaN() || d2.isNaN()) {
        return d1.isNaN() && d2.isNaN()
    }
    if (d1 == 0.0 || d2 == 0.0) {
        return d1 == 0.0 && d2 == 0.0
    }
    return abs(d1 - d2) < absTol && abs(max(d1/d2, d2/d1) - 1) < relTol

}