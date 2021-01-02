package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.objects.helpers.Factories

class Ingredient internal constructor(data: ColumnData<Ingredient>, objectSource: ObjectSource
) : FoodQuantity<Ingredient>(
    data,
    objectSource,
    Schema.IngredientTable.FOOD_ID,
    Schema.IngredientTable.SERVING_ID,
    Schema.IngredientTable.QUANTITY,
    Schema.IngredientTable.QUANTITY_UNIT,
    Schema.IngredientTable.NOTES,
    Schema.IngredientTable.NUTRIENT_MAX_VERSION,
) {

    companion object {
        val factory: Factory<Ingredient>
            get() = Factories.ingredient

        val table: Table<Ingredient>
            get() = Schema.IngredientTable.instance
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
        get() = getData(Schema.IngredientTable.PARENT_FOOD_ID)!!


    // we already use polymorphism to check the data is equal for subclasses of MacrosEntity;
    // the only thing that it misses out is checking that o is actually an instance of the subclass.
    override fun equals(other: Any?): Boolean {
        return other is Ingredient && super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    fun initCompositeFood(f: Food) {
        assert(f is CompositeFood && f.foodType === FoodType.COMPOSITE)
        assert(parentFoodId == f.id)
        parentFood = f
    }


}

