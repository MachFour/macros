package com.machfour.macros.entities

import com.machfour.macros.core.FoodType
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.nutrients.NutrientData
import com.machfour.macros.schema.FoodTable
import com.machfour.macros.schema.IngredientTable
import com.machfour.macros.sql.rowdata.RowData

typealias CompositeFood = CompositeFoodImpl

// don't need hashcode override since equals implies super.equals true, so hashcode will match
@Suppress("EqualsOrHashCode")
class CompositeFoodImpl private constructor(dataMap: RowData<Food>, objectSource: ObjectSource) : Food(dataMap, objectSource) {

    companion object {
        internal fun new(dataMap: RowData<Food>, objectSource: ObjectSource) : CompositeFoodImpl {
            require(FoodType.fromString(dataMap[FoodTable.FOOD_TYPE]!!) == FoodType.COMPOSITE)
            return CompositeFoodImpl(dataMap, objectSource)
        }
    }

    // cached sum of ingredients' nutrition data, combined with any overriding data belonging to this food
    private var ingredientNutrientData = FoodNutrientData()
    private var hasOverridingNutrientData: Boolean = false
    private val mutableIngredients = ArrayList<Ingredient>()

    private var ingredientsNutrientDataNeedsUpdate = false

    // add overriding nutrient value
    override fun addNutrientValue(nv: FoodNutrientValue) {
        hasOverridingNutrientData = true
        super.addNutrientValue(nv)
    }

    private fun updateIngredientsNutrientData() {
        // don't combine densities of the foods
        ingredientNutrientData = FoodNutrientData.sum(mutableIngredients.map { it.nutrientData })
        ingredientsNutrientDataNeedsUpdate = false
    }

    /*
      TODO save the result of this into the database?

      NOTE: combined density is estimated using a weighted sum of the densities of the components.
      This is obviously inaccurate if any item does not have the density recorded,
      HOWEVER ALSO, density of foods will more often than not change during preparation
      (e.g dry ingredients absorbing moisture).
      So really, it probably doesn't make sense to propagate the combined ingredients density value
     */
    override val nutrientData: NutrientData<FoodNutrientValue>
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

    val ingredients: List<Ingredient>
        get() = mutableIngredients // should be immutable

    fun addIngredient(i: Ingredient) {
        check(!mutableIngredients.contains(i) && foreignKeyMatches(i, IngredientTable.PARENT_FOOD_ID, this))
        mutableIngredients.add(i)
        // sort by ID ~> attempt to keep same order as entered by user or imported
        // note - this is essentially an insertion sort, pretty slow, but most foods shouldn't have too many ingredients
        mutableIngredients.sortBy { it.id.value }
        ingredientsNutrientDataNeedsUpdate = true
    }

    override fun equals(other: Any?): Boolean {
        return other is CompositeFood
                && super.equals(other)
                && ingredients == other.ingredients
    }
}
