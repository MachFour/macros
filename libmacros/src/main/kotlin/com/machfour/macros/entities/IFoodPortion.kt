package com.machfour.macros.entities

import com.machfour.macros.core.EntityId
import com.machfour.macros.nutrients.INutrientValue

interface IFoodPortion<E: INutrientValue>: IFoodQuantity<E> {

    val mealId: EntityId
}