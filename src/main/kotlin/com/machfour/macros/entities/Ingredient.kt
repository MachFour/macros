@file:Suppress("EqualsOrHashCode")

package com.machfour.macros.entities

import com.machfour.macros.core.Factory
import com.machfour.macros.core.FoodType
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.entities.auxiliary.Factories
import com.machfour.macros.schema.IngredientTable
import com.machfour.macros.sql.RowData
import com.machfour.macros.sql.Table

class Ingredient internal constructor(data: RowData<Ingredient>, objectSource: ObjectSource
) : FoodQuantity<Ingredient>(
    data,
    objectSource,
    IngredientTable.FOOD_ID,
    IngredientTable.SERVING_ID,
    IngredientTable.QUANTITY,
    IngredientTable.QUANTITY_UNIT,
    IngredientTable.NOTES,
    IngredientTable.NUTRIENT_MAX_VERSION,
) {

    companion object {
        val factory: Factory<Ingredient>
            get() = Factories.ingredient

        val table: Table<Ingredient>
            get() = IngredientTable
    }

    override val table: Table<Ingredient>
        get() = Companion.table
    override val factory: Factory<Ingredient>
        get() = Companion.factory


    /* These are not set on construction, but are only settable once: "pseudo-immutable".
     * This makes it easier to create the objects from the DB.
     */
    // TODO enforce single set
    lateinit var parentFood: Food
        private set

    val parentFoodId: Long
        get() = data[IngredientTable.PARENT_FOOD_ID]!!


    // we already use polymorphism to check the data is equal for subclasses of MacrosEntity;
    // the only thing that it misses out is checking that o is actually an instance of the subclass.
    override fun equals(other: Any?): Boolean {
        return other is Ingredient && super.equals(other)
    }

    fun initCompositeFood(f: Food) {
        assert(f is CompositeFood && f.foodType === FoodType.COMPOSITE)
        assert(parentFoodId == f.id)
        parentFood = f
    }


}

