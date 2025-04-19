package com.machfour.macros.sql.entities

import com.machfour.macros.core.MacrosEntity
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.AttrMapping
import com.machfour.macros.entities.FoodCategory
import com.machfour.macros.entities.FoodNutrientValue
import com.machfour.macros.entities.FoodPortion
import com.machfour.macros.entities.IFoodPortion
import com.machfour.macros.entities.INutrient
import com.machfour.macros.entities.IServing
import com.machfour.macros.entities.Ingredient
import com.machfour.macros.entities.Meal
import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.NutrientGoal
import com.machfour.macros.entities.NutrientGoalDayMapping
import com.machfour.macros.entities.NutrientGoalValue
import com.machfour.macros.entities.Serving
import com.machfour.macros.entities.Unit
import com.machfour.macros.nutrients.INutrientValue
import com.machfour.macros.sql.rowdata.RowData
import com.machfour.macros.sql.rowdata.foodNutrientValueToRowData
import com.machfour.macros.sql.rowdata.foodPortionToRowData
import com.machfour.macros.sql.rowdata.ingredientToRowData
import com.machfour.macros.sql.rowdata.mealToRowData
import com.machfour.macros.sql.rowdata.nutrientToRowData
import com.machfour.macros.sql.rowdata.servingToRowData

// Contains factories for the different objects
// They're here because putting them in the same file as the object causes static initialisation order issues


// TODO make factories lazy inside each class and make object constructors private

object Factories {

    private fun <I: MacrosEntity, M: I> defaultFactory(
        construct: (RowData<M>, ObjectSource) -> M,
        deconstruct: (I) -> RowData<M>
    ) : Factory<I, M> {
        return object: Factory<I, M> {
            override fun construct(data: RowData<M>, source: ObjectSource): M {
                data.makeImmutable()
                return construct(data, source)
            }

            override fun deconstruct(obj: I): RowData<M> {
                return deconstruct(obj)
            }
        }
    }

    val attributeMapping: Factory<AttrMapping, AttrMapping>
        = defaultFactory(::AttrMapping) { it.data }

    val foodCategory: Factory<FoodCategory, FoodCategory>
        = defaultFactory(::FoodCategory) { it.data }

    val serving: Factory<IServing, Serving>
        = defaultFactory(::Serving) { servingToRowData(it) }

    val meal: Factory<Meal, Meal>
        = defaultFactory(::Meal) { mealToRowData(it) }

    val foodPortion: Factory<IFoodPortion<*>, FoodPortion>
        = defaultFactory(::FoodPortion) { foodPortionToRowData(it) }

    val ingredient: Factory<Ingredient, Ingredient>
        = defaultFactory(::Ingredient) { ingredientToRowData(it) }

    val foodNutrientValue: Factory<INutrientValue, FoodNutrientValue>
        = defaultFactory(::FoodNutrientValue) { foodNutrientValueToRowData(it) }

    val nutrientGoal: Factory<NutrientGoal, NutrientGoal>
        = defaultFactory(::NutrientGoal) { it.data }

    val nutrientGoalDayMapping: Factory<NutrientGoalDayMapping, NutrientGoalDayMapping>
        = defaultFactory(::NutrientGoalDayMapping) { it.data }

    val nutrientGoalValue: Factory<INutrientValue, NutrientGoalValue>
        = defaultFactory(::NutrientGoalValue) { TODO() }

    val nutrient: Factory<INutrient, Nutrient>
        = defaultFactory(::Nutrient) { nutrientToRowData(it) }

    val unit: Factory<Unit, Unit>
        = defaultFactory(::Unit) { it.data }
}