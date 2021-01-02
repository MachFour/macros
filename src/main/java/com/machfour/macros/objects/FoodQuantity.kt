package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.objects.inbuilt.Units
import kotlin.math.abs
import kotlin.math.roundToInt

abstract class FoodQuantity<M: FoodQuantity<M>> protected constructor(
    data: ColumnData<M>,
    objectSource: ObjectSource,
    private val foodIdCol: Column.Fk<M, Long, Food>,
    private val servingIdCol: Column.Fk<M, Long, Serving>,
    private val quantityCol: Column<M, Double>,
    private val quantityUnitCol: Column<M, String>,
    private val notesCol: Column<M, String>,
    private val maxNutrientVersionCol: Column<M, Long>
)
    : MacrosEntityImpl<M>(data, objectSource) {

    /* These are not set on construction, but are only settable once: "pseudo-immutable".
     * This makes it easier to create the objects from the DB.
     */
    lateinit var food: Food
        private set
    lateinit var nutrientData: NutrientData
        private set

    val qtyUnit = Units.fromAbbreviation(data[quantityUnitCol]!!)

    // this is the only thing that may remain null after all initialisation is complete
    var serving: Serving? = null
        private set

    val foodId: Long
        get() = getData(foodIdCol)!!

    val servingId: Long?
        get() = getData(servingIdCol)

    val quantity: Double
        get() = getData(quantityCol)!!

    val maxNutrientVersion: Long
        get() = getData(maxNutrientVersionCol)!!

    val notes: String?
        get() = getData(notesCol)

    fun prettyFormat(withNotes: Boolean): String {
        val quantityStr = "%.1f".format(quantity)
        return buildString {
            append("${food.mediumName}, ${quantityStr}${qtyUnit.abbr}")
            servingString?.let { append(" ($it)") }
            notes?.takeIf { withNotes && it.isNotEmpty() }?.let { append(" [$it]") }
        }
    }

    fun initFoodAndNd(f: Food) {
        assert(foreignKeyMatches(this, foodIdCol, f))
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
        assert(serving == null && foreignKeyMatches(this, servingIdCol, s))
        assert(foodId == s.foodId)
        serving = s
    }

    val servingString: String?
        get() {
            val s = serving
            return if (s != null) "$servingCountString ${s.name}" else null
        }

    // returns a string containing the serving count. If the serving count is close to an integer,
    // it is formatted as an integer.
    private val servingCountString: String
        // test if can round
        get() {
            val c = servingCount
            val asInt = c.roundToInt()
            val error = c - asInt
            return if (abs(error) < 0.001) {
                asInt.toString()
            } else {
                "%.2f".format(c)
            }
        }

    val servingCount: Double
        get() {
            val s = serving
            return if (s != null) quantity / s.quantity else 0.0
        }

}

