package com.machfour.macros.objects

import com.machfour.macros.core.*

import java.util.Collections

class CompositeFood internal constructor(dataMap: ColumnData<Food>, objectSource: ObjectSource) : Food(dataMap, objectSource) {

    // cached sum of ingredients' nutrition data, combined with any overriding data belonging to this food
    private var ingredientNutrientData = NutrientData()
    private var hasOverridingNutrientData: Boolean = false
    private val ingredients: MutableList<Ingredient> = ArrayList()

    private var ingredientsNutrientDataNeedsUpdate = false

    init {
        assert(FoodType.fromString(dataMap[Schema.FoodTable.FOOD_TYPE]!!) == FoodType.COMPOSITE)
    }

    // add overriding nutrient value
    override fun addNutrientValue(nv: NutrientValue) {
        hasOverridingNutrientData = true
        super.addNutrientValue(nv)
    }

    private fun updateIngredientsNutrientData() {
        // don't combine densities of the foods
        ingredientNutrientData = NutrientData.sum(ingredients.map { it.nutrientData })
        ingredientsNutrientDataNeedsUpdate = false
    }

    /*
     * TODO save the result of this into the database?
     *
     *//*
      NOTE: combined density is estimated using a weighted sum of the densities of the components.
      This is obviously inaccurate if any item does not have the density recorded,
      HOWEVER ALSO, density of foods will more often than not change during preparation
      (e.g dry ingredients absorbing moisture).
      So really, it probably doesn't make sense to propagate the combined ingredients density value
     */
    override val nutrientData: NutrientData
        get() {
        // cache mutable property value

        if (ingredientsNutrientDataNeedsUpdate) {
            updateIngredientsNutrientData()
        }

        return if (hasOverridingNutrientData) {
            // combine missing data from the foods nData with the overriding data
            val overridingData = super.nutrientData
            overridingData.fillMissingData(ingredientNutrientData)
        } else {
            ingredientNutrientData
        }
    }

    fun getIngredients(): List<Ingredient> {
        return Collections.unmodifiableList(ingredients)
    }

    fun addIngredient(i: Ingredient) {
        assert(!ingredients.contains(i) && foreignKeyMatches(i, Schema.IngredientTable.PARENT_FOOD_ID, this))
        ingredients.add(i)
        // sort by ID ~> attempt to keep same order as entered by user or imported
        // note - this is essentially an insertion sort, pretty slow, but most foods shouldn't have too many ingredients
        ingredients.sortBy { it.id }
        ingredientsNutrientDataNeedsUpdate = true
    }

}
