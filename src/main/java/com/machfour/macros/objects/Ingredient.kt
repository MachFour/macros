package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.validation.Validation
import com.machfour.macros.validation.ValidationError

class Ingredient internal constructor(data: ColumnData<FoodQuantity>, objectSource: ObjectSource)
    : FoodQuantity(data, objectSource) {

    companion object {
        val validation = Validation<FoodQuantity> {
            val parentFoodCol = Schema.FoodQuantityTable.PARENT_FOOD_ID
            HashMap<Column<FoodQuantity, *>, List<ValidationError>>().apply {
                if (!it.hasData(parentFoodCol)) {
                    put(parentFoodCol, listOf(ValidationError.NON_NULL))
                }
            }
        }
    }

    init {
        assert (getData(Schema.FoodQuantityTable.PARENT_FOOD_ID) != null) { "Parent Food ID cannot be null for FoodPortion" }
    }

    /* These are not set on construction, but are only settable once: "pseudo-immutable".
     * This makes it easier to create the objects from the DB.
     */
    // TODO enforce single set
    lateinit var parentFood: Food
        private set

    val parentFoodId: Long
        get() = getData(Schema.FoodQuantityTable.PARENT_FOOD_ID)!!


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

