package com.machfour.macros.core

import com.machfour.macros.core.NutritionCalculations.withQuantityUnit
import com.machfour.macros.objects.Nutrient
import com.machfour.macros.objects.NutrientValue
import com.machfour.macros.objects.Unit
import com.machfour.macros.objects.UnitType
import com.machfour.macros.objects.inbuilt.DefaultUnits
import com.machfour.macros.objects.inbuilt.Nutrients
import com.machfour.macros.objects.inbuilt.Units

// immutable class storing nutrition data for a food or meal
class NutrientData(
    val dataCompleteIfNotNull: Boolean = true
) {

    private val data: Array<NutrientValue?> = arrayOfNulls(Nutrients.numNutrients)
    private val isDataComplete: Array<Boolean> = Array(Nutrients.numNutrients) { false }

    // TODO should this be passed in explicitly to show dependence?
    var isImmutable: Boolean = false
        private set
    fun setImmutable() {
        this.isImmutable = true
    }

    var quantityObj: NutrientValue
        get() {
            val quantityValue = this[Nutrients.QUANTITY]
            checkNotNull(quantityValue) { "Error - quantity value not initialised" }
            return quantityValue
        }
        set(value) {
            this[Nutrients.QUANTITY] = value
        }

    val qtyUnit: Unit
        get() = quantityObj.unit

    val quantity: Double
        get() = quantityObj.value

    val qtyUnitAbbr: String
        get() = quantityObj.unit.abbr

    val nutrientValues: List<NutrientValue>
        get() {
            return data.filterNotNull()
        }

    val nutrientValuesExcludingQuantity: List<NutrientValue>
        get() {
            return nutrientValues.filter { it !== quantityObj }
        }

    // map of protein, fat, saturated fat, carbs, sugar, fibre to proportion of total energy
    private val energyProportionsMap: Map<Nutrient, Double> by lazy {
        NutritionCalculations.makeEnergyProportionsMap(this)
    }
    // map of protein, fat, saturated fat, carbs, sugar, fibre to amount of energy
    private val energyComponentsMap: Map<Nutrient, Double> by lazy {
        NutritionCalculations.makeEnergyComponentsMap(this, Units.CALORIES)
    }


    override fun equals(other: Any?): Boolean {
        return (other as? NutrientData)?.data?.contentDeepEquals(data) ?: false
    }

    override fun hashCode(): Int = data.contentHashCode()

    override fun toString(): String {
        val str = StringBuilder("NutrientData [")
        for (n in Nutrients.nutrients) {
            str.append("$n : ${get(n)}, ")
        }
        str.append("]")
        return str.toString()
    }

    // creates a mutable copy
    fun copy() : NutrientData {
        return NutrientData(dataCompleteIfNotNull).also { copy ->
            for (i in data.indices) {
                copy.data[i] = this.data[i]
                copy.isDataComplete[i] = this.isDataComplete[i]
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
        assert(!isImmutable) { "NutrientData has been made immutable" }
    }
    operator fun get(n: Nutrient): NutrientValue? = data[n.index]

    fun amountOf(n: Nutrient, unit: Unit? = null): Double? {
        val nutrientValue = this[n] ?: return null

        return if (unit == null) {
            nutrientValue.value
        } else {
            require(n.isConvertibleTo(unit)) { "Cannot convert nutrient $n to $unit" }
            nutrientValue.convertValueTo(unit)
        }
    }

    fun amountOf(n: Nutrient, unit: Unit? = null, defaultValue: Double): Double {
        return amountOf(n, unit) ?: defaultValue
    }


    fun hasNutrient(n: Nutrient) : Boolean {
        return this[n] != null
    }


    operator fun set(n: Nutrient, value: NutrientValue?) {
        assertMutable()
        data[n.index] = value
        if (dataCompleteIfNotNull) {
            isDataComplete[n.index] = value != null
        }
    }

    fun hasCompleteData(n: Nutrient) = isDataComplete[n.index]

    internal fun markCompleteData(n: Nutrient, complete: Boolean) {
        isDataComplete[n.index] = complete
    }


    fun getUnitOrDefault(n: Nutrient) : Unit {
        return this[n]?.unit ?: DefaultUnits.get(n)
    }


    // hack for USDA foods
    // subtracts fibre from carbohydrate if necessary to produce a carbohydrate amount
    // If fibre is not present, returns just carbs by diff
    // If there is not enough data to do that, return 0.
    private val carbsBestEffort: Double
        get() = if (hasCompleteData(Nutrients.CARBOHYDRATE)) {
            amountOf(Nutrients.CARBOHYDRATE)!!
        } else if (hasCompleteData(Nutrients.CARBOHYDRATE_BY_DIFF) && hasCompleteData(Nutrients.FIBRE)) {
            amountOf(Nutrients.CARBOHYDRATE_BY_DIFF)!! - amountOf(Nutrients.FIBRE)!!
        } else if (hasCompleteData(Nutrients.CARBOHYDRATE_BY_DIFF)) {
            amountOf(Nutrients.CARBOHYDRATE_BY_DIFF)!!
        } else {
            0.0
        }

    val proteinEnergyComponent: Double
        get() = getEnergyComponent(Nutrients.PROTEIN)

    val fatsEnergyComponent : Double
        get() = getEnergyComponent(Nutrients.FAT) + getEnergyComponent(Nutrients.SATURATED_FAT)

    val carbsEnergyComponent: Double
        get() = getEnergyComponent(Nutrients.CARBOHYDRATE) + getEnergyComponent(Nutrients.SUGAR)

    val fibreEnergyComponent: Double
        get() = getEnergyComponent(Nutrients.FIBRE)

    val proteinEnergyProportion : Double
        get() = getEnergyProportion(Nutrients.PROTEIN)

    val carbsEnergyProportion : Double
        get() = getEnergyProportion(Nutrients.CARBOHYDRATE) + getEnergyProportion(Nutrients.SUGAR)

    val fatsEnergyProportion : Double
        get() = getEnergyProportion(Nutrients.FAT) + getEnergyProportion(Nutrients.SATURATED_FAT)

    val fibreEnergyProportion: Double
        get() = getEnergyProportion(Nutrients.FIBRE)

    fun getEnergyProportion(n: Nutrient) : Double {
        if (!energyProportionNutrients.contains(n)) {
            return 0.0
        }
        // the map is computed the first time this function is called
        return energyProportionsMap[n] ?: 0.0
    }

    fun getEnergyComponent(n: Nutrient, unit: Unit = Units.CALORIES) : Double {
        if (!energyProportionNutrients.contains(n)) {
            return 0.0
        }

        require (unit.type == UnitType.ENERGY) { "Invalid energy unit" }

        // the map is computed the first time this function is called
        return (energyComponentsMap[n] ?: 0.0) * unit.metricEquivalent
    }

    companion object {
        val Nutrient.index : Int
            get() = id.toInt()

        // measured in the relevant FoodTable's QuantityUnits
        const val DEFAULT_QUANTITY = 100.0

        val energyProportionNutrients = setOf(
                Nutrients.PROTEIN,
                Nutrients.FAT,
                Nutrients.SATURATED_FAT,
                Nutrients.CARBOHYDRATE,
                Nutrients.SUGAR,
                Nutrients.FIBRE
        )

    }

}



