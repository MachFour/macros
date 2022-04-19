package com.machfour.macros.jvm

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.entities.Meal
import java.time.Instant

val MacrosEntity<*>.createInstant: Instant
    get() = Instant.ofEpochSecond(createTime)

val MacrosEntity<*>.modifyInstant: Instant
    get() = Instant.ofEpochSecond(modifyTime)

val Meal.startTimeInstant: Instant
    get() = Instant.ofEpochSecond(startTime)


