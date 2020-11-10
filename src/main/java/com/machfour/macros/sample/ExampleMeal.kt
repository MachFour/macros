package com.machfour.macros.sample

import com.machfour.macros.core.MacrosBuilder
import com.machfour.macros.core.Schema
import com.machfour.macros.objects.Meal
import com.machfour.macros.util.DateStamp
import java.time.Instant

object ExampleMeal {
    val meal = MacrosBuilder(Meal.table).run {
        setField(Schema.MealTable.DAY, DateStamp(2020, 10, 28))
        setField(Schema.MealTable.NAME, "Example meal")
        setField(Schema.MealTable.START_TIME, Instant.now().epochSecond)
        build()
    }
}