package com.machfour.macros.nutrients

import com.machfour.macros.entities.INutrient
import com.machfour.macros.entities.Nutrient
import com.machfour.macros.entities.NutrientValue
import com.machfour.macros.entities.Unit
import com.machfour.macros.nutrients.Quantity.Companion.NullQuantity
import com.machfour.macros.nutrients.Quantity.Companion.toQuantity
import com.machfour.macros.units.KILOJOULES
import com.machfour.macros.units.UnitType

private val INutrient.index : Int
    get() = id.value.toInt()


private fun <M: NutrientValue<M>> initData(data: Map<INutrient, M>): MutableList<M?> {
    val list = MutableList<M?>(AllNutrients.size) { null }
    for ((n, value) in data) {
        list[n.index] = value
    }
    return list
}

private fun initIncompleteList(incompleteSet: Set<INutrient>): MutableList<Boolean> {
    val list = MutableList(AllNutrients.size) { false }
    for (n in incompleteSet) {
        list[n.index] = true
    }
    return list
}

// Class storing a set of nutrient values for any purpose.
// It could be for a food or meal, or for a nutrition goal
abstract class GenericNutrientData<M: NutrientValue<M>>(
    nutrients: Map<INutrient, M> = emptyMap(),
    incompleteNutrients: Set<INutrient> = emptySet(),
): BasicNutrientData<M> {
    protected val data: MutableList<M?> = initData(nutrients)
    protected val isDataIncomplete = initIncompleteList(incompleteNutrients)

    override val perQuantity: IQuantity
        get() = (get(QUANTITY)?.toQuantity() ?: NullQuantity)

    override fun getValueOrNull(n: INutrient): M? {
        return get(n)
    }

    override fun nutrientValues(): Map<INutrient, M> {
        return buildMap {
            for (value in data) {
                if (value != null) {
                    put(value.nutrient, value)
                }
            }
        }
    }

    val values: List<M>
        get() = data.filterNotNull()

    val valuesExcludingQuantity: List<M>
        get() = values.filter { it.nutrientId != QUANTITY.id }

    // map of protein, fat, saturated fat, carbs, sugar, fibre to amount of energy in calories
    private val energyComponentsMapKj: Map<INutrient, Double> by lazy {
        makeEnergyComponentsMap(KILOJOULES)
    }

    // map of protein, fat, saturated fat, carbs, sugar, fibre to proportion of total energy
    private val energyProportionsMap: Map<INutrient, Double> by lazy {
        makeEnergyProportionsMap(KILOJOULES, energyComponentsMapKj)
    }


    override fun equals(other: Any?): Boolean {
        return other is GenericNutrientData<*>
                && data == other.data
                && isDataIncomplete == other.isDataIncomplete
    }

    override fun hashCode(): Int = data.hashCode()

    override fun toString(): String {
        return buildString {
            append("GenericNutrientData [")
            for (n in AllNutrients) {
                append("$n : ${get(n)}, ")
            }
            append("]")
        }
    }

    fun clear() {
        for (i in data.indices) {
            data[i] = null
        }
    }

    operator fun get(n: INutrient): M? = data[n.index]

    override fun hasNutrient(n: INutrient) : Boolean {
        return this[n] != null
    }

    operator fun set(n: Nutrient, value: M?) {
        data[n.index] = value
    }

    override fun hasIncompleteData(n: INutrient) = isDataIncomplete[n.index]

    override fun incompleteDataNutrients(): Set<INutrient> {
        return AllNutrients.filter { isDataIncomplete[it.index] }.toSet()
    }

    internal fun markIncompleteData(n: Nutrient, isIncomplete: Boolean = true) {
        isDataIncomplete[n.index] = isIncomplete
    }

    override val foodDensity: Double?
        get() = null

    override fun getUnit(n: INutrient): Unit? {
        return this[n]?.unit
    }

    // hack for USDA foods
    // subtracts fibre from carbohydrate if necessary to produce a carbohydrate amount
    // If fibre is not present, returns just carbs by diff
    // If there is not enough data to do that, return 0.
    private val carbsBestEffort: Double
        get() = if (!hasNutrient(CARBOHYDRATE)) {
            amountOf(CARBOHYDRATE)!!
        } else if (!hasNutrient(CARBOHYDRATE_BY_DIFF) && !hasNutrient(FIBRE)) {
            amountOf(CARBOHYDRATE_BY_DIFF)!! - amountOf(FIBRE)!!
        } else if (!hasNutrient(CARBOHYDRATE_BY_DIFF)) {
            amountOf(CARBOHYDRATE_BY_DIFF)!!
        } else {
            0.0
        }

    override fun getEnergyProportion(n: INutrient) : Double {
        // the map is computed the first time this function is called
        return energyProportionsMap[n] ?: 0.0
    }

    fun getEnergyAs(unit: Unit) : Double? {
        require(unit.type === UnitType.ENERGY) { "Unit $unit is not an energy unit "}
        return this[ENERGY]?.convertValueTo(unit)
    }
}



