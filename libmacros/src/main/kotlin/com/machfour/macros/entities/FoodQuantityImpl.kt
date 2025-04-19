package com.machfour.macros.entities

import com.machfour.macros.core.EntityId
import com.machfour.macros.sql.entities.MacrosEntityImpl
import com.machfour.macros.core.ObjectSource
import com.machfour.macros.formatting.toString
import com.machfour.macros.formatting.toStringWithRounding
import com.machfour.macros.nutrients.INutrientValue
import com.machfour.macros.nutrients.NutrientData
import com.machfour.macros.nutrients.Quantity
import com.machfour.macros.sql.Column
import com.machfour.macros.sql.rowdata.RowData
import com.machfour.macros.units.unitWithAbbr

abstract class FoodQuantityImpl<M : FoodQuantityImpl<M, E>, E: INutrientValue> protected constructor(
    data: RowData<M>,
    objectSource: ObjectSource,
    private val foodIdCol: Column.Fk<M, Long, Food>,
    private val servingIdCol: Column.Fk<M, Long, Serving>,
    private val quantityCol: Column<M, Double>,
    /* private val */ quantityUnitCol: Column<M, String>,
    private val notesCol: Column<M, String>,
    private val maxNutrientVersionCol: Column<M, Int>
) : MacrosEntityImpl<M>(data, objectSource), IFoodQuantity<E> {

    /* These are not set on construction, but are only settable once: "pseudo-immutable".
     * This makes it easier to create the objects from the DB.
     */
    override lateinit var food: IFood<E>

    override lateinit var nutrientData: NutrientData<E>

    override val qtyUnit = unitWithAbbr(data[quantityUnitCol]!!)

    // this is the only thing that may remain null after all initialisation is complete
    override var serving: Serving? = null

    override val foodId: Long
        get() = data[foodIdCol]!!

    override val servingId: EntityId?
        get() = data[servingIdCol]

    override val quantity: Double
        get() = data[quantityCol]!!

    override val maxNutrientVersion: Int
        get() = data[maxNutrientVersionCol]!!

    override val notes: String?
        get() = data[notesCol]

   fun prettyFormat(withNotes: Boolean): String {
        return buildString {
            append("${food.mediumName}, ${this@FoodQuantityImpl.quantity.toString(1)}${qtyUnit.abbr}")
            servingString?.let { append(" ($it)") }
            notes?.takeIf { withNotes && it.isNotEmpty() }?.let { append(" [$it]") }
        }
    }

    fun initFoodAndNd(f: IFood<E>) {
        if (f is Food) {
            check(foreignKeyMatches(this, foodIdCol, f as Food))
            food = f
            nutrientData = (f as IFood<E>).nutrientData.rescale(Quantity(amount = quantity, unit = qtyUnit))
        }
    }

    // for use during construction
    fun initServing(s: Serving) {
        check(serving == null && foreignKeyMatches(this, servingIdCol, s))
        check(foodId == s.foodId)
        serving = s
    }

    override val servingString: String?
        get() {
            val s = serving
            return if (s != null) "$servingCountString ${s.name}" else null
        }

    // returns a string containing the serving count. If the serving count is close to an integer,
    // it is formatted as an integer.
    private val servingCountString: String?
        get() = servingCount?.toStringWithRounding()

    override val servingCount: Double?
        get() {
            val s = serving
            return if (s != null) quantity / s.amount else 0.0
        }

}

