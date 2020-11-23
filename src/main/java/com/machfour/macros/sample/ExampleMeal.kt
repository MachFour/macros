package com.machfour.macros.sample

import com.machfour.macros.core.MacrosBuilder
import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.Schema
import com.machfour.macros.objects.FoodPortion
import com.machfour.macros.objects.FoodQuantity
import com.machfour.macros.objects.Meal
import com.machfour.macros.util.DateStamp
import java.time.Instant

object ExampleMeal {
    val foodPortion = MacrosBuilder(FoodQuantity.table).run {
        setField(Schema.FoodQuantityTable.MEAL_ID, MacrosEntity.NO_ID)
        setField(Schema.FoodQuantityTable.FOOD_ID, ExampleFood.food2.id) // who knows what food this is haha
        setField(Schema.FoodQuantityTable.QUANTITY, 100.0)
        setField(Schema.FoodQuantityTable.QUANTITY_UNIT, "g")
        setField(Schema.FoodQuantityTable.NOTES, "This is an example food portion")
        build() as FoodPortion
    }.also {
        it.initFoodAndNd(ExampleFood.food2)
    }
    
    val meal = MacrosBuilder(Meal.table).run {
        setField(Schema.MealTable.DAY, DateStamp(2020, 10, 28))
        setField(Schema.MealTable.NAME, "Example meal")
        build()
    }.also {
        it.addFoodPortion(foodPortion)
    }
}