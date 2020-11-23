package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.core.NutritionCalculations.rescale
import com.machfour.macros.core.NutritionCalculations.withQuantityUnit
import com.machfour.macros.objects.inbuilt.Units
import kotlin.math.abs
import kotlin.math.roundToInt

open class FoodQuantity internal constructor(data: ColumnData<FoodQuantity>, objectSource: ObjectSource)
    : MacrosEntityImpl<FoodQuantity>(data, objectSource) {

    companion object {
        val factory: Factory<FoodQuantity> = Factory {
            dataMap, objectSource ->
            val parentFoodId = dataMap[Schema.FoodQuantityTable.PARENT_FOOD_ID]
            val mealId = dataMap[Schema.FoodQuantityTable.MEAL_ID]
            require((parentFoodId != null) xor (mealId != null)) { "Exactly one of mealId and parentFoodId must be defined" }
            when {
                parentFoodId != null -> Ingredient(dataMap, objectSource)
                mealId != null -> FoodPortion(dataMap, objectSource)
                else -> throw IllegalArgumentException("Both meal id and parent food id are null")
            }
        }
        val table: Table<FoodQuantity>
            get() = Schema.FoodQuantityTable.instance
    }

    /* These are not set on construction, but are only settable once: "pseudo-immutable".
     * This makes it easier to create the objects from the DB.
     */
    lateinit var food: Food
        private set
    lateinit var nutrientData: NutrientData
        private set
    val qtyUnit = Units.fromAbbreviation(data[Schema.FoodQuantityTable.QUANTITY_UNIT]!!)

    // this is the only thing that may remain null after all initialisation is complete
    var serving: Serving? = null
        private set


    override val table: Table<FoodQuantity>
        get() = Companion.table
    override val factory: Factory<FoodQuantity>
        get() = Companion.factory

    val foodId: Long
        get() = getData(Schema.FoodQuantityTable.FOOD_ID)!!

    val servingId: Long?
        get() = getData(Schema.FoodQuantityTable.SERVING_ID)

    val quantity: Double
        get() = getData(Schema.FoodQuantityTable.QUANTITY)!!

    val notes: String?
        get() = getData(Schema.FoodQuantityTable.NOTES)

    fun prettyFormat(withNotes: Boolean): String {
        val quantityStr = "%.1f".format(quantity)
        return buildString {
            append("${food.mediumName}, ${quantityStr}${qtyUnit.abbr}")
            servingString?.let { append(" ($it)") }
            notes?.takeIf { withNotes && it.isNotEmpty() }?.let { append(" [$it]") }
        }
    }

    // we already use polymorphism to check the data is equal for subclasses of MacrosEntity;
    // the only thing that it misses out is checking that o is actually an instance of the subclass.
    override fun equals(other: Any?): Boolean {
        return other is FoodQuantity && super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    fun initFoodAndNd(f: Food) {
        assert(foreignKeyMatches(this, Schema.FoodQuantityTable.FOOD_ID, f))
        food = f
        nutrientData = f.nutrientData.let {
            if (it.qtyUnit != qtyUnit) {
                it.withQuantityUnit(qtyUnit, f.density, allowDefaultDensity = true)
            } else {
                it
            }
        }.rescale(quantity)
    }

    // for use during construction
    fun initServing(s: Serving) {
        assert(serving == null && foreignKeyMatches(this, Schema.FoodQuantityTable.SERVING_ID, s))
        assert(foodId == s.foodId)
        serving = s
    }

    val servingString: String?
        get() = serving?.let { "$servingCountString ${it.name}" }

    // returns a string containing the serving count. If the serving count is close to an integer,
    // it is formatted as an integer.
    private val servingCountString: String
        get() {
            // test if can round
            servingCount.let {
                val asInt = it.roundToInt()
                return (if (abs(it - asInt) < 0.001) asInt else it).toString()
            }
        }

    val servingCount: Double
        get() = serving?.let { quantity/ it.quantity } ?: 0.0

}

