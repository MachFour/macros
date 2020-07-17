package com.machfour.macros.objects

import com.machfour.macros.core.*

import java.util.Collections

class CompositeFood internal constructor(dataMap: ColumnData<Food>, objectSource: ObjectSource) : Food(dataMap, objectSource) {

    // cached sum of ingredients' nutrition data, combined with any overriding data belonging to this food
    private var ingredientNutritionData: NutritionData? = null
    private var hasOverridingNutritionData: Boolean = false
    private val ingredients: MutableList<Ingredient>

    init {
        assert(FoodType.fromString(dataMap[Schema.FoodTable.FOOD_TYPE]!!) == FoodType.COMPOSITE)

        ingredients = ArrayList()
        ingredientNutritionData = null
        hasOverridingNutritionData = false
    }

    // uses data from the ingredients to add to the existing nutrition data
    private fun calculateIngredientsNutritionData(): NutritionData {
        val nutritionComponents = ArrayList<NutritionData>(ingredients.size)
        for (i in ingredients) {
            nutritionComponents.add(i.nutritionData)
        }
        // don't combine densities of the foods
        return NutritionData.sum(nutritionComponents, false)
    }

    // Sets this Composite food's (overriding) nutrition data
    // TODO call this
    override fun setNutritionData(nd: NutritionData) {
        super.setNutritionData(nd)
        hasOverridingNutritionData = true
    }

    /*
     * TODO save the result of this into the database
     *
     *//*
      NOTE: combined density is estimated using a weighted sum of the densities of the components.
      This is obviously inaccurate if any item does not have the density recorded,
      HOWEVER ALSO, density of foods will more often than not change during preparation
      (e.g dry ingredients absorbing moisture).
      So really, it probably doesn't make sense to propagate the combined ingredients density value
     */
    override fun getNutritionData(): NutritionData {
        if (ingredientNutritionData == null) {
            ingredientNutritionData = calculateIngredientsNutritionData()
        }

        if (hasOverridingNutritionData) {
            // combine missing data from the foods nData with the overriding data
            val overridingData = super.getNutritionData()
            return NutritionData.combine(overridingData, ingredientNutritionData!!)
        } else {
            return ingredientNutritionData!!
        }
    }

    fun getIngredients(): List<Ingredient> {
        return Collections.unmodifiableList(ingredients)
    }

    fun addIngredient(i: Ingredient) {
        assert(!ingredients.contains(i) && foreignKeyMatches(i, Schema.IngredientTable.COMPOSITE_FOOD_ID, this))
        ingredients.add(i)
        // sort by ID ~> attempt to keep same order as entered by user or imported
        // note - this is essentially an insertion sort, pretty slow, but most foods shouldn't have too many ingredients
        ingredients.sortBy { it.id }
    }

}
