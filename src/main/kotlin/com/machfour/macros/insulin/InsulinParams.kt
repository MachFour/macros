package com.machfour.macros.insulin

// Class used to hold parameters relating to calculation of insulin dosages from nutrition data

data class InsulinParams(val icRatio: Double, val proteinFactor: Double) {
    constructor(icRatio: Float, proteinFactor: Float): this(icRatio.toDouble(), proteinFactor.toDouble())

    // direct conversion from protein to insulin
    val ipRatio: Double = icRatio * proteinFactor
}