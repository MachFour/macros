package com.machfour.macros.json

import com.machfour.macros.entities.INutrient
import com.machfour.macros.entities.Unit
import com.machfour.macros.nutrients.*
import com.machfour.macros.units.KILOJOULES
import com.machfour.macros.units.NutrientUnits
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class JsonNutrientData(
    @SerialName("per_quantity")
    override val perQuantity: JsonQuantity,
    val nutrients: Set<JsonNutrientValue>,
    @Transient
    val incompleteData: Set<INutrient> = emptySet(),
    @Transient
    override val foodDensity: Double? = null,
): NutrientData<JsonNutrientValue> {

    private val nutrientMap = nutrients.associateBy { it.nutrient }

    override fun nutrientValues(): Map<INutrient, JsonNutrientValue> {
        return nutrients.associateBy { it.nutrient }
    }

    override fun getUnit(n: INutrient): Unit? {
        return nutrientMap[n]?.unit
    }

    override fun getValueOrNull(n: INutrient): JsonNutrientValue? {
        return nutrientMap[n]
    }

    override fun hasNutrient(n: INutrient): Boolean {
        return n in nutrientMap
    }

    override fun getEnergyProportion(n: INutrient): Double {
        return energyProportionsMap[n] ?: 0.0
    }

    // map of protein, fat, saturated fat, carbs, sugar, fibre to amount of energy in calories
    private val energyComponentsMapKj: Map<INutrient, Double> by lazy {
        makeEnergyComponentsMap(KILOJOULES)
    }

    // map of protein, fat, saturated fat, carbs, sugar, fibre to proportion of total energy
    private val energyProportionsMap: Map<INutrient, Double> by lazy {
        makeEnergyProportionsMap(KILOJOULES, energyComponentsMapKj)
    }

    override fun hasIncompleteData(n: INutrient): Boolean {
        return n in incompleteData
    }

    override fun incompleteDataNutrients(): Set<INutrient> {
        return incompleteData
    }

    override fun toString(): String {
        return nutrientDataToString(this)
    }

    override fun fillMissingData(other: BasicNutrientData<JsonNutrientValue>): JsonNutrientData {
        val newValues = HashSet<JsonNutrientValue>()
        val incompleteData = HashSet<INutrient>()
        for (n in AllNutrients) {
            val thisValue = getValueOrNull(n)
            val otherValue = getValueOrNull(n)

            val resultValue = thisValue ?: otherValue

            resultValue?.let {
                newValues.add(it)
            }

            // note: hasIncompleteData is a stricter condition than hasData:
            // hasIncompleteData can be true even if there is a non-null value for that column, when the
            // nData object was produced by summation and there was at least one food with missing data.
            // for this purpose, we'll only replace the primary data if it was null

            val resultIsDataIncomplete = when (thisValue != null) {
                true -> hasIncompleteData(n)
                false -> other.hasIncompleteData(n) || otherValue == null
            }

            if (resultIsDataIncomplete) {
                incompleteData.add(n)
            }
        }
        return JsonNutrientData(
            perQuantity = this.perQuantity,
            nutrients = newValues,
            incompleteData = incompleteData
        )
    }

    override fun scale(ratio: Double): NutrientData<JsonNutrientValue> {
        TODO("Not yet implemented")
    }

    override fun withQuantityUnit(unit: Unit): NutrientData<JsonNutrientValue> {
        TODO("Not yet implemented")
    }

    override fun withDefaultUnits(
        defaultUnits: NutrientUnits
    ): NutrientData<JsonNutrientValue> {
        TODO("Not yet implemented")
    }


    /*
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JsonNutrientData) {
            return false
        }

        if (perQuantity != other.perQuantity) {
            return false
        }

        if (nutrients != other.nutrients) {
            return false
        }

        if (incompleteData != other.incompleteData) {
            return false
        }

        if (foodDensity != other.foodDensity) {
            return false
        }
        return true
    }
     */
}