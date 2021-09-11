package com.machfour.macros.core

import com.machfour.macros.entities.Unit

// abstracts Units and servings into a common interface that can be used to enter FoodPortions

interface PortionMeasurement {

    val isVolumeMeasurement: Boolean

    val name: String

    val baseUnit: Unit

    // how many 1 of this 'measurement' is in the base unit
    val unitMultiplier: Double


}

