package com.machfour.macros.nutrients

import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.NutrientValue
import com.machfour.macros.entities.Unit
import com.machfour.macros.units.KILOJOULES
import com.machfour.macros.units.NutrientUnits
import com.machfour.macros.units.UnitType

// class storing a set of nutrient values for any purpose.
// It could be for a food or meal, or for a nutrition goal

open class GenericNutrientData<M: NutrientValue<M>>(
    val dataCompleteIfNotNull: Boolean = true
): NutrientData {
    companion object {
        private val Nutrient.index : Int
            get() = id.toInt()
    }

    protected val data: ArrayList<M?> = ArrayList(NumNutrients)
    protected val isDataComplete: Array<Boolean> = Array(NumNutrients) { false }

    // initialise the data (which can't be done inline easily)
    init {
        repeat(NumNutrients) {
            data.add(null)
        }
    }

    // TODO should this be passed in explicitly to show dependence?
    var isImmutable: Boolean = false
        set(value) {
            // cannot make mutable after being set
            if (value) {
                field = true
            }
        }

    val values: List<M>
        get() = data.filterNotNull()

    val valuesExcludingQuantity: List<M>
        get() = values.filter { it.nutrientId != QUANTITY.id }

    // map of protein, fat, saturated fat, carbs, sugar, fibre to amount of energy in calories
    private val energyComponentsMapKj: Map<Nutrient, Double> by lazy {
        makeEnergyComponentsMap(KILOJOULES)
    }

    // map of protein, fat, saturated fat, carbs, sugar, fibre to proportion of total energy
    private val energyProportionsMap: Map<Nutrient, Double> by lazy {
        makeEnergyProportionsMap(KILOJOULES, energyComponentsMapKj)
    }


    override fun equals(other: Any?): Boolean {
        return other is GenericNutrientData<*>
                && data == other.data
                && isDataComplete.contentEquals(other.isDataComplete)
    }

    override fun hashCode(): Int = data.hashCode()

    override fun toString(): String {
        val str = StringBuilder("GenericNutrientData [")
        for (n in AllNutrients) {
            str.append("$n : ${get(n)}, ")
        }
        str.append("]")
        return str.toString()
    }

    // creates a mutable copy
    open fun copy() : GenericNutrientData<M> {
        return GenericNutrientData<M>(dataCompleteIfNotNull).also { copy ->
            for (i in data.indices) {
                copy.data[i] = data[i]
                copy.isDataComplete[i] = isDataComplete[i]
            }
        }
    }

    fun clear() {
        for (i in data.indices) {
            data[i] = null
            isDataComplete[i] = false
        }
    }

    private fun assertMutable() {
        check(!isImmutable) { "NutrientData has been made immutable" }
    }
    
    operator fun get(n: Nutrient): M? = data[n.index]

    override fun amountOf(n: Nutrient, unit: Unit?): Double? {
        return if (unit != null) {
            require(n.compatibleWith(unit)) { "Cannot convert nutrient $n to $unit" }
            this[n]?.convertValueTo(unit)
        } else {
            this[n]?.value
        }
    }

    override fun amountOf(n: Nutrient, unit: Unit?, defaultValue: Double): Double {
        return amountOf(n, unit) ?: defaultValue
    }

    override fun hasNutrient(n: Nutrient) : Boolean {
        return this[n] != null
    }

    operator fun set(n: Nutrient, value: M?) {
        assertMutable()
        data[n.index] = value
        if (dataCompleteIfNotNull) {
            isDataComplete[n.index] = value != null
        }
    }

    override fun hasCompleteData(n: Nutrient) = isDataComplete[n.index]

    internal fun markCompleteData(n: Nutrient, complete: Boolean) {
        isDataComplete[n.index] = complete
    }

    override val foodDensity: Double?
        get() = null

    override fun getUnit(n: Nutrient): Unit? {
        return this[n]?.unit
    }


    override fun getUnit(n: Nutrient, defaultUnits: NutrientUnits) : Unit {
        return getUnit(n) ?: defaultUnits[n]
    }

    // hack for USDA foods
    // subtracts fibre from carbohydrate if necessary to produce a carbohydrate amount
    // If fibre is not present, returns just carbs by diff
    // If there is not enough data to do that, return 0.
    private val carbsBestEffort: Double
        get() = if (hasCompleteData(CARBOHYDRATE)) {
            amountOf(CARBOHYDRATE)!!
        } else if (hasCompleteData(CARBOHYDRATE_BY_DIFF) && hasCompleteData(FIBRE)) {
            amountOf(CARBOHYDRATE_BY_DIFF)!! - amountOf(FIBRE)!!
        } else if (hasCompleteData(CARBOHYDRATE_BY_DIFF)) {
            amountOf(CARBOHYDRATE_BY_DIFF)!!
        } else {
            0.0
        }

    override fun getEnergyProportion(n: Nutrient) : Double {
        if (!energyProportionNutrients.contains(n)) {
            return 0.0
        }
        // the map is computed the first time this function is called
        return energyProportionsMap[n] ?: 0.0
    }

    fun getEnergyAs(unit: Unit) : Double? {
        require(unit.type === UnitType.ENERGY) { "Unit $unit is not an energy unit "}
        return this[ENERGY]?.convertValueTo(unit)
    }
}



