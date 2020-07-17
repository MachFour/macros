package com.machfour.macros.objects

// abstracts QtyUnits and servings into a common interface that can be used to enter FoodPortions

interface PortionMeasurement {

    val isVolumeMeasurement: Boolean

    // whether this Measurement is actually a serving or a QtyUnit object
    // (dirty, I know)
    val isServing: Boolean

    val name: String

    val baseUnit: QtyUnit

    // how many 1 of this 'measurement' is in the base unit
    val unitMultiplier: Double


}

