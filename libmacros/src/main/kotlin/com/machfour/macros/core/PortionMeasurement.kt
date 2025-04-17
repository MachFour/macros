package com.machfour.macros.core

import com.machfour.macros.entities.Unit

// abstracts Units and servings into a common interface that can be used to enter FoodPortions

interface PortionMeasurement {
    val name: String

    val amount: Double

    val unit: Unit

    val isVolumeMeasurement: Boolean
        get() = unit.isVolumeMeasurement
}

