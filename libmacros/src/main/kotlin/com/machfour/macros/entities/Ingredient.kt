@file:Suppress("EqualsOrHashCode")

package com.machfour.macros.entities

import com.machfour.macros.core.EntityId
import com.machfour.macros.core.FoodType
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.schema.IngredientTable
import com.machfour.macros.sql.Table
import com.machfour.macros.sql.entities.Factories
import com.machfour.macros.sql.entities.Factory
import com.machfour.macros.sql.rowdata.RowData

class Ingredient internal constructor(data: RowData<Ingredient>, objectSource: ObjectSource
) : FoodQuantityImpl<Ingredient, FoodNutrientValue> (
    data,
    objectSource,
    IngredientTable.FOOD_ID,
    IngredientTable.SERVING_ID,
    IngredientTable.QUANTITY,
    IngredientTable.QUANTITY_UNIT,
    IngredientTable.NOTES,
    IngredientTable.NUTRIENT_MAX_VERSION,
), IFoodQuantity<FoodNutrientValue> {

    companion object {
        val factory: Factory<Ingredient, Ingredient>
            get() = Factories.ingredient

    }

    override fun getTable(): Table<*, Ingredient> {
        return IngredientTable
    }

    /* These are not set on construction, but are only settable once: "pseudo-immutable".
     * This makes it easier to create the objects from the DB.
     */
    // TODO enforce single set
    lateinit var parentFood: Food
        private set

    val parentFoodId: EntityId
        get() = data[IngredientTable.PARENT_FOOD_ID]!!


    // we already use polymorphism to check the data is equal for subclasses of MacrosEntity;
    // the only thing that it misses out is checking that o is actually an instance of the subclass.
    override fun equals(other: Any?): Boolean {
        return other is Ingredient && super.equals(other)
    }

    fun initCompositeFood(f: Food) {
        check(f is CompositeFood && f.foodType === FoodType.COMPOSITE)
        check(parentFoodId == f.id)
        parentFood = f
    }


}

