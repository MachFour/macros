package com.machfour.macros.entities

import com.machfour.macros.core.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.formatting.toString
import com.machfour.macros.formatting.toStringWithRounding
import com.machfour.macros.nutrients.FoodNutrientData
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.RowData
import com.machfour.macros.units.unitWithAbbr

abstract class FoodQuantity<M : FoodQuantity<M>> protected constructor(
    data: RowData<M>,
    objectSource: ObjectSource,
    private val foodIdCol: Column.Fk<M, Long, Food>,
    private val servingIdCol: Column.Fk<M, Long, Serving>,
    private val quantityCol: Column<M, Double>,
    /* private val */ quantityUnitCol: Column<M, String>,
    private val notesCol: Column<M, String>,
    private val maxNutrientVersionCol: Column<M, Int>
) : MacrosEntityImpl<M>(data, objectSource) {

    /* These are not set on construction, but are only settable once: "pseudo-immutable".
     * This makes it easier to create the objects from the DB.
     */
    lateinit var food: Food
        private set
    lateinit var nutrientData: FoodNutrientData
        private set

    val qtyUnit = unitWithAbbr(data[quantityUnitCol]!!)

    // this is the only thing that may remain null after all initialisation is complete
    var serving: Serving? = null
        private set

    val foodId: Long
        get() = data[foodIdCol]!!

    val servingId: Long?
        get() = data[servingIdCol]

    val quantity: Double
        get() = data[quantityCol]!!

    val maxNutrientVersion: Int
        get() = data[maxNutrientVersionCol]!!

    val notes: String?
        get() = data[notesCol]

    fun prettyFormat(withNotes: Boolean): String {
        return buildString {
            append("${food.mediumName}, ${this@FoodQuantity.quantity.toString(1)}${qtyUnit.abbr}")
            servingString?.let { append(" ($it)") }
            notes?.takeIf { withNotes && it.isNotEmpty() }?.let { append(" [$it]") }
        }
    }

    fun initFoodAndNd(f: Food) {
        check(foreignKeyMatches(this, foodIdCol, f))
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
        check(serving == null && foreignKeyMatches(this, servingIdCol, s))
        check(foodId == s.foodId)
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
        get() = servingCount.toStringWithRounding()

    val servingCount: Double
        get() {
            val s = serving
            return if (s != null) quantity / s.quantity else 0.0
        }

}

