package com.machfour.macros.objects

import com.machfour.macros.core.*
import kotlin.math.roundToInt

class Ingredient private constructor(data: ColumnData<Ingredient>, objectSource: ObjectSource)
    : MacrosEntityImpl<Ingredient>(data, objectSource) {

    /* These are not set on construction, but are only settable once: "pseudo-immutable".
     * This makes it easier to create the objects from the DB.
     */
    // TODO enforce single set
    lateinit var compositeFood: Food
        private set
    lateinit var ingredientFood: Food
        private set

    // not null only after setIngredientFood() is called
    lateinit var nutritionData: NutritionData
        private set

    val qtyUnit = QtyUnits.fromAbbreviation(data[Schema.IngredientTable.QUANTITY_UNIT]!!)

    // this is the only thing that may remain null after all initialisation is complete
    var serving: Serving? = null
        private set

    override val factory: Factory<Ingredient>
        get() = Companion.factory

    override val table: Table<Ingredient>
        get() = Companion.table

    val compositeFoodId: Long
        get() = getData(Schema.IngredientTable.COMPOSITE_FOOD_ID)!!

    val ingredientFoodId: Long
        get() = getData(Schema.IngredientTable.INGREDIENT_FOOD_ID)!!

    val servingId: Long?
        get() = getData(Schema.IngredientTable.SERVING_ID)

    val notes: String?
        get() = getData(Schema.IngredientTable.NOTES)

    // we already use polymorphism to check the data is equal for subclasses of MacrosEntity;
    // the only thing that it misses out is checking that o is actually an instance of the subclass.
    override fun equals(other: Any?): Boolean {
        return other is Ingredient && super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    fun qtyUnit(): QtyUnit {
        return qtyUnit
    }

    val qtyUnitAbbr: String
        get() = getData(Schema.IngredientTable.QUANTITY_UNIT)!!

    fun initCompositeFood(f: Food) {
        assert(f is CompositeFood && f.foodType === FoodType.COMPOSITE)
        assert(compositeFoodId == f.id)
        compositeFood = f
    }


    fun initIngredientFood(f: Food) {
        assert(foreignKeyMatches(this, Schema.IngredientTable.INGREDIENT_FOOD_ID, f))
        ingredientFood = f
        nutritionData = NutritionCalculations.rescale(f.getNutritionData(), quantity(), qtyUnit())
    }

    fun initServing(s: Serving) {
        assert(serving == null && foreignKeyMatches(this, Schema.IngredientTable.SERVING_ID, s))
        assert(ingredientFoodId == s.foodId)
        serving = s
    }

    fun quantity(): Double {
        return getData(Schema.IngredientTable.QUANTITY)!!
    }

    // returns a string containing the serving count. If the serving count is close to an integer,
    // it is formatted as an integer.
    fun servingCountString(): String {
        // test if can round
        val intVer = servingCount.roundToInt()
        return if (intVer - servingCount < 0.001) {
            intVer.toString()
        } else {
            servingCount.toString()
        }
    }

    val servingCount: Double
        get() = serving?.let { quantity() / it.quantity} ?: 0.0

    companion object {
        // factory must come before table due to static initialisation order
        val factory = Factory<Ingredient> { dataMap, objectSource -> Ingredient(dataMap, objectSource) }

        val table: Table<Ingredient>
            get() = Schema.IngredientTable.instance
    }
}

