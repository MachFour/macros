package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.objects.NutritionCalculations.rescale
import com.machfour.macros.objects.NutritionCalculations.withQuantityUnit
import com.machfour.macros.objects.inbuilt.Units
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
    lateinit var nutritionData: NutritionData
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
        val sb = StringBuilder()
        sb.append(food.mediumName)
        sb.append(", ")
        sb.append(String.format("%.1f", getData(Schema.FoodQuantityTable.QUANTITY)))
        sb.append(qtyUnit.abbr)
        if (serving != null) {
            sb.append(" (").append(servingCountString()).append(" ").append(serving!!.name).append(")")
        }
        if (withNotes) {
            val notes = notes
            if (notes != null && notes.isNotEmpty()) {
                sb.append(" [").append(notes).append("]")
            }
        }
        return sb.toString()
    }

    // we already use polymorphism to check the data is equal for subclasses of MacrosEntity;
    // the only thing that it misses out is checking that o is actually an instance of the subclass.
    override fun equals(other: Any?): Boolean {
        return other is FoodQuantity && super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    fun initFood(f: Food) {
        assert(foreignKeyMatches(this, Schema.FoodQuantityTable.FOOD_ID, f))
        food = f
        var nd = f.getNutritionData().nutrientData
        assert (nd.qtyUnit.type === qtyUnit.type || f.density != null ) {
            "Quantity unit conversion required but food has no density."
        }
        if (nd.qtyUnit != qtyUnit) {
            nd = nd.withQuantityUnit(qtyUnit, f.density ?: 1.0, f.density == null)
        }
        nutritionData = NutritionData(nd.rescale(quantity))
    }

    // for use during construction
    fun initServing(s: Serving) {
        assert(serving == null && foreignKeyMatches(this, Schema.FoodQuantityTable.SERVING_ID, s))
        assert(foodId == s.foodId)
        serving = s
    }

    // returns a string containing the serving count. If the serving count is close to an integer,
    // it is formatted as an integer.
    private fun servingCountString(): String {
        // test if can round
        val intVersion = servingCount().roundToInt()
        return if (intVersion - servingCount() < 0.001) {
            intVersion.toString()
        } else {
            servingCount().toString()
        }
    }

    private fun servingCount(): Double {
        return serving?. let { quantity/ it.quantity } ?: 0.0
    }

}

