package com.machfour.macros.entities.auxiliary

import com.machfour.macros.core.Factory
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.*
import com.machfour.macros.entities.Unit
import com.machfour.macros.sql.rowdata.*

// Contains factories for the different objects
// They're here because putting them in the same file as the object causes static initialisation order issues


// TODO make factories lazy inside each class and make object constructors private

object Factories {

    private fun <M> defaultFactory(
        construct: (RowData<M>, ObjectSource) -> M,
        deconstruct: (M) -> RowData<M>,
    ) : Factory<M> {
        return object: Factory<M> {
            override fun construct(data: RowData<M>, source: ObjectSource): M {
                data.makeImmutable()
                return construct(data, source)
            }

            override fun deconstruct(obj: M): RowData<M> {
                return deconstruct(obj)
            }
        }
    }

    val attributeMapping = defaultFactory(::AttrMapping) { TODO() }

    val foodCategory = defaultFactory(::FoodCategory) { TODO() }

    val serving = defaultFactory(::Serving) { servingToRowData(it) }

    val meal = defaultFactory(::Meal) { mealToRowData(it) }

    val foodPortion = defaultFactory(::FoodPortion) { foodPortionToRowData(it) }

    val ingredient = defaultFactory(::Ingredient) { ingredientToRowData(it) }

    val foodNutrientValue = defaultFactory(::FoodNutrientValue) { foodNutrientValueToRowData(it) }

    val nutrientGoal = defaultFactory(::NutrientGoal) { TODO() }

    val nutrientGoalDayMapping = defaultFactory(::NutrientGoalDayMapping) { TODO() }

    val nutrientGoalValue = defaultFactory(::NutrientGoalValue) { TODO() }

    val nutrient = defaultFactory(::Nutrient) { nutrientToRowData(it) }

    val unit = defaultFactory(::Unit) { TODO() }

}