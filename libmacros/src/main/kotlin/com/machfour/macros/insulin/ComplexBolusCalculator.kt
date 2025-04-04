package com.machfour.macros.insulin

import kotlin.math.abs
import kotlin.math.min


const val defaultLowCarbsThreshold = 10.0 // grams
const val defaultFatLowCarbCoefficient = 0.05
const val defaultProteinLowCarbCoefficient = 0.25

const val defaultProteinMaxCoefficient = 0.5
const val defaultFatMaxCoefficient = 0.4

const val defaultFatThresholdForMaxExtendedCarbs = 40.0 // grams
const val defaultCarbsMaxExtendedWeighting = 1.0/3

const val defaultProteinExtendedWeighting = 0.2

// Implements experimental calculation of insulin bolus dosages from nutrition data,
// attempting to model interactions between protein / carbs and fat / carbs when
// the amounts of each reach a certain level.
// Retains the I:C ratio as a global scaling parameter.
//
// NOTE: INSULIN BOLUS CALCULATION FOR FAT AND PROTEIN IS NOT CLINICALLY VERIFIED.
// USE AT YOUR OWN RISK!
class ComplexBolusCalculator(
    val icRatio: Double,

    val lowCarbsThreshold: Double = defaultLowCarbsThreshold, // grams
    val proteinLowCarbCoefficient: Double = defaultProteinLowCarbCoefficient,
    val fatLowCarbCoefficient: Double = defaultFatLowCarbCoefficient,
    val proteinMaxCoefficient: Double = defaultProteinMaxCoefficient,
    val fatMaxCoefficient: Double = defaultFatMaxCoefficient,
    // For input data with at least amount of fat (in grams), the insulin bolus
    // for carbs has the highest extended bolus proportion / smallest upfront proportion.
    val fatThresholdForMaxExtendedCarbs: Double = defaultFatThresholdForMaxExtendedCarbs,
    val carbsMaxExtendedWeighting: Double = defaultCarbsMaxExtendedWeighting,
    val proteinExtendedWeighting: Double = defaultProteinExtendedWeighting,
) : BolusCalculator {

    constructor(
        icRatio: Float,
        lowCarbsThreshold: Double = defaultLowCarbsThreshold, // grams
        proteinLowCarbCoefficient: Double = defaultProteinLowCarbCoefficient,
        fatLowCarbCoefficient: Double = defaultFatLowCarbCoefficient,
        proteinMaxCoefficient: Double = defaultProteinMaxCoefficient,
        fatMaxCoefficient: Double = defaultFatMaxCoefficient,
        fatThresholdForMaxExtendedCarbs: Double = defaultFatThresholdForMaxExtendedCarbs,
        carbsMaxExtendedWeighting: Double = defaultCarbsMaxExtendedWeighting,
        proteinExtendedWeighting: Double = defaultProteinExtendedWeighting,
    ): this(
        icRatio = icRatio.toDouble(),
        fatThresholdForMaxExtendedCarbs = fatThresholdForMaxExtendedCarbs,
        lowCarbsThreshold = lowCarbsThreshold,
        fatLowCarbCoefficient = fatLowCarbCoefficient,
        proteinLowCarbCoefficient = proteinLowCarbCoefficient,
        carbsMaxExtendedWeighting = carbsMaxExtendedWeighting,
        proteinExtendedWeighting = proteinExtendedWeighting,
        proteinMaxCoefficient = proteinMaxCoefficient,
        fatMaxCoefficient = fatMaxCoefficient,
    )

    init {
        require(carbsMaxExtendedWeighting in 0.0 .. 1.0) { "carbsMaxExtendedWeighting=$carbsMaxExtendedWeighting (must be between 0-1)" }
        require(proteinExtendedWeighting in 0.0 .. 1.0) { "proteinExtendedWeighting=$proteinExtendedWeighting (must be between 0-1)" }
        require(proteinMaxCoefficient >= 0) { "proteinMaxCoefficient=$proteinMaxCoefficient (must be >= 0)" }
        require(fatMaxCoefficient >= 0) { "fatMaxCoefficient=$fatMaxCoefficient (must be >= 0)" }
        require(fatLowCarbCoefficient >= 0) { "fatLowCarbCoefficient=$fatLowCarbCoefficient (must be >= 0)" }
        require(proteinLowCarbCoefficient >= 0) { "proteinLowCarbCoefficient=$proteinLowCarbCoefficient (must be >= 0)" }
    }

    /*
    Cases:

    ## LOW CARB (C < 10g)
       I[upfront] = C / icr
       I[extended] = (P * 0.25 + F * 0.05) / icr


    ## HIGH CARB (C >= 10g)
    - fat coefficient for F < 40g: F/100
    -> always have extended fat bolus of F*max(0.4,F/100)/icr
    - total protein coefficient for F < 40g: P/80
    -> always have upfront protein bolus of P*max(0.4,P/100)/icr
    -> always have extended protein bolus of P*max(0.1,P/400)/icr
    - split carbs bolus into upfront/extended based on how much fat there is
    -> up to 33% extended if fat >= 40g

    HC:
       C_u = C * (1 - 0.33*min(1, F/40)))
       C_e = C * 0.33*min(1, F/40))

       I[upfront] = (C_u + P * max(0.4, P/100)) / icr
       I[extended] = (C_e P * max(0.1, P / 400) + F * max(0.4, F / 100)) / icr

    OLD
    HC1: P < 40g, F < 40g
       I[upfront] = C / icr
       I[extended] = F * max(0.4, F / 100) / icr
    HC2: P >= 40g, F < 40g
       I[upfront] = (C + P * 0.4) / icr
       I[extended] = (P * 0.1) / icr + F * max(0.4, F / 100) / icr
    HC3: F >= 40g, P < 40g
       I[extended] = (C + F * 0.4) / icr
    HC4. F >= 40g, P >= 40g
       I[upfront] = (C/2 + P * 0.4) / icr
       I[extended or +3 hrs] = (C/2 + P * 0.1 + F * 0.4) / icr

    NOTE: typically have to reduce ICR by around 30% (multiply by 1.3)
          when using this method
    */

    override fun insulinFor(
        carbs: Double?,
        fat: Double?,
        protein: Double?,
        precision: Int,
    ): InsulinAmounts {
        if (icRatio == 0.0) {
            return insulinAmounts()
        }
        if (carbs != null && carbs >= lowCarbsThreshold) {
            // high carb setting
            val totalCarbsAmount = carbs / icRatio
            val totalProteinAmount = protein?.let {
                it * min(proteinMaxCoefficient, it/80) / icRatio
            }
            val totalFatAmount = fat?.let {
                it * min(fatMaxCoefficient, it/100) / icRatio
            }

            // split carbs dose up to 33% as extended, based on how close
            // the fat amount is to the fatSignificantThreshold.
            val fatProportionOfThreshold = min(1.0, abs(fat ?: 0.0) / fatThresholdForMaxExtendedCarbs)
            val carbsExtendedWeighting = carbsMaxExtendedWeighting * fatProportionOfThreshold

            return insulinAmounts(
                carbsUpfront = totalCarbsAmount * (1 - carbsExtendedWeighting),
                carbsExtended = totalCarbsAmount * carbsExtendedWeighting,
                fatUpfront = totalFatAmount?.let { 0.0 },
                fatExtended = totalFatAmount,
                proteinUpfront = totalProteinAmount?.let { it * (1 - proteinExtendedWeighting) },
                proteinExtended = totalProteinAmount?.let { it * proteinExtendedWeighting },
                precision = precision,
            )
        } else {
            // low carb setting
            val totalCarbsAmount = carbs?.let { it / icRatio }
            val totalFatAmount = fat?.let { it * fatLowCarbCoefficient / icRatio }
            val totalProteinAmount = protein?.let { it * proteinLowCarbCoefficient / icRatio }

            return insulinAmounts(
                carbsUpfront = totalCarbsAmount,
                carbsExtended = totalCarbsAmount?.let { 0.0 },
                fatUpfront = totalFatAmount?.let { 0.0 },
                fatExtended = totalFatAmount,
                proteinUpfront =  totalProteinAmount?.let { 0.0 },
                proteinExtended = totalProteinAmount,
                precision = precision,
            )
        }
    }
}